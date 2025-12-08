package com.gxuwz.ccsa.ui.resident;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.MediaGridAdapter;
import com.gxuwz.ccsa.model.PostMedia;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MediaSelectActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MediaGridAdapter adapter;
    private TextView btnContinue;
    private List<PostMedia> allMedia = new ArrayList<>();
    private List<PostMedia> selectedMedia = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_select);

        // 1. 初始化UI
        recyclerView = findViewById(R.id.recycler_view);
        btnContinue = findViewById(R.id.tv_continue);

        findViewById(R.id.iv_close).setOnClickListener(v -> finish());

        // 2. 初始化列表
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        adapter = new MediaGridAdapter(this, allMedia);
        recyclerView.setAdapter(adapter);

        // 3. 点击继续按钮逻辑
        btnContinue.setOnClickListener(v -> {
            selectedMedia = adapter.getSelectedItems();
            // 不选媒体也可以跳过去发纯文字，所以不需要 return
            Intent intent = new Intent(MediaSelectActivity.this, PostEditActivity.class);
            intent.putExtra("selected_media", (Serializable) selectedMedia);
            startActivity(intent);
            // 建议：不要在这里finish()，等发完帖子再销毁，或者根据需求决定
        });

        // 4. 检查权限并加载数据
        checkPermissionAndLoad();
    }

    private void checkPermissionAndLoad() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        } else {
            // 已有权限，直接加载
            loadMedia();
        }
    }

    private void loadMedia() {
        new Thread(() -> {
            ContentResolver contentResolver = getContentResolver();

            // 同时查询图片和视频
            // 注意：这里为了简化，先只查图片。如果要混排视频，需要更复杂的查询或分别查询后合并
            Cursor cursor = contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null,
                    null,
                    null,
                    MediaStore.Images.Media.DATE_ADDED + " DESC");

            if (cursor != null) {
                int count = 0;
                while (cursor.moveToNext()) {
                    // 获取路径
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                    PostMedia media = new PostMedia();
                    media.url = path;
                    media.type = 1; // 1代表图片
                    allMedia.add(media);

                    count++;
                    // 为了性能，演示Demo可以限制只加载前500张
                    if (count > 500) break;
                }
                cursor.close();
                Log.d("MediaSelect", "Loaded " + allMedia.size() + " images.");
            } else {
                Log.e("MediaSelect", "Cursor is null");
            }

            // 刷新UI
            runOnUiThread(() -> {
                if (allMedia.isEmpty()) {
                    Toast.makeText(MediaSelectActivity.this, "相册为空或未读取到图片", Toast.LENGTH_SHORT).show();
                }
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限授予成功，加载图片
                loadMedia();
            } else {
                Toast.makeText(this, "需要读取存储权限才能选择照片", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
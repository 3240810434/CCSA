package com.gxuwz.ccsa.ui.resident;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
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
import com.gxuwz.ccsa.model.User;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MediaSelectActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MediaGridAdapter adapter;
    private TextView btnContinue;
    private List<PostMedia> allMedia = new ArrayList<>();
    private List<PostMedia> selectedMedia = new ArrayList<>();
    private User currentUser;

    // 临时内部类，用于排序
    private static class MediaItem {
        long id;
        Uri uri;
        long dateAdded;
        int type; // 1: Image, 2: Video

        public MediaItem(long id, Uri uri, long dateAdded, int type) {
            this.id = id;
            this.uri = uri;
            this.dateAdded = dateAdded;
            this.type = type;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_select);

        // 接收用户信息
        currentUser = (User) getIntent().getSerializableExtra("user");

        // 初始化视图
        recyclerView = findViewById(R.id.recycler_view);
        btnContinue = findViewById(R.id.tv_continue);
        findViewById(R.id.iv_close).setOnClickListener(v -> finish());

        // 设置列表
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        adapter = new MediaGridAdapter(this, allMedia);
        recyclerView.setAdapter(adapter);

        // 点击继续
        btnContinue.setOnClickListener(v -> {
            selectedMedia = adapter.getSelectedItems();
            Intent intent = new Intent(MediaSelectActivity.this, PostEditActivity.class);
            intent.putExtra("selected_media", (Serializable) selectedMedia);
            // 传递用户信息到编辑页
            intent.putExtra("user", currentUser);
            startActivity(intent);
            // 关键：关闭当前页面，这样PostEditActivity关闭后会直接回到LifeDynamicsFragment
            finish();
        });

        checkPermissionAndLoad();
    }

    private void checkPermissionAndLoad() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        } else {
            loadMedia();
        }
    }

    private void loadMedia() {
        new Thread(() -> {
            List<MediaItem> tempItems = new ArrayList<>();
            ContentResolver contentResolver = getContentResolver();

            // 1. 查询图片
            try (Cursor cursor = contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED},
                    null, null, MediaStore.Images.Media.DATE_ADDED + " DESC")) {

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                        long date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                        Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                        tempItems.add(new MediaItem(id, uri, date, 1));
                    }
                }
            } catch (Exception e) {
                Log.e("MediaSelect", "Error loading images", e);
            }

            // 2. 查询视频
            try (Cursor cursor = contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Video.Media._ID, MediaStore.Video.Media.DATE_ADDED},
                    null, null, MediaStore.Video.Media.DATE_ADDED + " DESC")) {

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                        long date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED));
                        Uri uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                        tempItems.add(new MediaItem(id, uri, date, 2));
                    }
                }
            } catch (Exception e) {
                Log.e("MediaSelect", "Error loading videos", e);
            }

            Collections.sort(tempItems, (o1, o2) -> Long.compare(o2.dateAdded, o1.dateAdded));

            List<PostMedia> finalList = new ArrayList<>();
            for (MediaItem item : tempItems) {
                PostMedia m = new PostMedia();
                m.url = item.uri.toString();
                m.type = item.type;
                finalList.add(m);
                if (finalList.size() > 1000) break;
            }

            runOnUiThread(() -> {
                allMedia.clear();
                allMedia.addAll(finalList);
                adapter.notifyDataSetChanged();
                if (allMedia.isEmpty()) {
                    Toast.makeText(this, "未找到媒体文件", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadMedia();
        } else {
            Toast.makeText(this, "没有权限无法获取照片", Toast.LENGTH_SHORT).show();
        }
    }
}
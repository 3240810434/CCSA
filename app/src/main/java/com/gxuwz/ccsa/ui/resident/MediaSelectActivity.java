package com.gxuwz.ccsa.ui.resident;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build; // 需要导入 Build
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

    // ============ 修改重点：适配 Android 13+ 权限 ============
    private void checkPermissionAndLoad() {
        List<String> permissionsToRequest = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (API 33) 及以上：申请细分媒体权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO);
            }
        } else {
            // Android 12 及以下：申请旧版存储权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), 100);
        } else {
            loadMedia();
        }
    }
    // =======================================================

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
                    // 如果权限获取了但还是没图，可能是相册真没图，或者权限被永久拒绝了
                    // 可以在这里提示更详细的信息，但在 loadMedia 调用前权限已检查
                    Toast.makeText(this, "未扫描到媒体文件", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            boolean allGranted = true;
            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
            } else {
                allGranted = false;
            }

            if (allGranted) {
                loadMedia();
            } else {
                Toast.makeText(this, "请在设置中开启存储权限以获取照片", Toast.LENGTH_LONG).show();
            }
        }
    }
}
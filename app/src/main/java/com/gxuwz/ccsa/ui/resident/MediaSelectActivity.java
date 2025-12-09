package com.gxuwz.ccsa.ui.resident;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
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
    private boolean isHelpPost = false; // 标记是否来自求助帖页面

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

        currentUser = (User) getIntent().getSerializableExtra("user");
        isHelpPost = getIntent().getBooleanExtra("is_help_post", false);

        recyclerView = findViewById(R.id.recycler_view);
        btnContinue = findViewById(R.id.tv_continue);
        findViewById(R.id.iv_close).setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        adapter = new MediaGridAdapter(this, allMedia);
        recyclerView.setAdapter(adapter);

        btnContinue.setOnClickListener(v -> handleContinue());

        checkPermissionAndLoad();
    }

    private void handleContinue() {
        selectedMedia = adapter.getSelectedItems();

        if (selectedMedia.isEmpty()) {
            Toast.makeText(this, "请先选择图片或视频", Toast.LENGTH_SHORT).show();
            return;
        }

        // === 校验逻辑：图片最多9张，视频只能1个，且不可混选 ===
        int imageCount = 0;
        int videoCount = 0;
        for (PostMedia m : selectedMedia) {
            if (m.type == 2) videoCount++; // 假设 2 是视频
            else imageCount++;             // 假设 1 是图片
        }

        if (videoCount > 0 && imageCount > 0) {
            Toast.makeText(this, "不能同时选择图片和视频", Toast.LENGTH_SHORT).show();
            return;
        }
        if (videoCount > 1) {
            Toast.makeText(this, "视频最多只能选择 1 个", Toast.LENGTH_SHORT).show();
            return;
        }
        if (imageCount > 9) {
            Toast.makeText(this, "图片最多只能选择 9 张", Toast.LENGTH_SHORT).show();
            return;
        }
        // ====================================================

        if (isHelpPost) {
            // 如果是求助帖，返回数据给 HelpPostEditActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_media", (Serializable) selectedMedia);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } else {
            // 原有的生活动态逻辑，跳转到 PostEditActivity
            Intent intent = new Intent(MediaSelectActivity.this, PostEditActivity.class);
            intent.putExtra("selected_media", (Serializable) selectedMedia);
            intent.putExtra("user", currentUser);
            startActivity(intent);
            finish();
        }
    }

    private void checkPermissionAndLoad() {
        List<String> permissionsToRequest = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED)
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), 100);
        } else {
            loadMedia();
        }
    }

    private void loadMedia() {
        new Thread(() -> {
            List<MediaItem> tempItems = new ArrayList<>();
            ContentResolver contentResolver = getContentResolver();
            try (Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
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
            } catch (Exception e) { Log.e("MediaSelect", "Error", e); }

            try (Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
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
            } catch (Exception e) { Log.e("MediaSelect", "Error", e); }

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
            });
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) loadMedia();
            else Toast.makeText(this, "请开启权限", Toast.LENGTH_SHORT).show();
        }
    }
}
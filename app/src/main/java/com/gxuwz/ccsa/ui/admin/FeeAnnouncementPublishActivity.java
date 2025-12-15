// 文件路径: app/src/main/java/com/gxuwz/ccsa/ui/admin/FeeAnnouncementPublishActivity.java
package com.gxuwz.ccsa.ui.admin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.FeeAnnouncement;
import com.gxuwz.ccsa.model.Notification;
import com.gxuwz.ccsa.model.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FeeAnnouncementPublishActivity extends AppCompatActivity {

    // 定义请求码
    private static final int REQUEST_CODE_PICK_FILE = 1001;
    private static final int REQUEST_CODE_PERMISSION = 1002;

    private String community;
    private String adminAccount;
    private EditText etTitle, etContent, etStartTime, etEndTime;
    private TextView tvAttachmentName;
    private Button btnUpload, btnPublish;
    private String attachmentFileName = ""; // 存储真实获取的附件名称
    private Uri attachmentUri; // 存储附件Uri (如需上传文件流可使用此Uri)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fee_announcement_publish);

        community = getIntent().getStringExtra("community");
        adminAccount = getIntent().getStringExtra("adminAccount");

        initViews();
    }

    private void initViews() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        etStartTime = findViewById(R.id.et_start_time);
        etEndTime = findViewById(R.id.et_end_time);
        tvAttachmentName = findViewById(R.id.tv_attachment_name);
        btnUpload = findViewById(R.id.btn_upload_attachment);
        btnPublish = findViewById(R.id.btn_publish);

        // 自动填充今天的日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());
        etStartTime.setText(today);

        // 点击上传附件，执行权限检查和文件选择
        btnUpload.setOnClickListener(v -> checkPermissionAndPickFile());

        btnPublish.setOnClickListener(v -> publishAnnouncement());
    }

    /**
     * 检查权限并打开文件选择器
     */
    private void checkPermissionAndPickFile() {
        // Android 13 (API 33) 及以上使用 Intent.ACTION_GET_CONTENT (SAF) 通常不需要 READ_EXTERNAL_STORAGE 权限即可访问用户选择的文件
        // 为了兼容旧版本，进行权限判断
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // 申请读取存储权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_PERMISSION);
            } else {
                openFilePicker();
            }
        } else {
            // Android 13+ 直接打开文件选择器
            openFilePicker();
        }
    }

    /**
     * 打开系统文件选择器
     */
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // 允许选择所有类型文件，如果只想选PDF可改为 "application/pdf"
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "请选择公示附件"), REQUEST_CODE_PICK_FILE);
        } catch (Exception e) {
            Toast.makeText(this, "未找到文件管理器", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 权限申请回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else {
                Toast.makeText(this, "需要存储权限才能读取手机文件", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Activity结果回调 (处理文件选择结果)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                attachmentUri = uri;
                // 获取文件名
                attachmentFileName = getFileName(uri);
                tvAttachmentName.setText("已添加附件: " + attachmentFileName);
                tvAttachmentName.setVisibility(View.VISIBLE);
                Toast.makeText(this, "附件已选择", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 根据Uri获取文件名
     */
    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void publishAnnouncement() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "请填写完整公示信息", Toast.LENGTH_SHORT).show();
            return;
        }

        // 如果有附件，追加到内容后面
        // 注意：实际项目中通常需要将文件(attachmentUri)上传到服务器并获取URL，此处仅保存文件名作为演示
        final String finalContent = content + (attachmentFileName.isEmpty() ? "" : "\n\n[附件]: " + attachmentFileName);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);

            // 1. 保存公示记录
            FeeAnnouncement announcement = new FeeAnnouncement(
                    community,
                    title,
                    finalContent,
                    startTime,
                    endTime,
                    System.currentTimeMillis(),
                    adminAccount
            );
            db.feeAnnouncementDao().insert(announcement);

            // 2. 获取该小区所有居民
            List<User> residents = db.userDao().findResidentsByCommunity(community);

            // 3. 为每位居民创建未读通知
            if (residents != null && !residents.isEmpty()) {
                for (User resident : residents) {
                    Notification notification = new Notification(
                            community,
                            resident.getPhone(),
                            "费用公示通知: " + title,
                            "物业发布了新的费用公示，请点击查看。\n\n" + finalContent, // 内容包含详情
                            1, // 1 代表系统/缴费类通知
                            new Date(),
                            false // 未读
                    );
                    db.notificationDao().insert(notification);
                }
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "发布成功，已通知 " + (residents != null ? residents.size() : 0) + " 位居民", Toast.LENGTH_LONG).show();
                finish();
            });
        });
    }
}
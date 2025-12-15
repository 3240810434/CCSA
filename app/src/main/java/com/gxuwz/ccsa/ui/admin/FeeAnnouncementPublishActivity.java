// 文件路径: app/src/main/java/com/gxuwz/ccsa/ui/admin/FeeAnnouncementPublishActivity.java
package com.gxuwz.ccsa.ui.admin;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
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

    private String community;
    private String adminAccount;
    private EditText etTitle, etContent, etStartTime, etEndTime;
    private TextView tvAttachmentName;
    private Button btnUpload, btnPublish;
    private String simulatedAttachmentPath = ""; // 模拟附件路径

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

        // 模拟文件上传
        btnUpload.setOnClickListener(v -> {
            simulatedAttachmentPath = "2023年度物业费用审计报告.pdf";
            tvAttachmentName.setText("已添加附件: " + simulatedAttachmentPath);
            tvAttachmentName.setVisibility(View.VISIBLE);
            Toast.makeText(this, "附件上传成功", Toast.LENGTH_SHORT).show();
        });

        btnPublish.setOnClickListener(v -> publishAnnouncement());
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

        // 如果有附件，追加到内容后面（因为数据库没有附件字段，为了演示效果）
        final String finalContent = content + (simulatedAttachmentPath.isEmpty() ? "" : "\n\n[附件]: " + simulatedAttachmentPath);

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

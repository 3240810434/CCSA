package com.gxuwz.ccsa.ui.admin;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.FeeAnnouncement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FeeAnnouncementActivity extends AppCompatActivity {

    private String community;
    private String adminAccount;
    private EditText etTitle;
    private EditText etContent;
    private EditText etStartTime;
    private EditText etEndTime;
    private Button btnPublish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 确保布局文件名正确
        setContentView(R.layout.activity_fee_announcement);

        community = getIntent().getStringExtra("community");
        adminAccount = getIntent().getStringExtra("adminAccount");

        initViews();
        setupListeners();
    }

    private void initViews() {
        // 确保所有控件ID与布局文件一致
        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        etStartTime = findViewById(R.id.et_start_time);
        etEndTime = findViewById(R.id.et_end_time);
        btnPublish = findViewById(R.id.btn_publish);
    }

    private void setupListeners() {
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

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            FeeAnnouncement announcement = new FeeAnnouncement(
                    community,
                    title,
                    content,
                    startTime,
                    endTime,
                    System.currentTimeMillis(),
                    adminAccount
            );

            // 确保FeeAnnouncementDao中存在insert方法
            AppDatabase.getInstance(this).feeAnnouncementDao().insert(announcement);

            runOnUiThread(() -> {
                Toast.makeText(this, "费用公示发布成功", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}

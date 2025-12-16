package com.gxuwz.ccsa.ui.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.AdminNotice;
import com.gxuwz.ccsa.util.DateUtils; // 确保导入了这个包
import java.util.concurrent.Executors;

public class AdminNoticeDetailActivity extends AppCompatActivity {
    private long noticeId;
    private AdminNotice notice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notice_detail);

        noticeId = getIntent().getLongExtra("notice_id", -1);
        if (noticeId == -1) finish();

        loadData();

        findViewById(R.id.btn_resend).setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminNoticeEditActivity.class);
            intent.putExtra("notice_id", noticeId);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btn_delete).setOnClickListener(v -> showDeleteConfirm());
    }

    private void loadData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            notice = AppDatabase.getInstance(this).adminNoticeDao().getById(noticeId);
            runOnUiThread(() -> updateUI());
        });
    }

    private void updateUI() {
        if (notice == null) return;
        ((TextView)findViewById(R.id.tv_detail_title)).setText(notice.getTitle());
        ((TextView)findViewById(R.id.tv_detail_content)).setText(notice.getContent());

        // 确保 DateUtils 中有 dateToString 方法，且 notice.getPublishTime() 返回 java.util.Date
        ((TextView)findViewById(R.id.tv_detail_info)).setText("发布人: 管理员  发布时间: " + DateUtils.dateToString(notice.getPublishTime()));

        String targetStr = "对象: ";
        if ("BOTH".equals(notice.getTargetType())) targetStr += "全体商家, 居民";
        else if ("MERCHANT".equals(notice.getTargetType())) targetStr += "全体商家";
        else targetStr += "居民";

        if (notice.getTargetBuildings() != null && !notice.getTargetBuildings().isEmpty()) {
            targetStr += "(" + (notice.getTargetBuildings().equals("ALL") ? "全楼栋" : notice.getTargetBuildings() + "栋") + ")";
        }
        ((TextView)findViewById(R.id.tv_detail_target)).setText(targetStr);

        if (notice.getAttachmentPath() != null) {
            ImageView iv = findViewById(R.id.iv_detail_attachment);
            iv.setVisibility(View.VISIBLE);
            iv.setImageURI(Uri.parse(notice.getAttachmentPath()));
        }
    }

    private void showDeleteConfirm() {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("删除此通知将同步撤回所有已发送给用户的消息，确认操作吗？")
                .setPositiveButton("删除", (dialog, which) -> deleteNotice())
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteNotice() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.adminNoticeDao().delete(notice);
            db.notificationDao().deleteByAdminNoticeId(noticeId);

            runOnUiThread(() -> {
                Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
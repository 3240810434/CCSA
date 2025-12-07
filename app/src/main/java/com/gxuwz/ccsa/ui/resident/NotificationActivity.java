// 路径：CCSA/app/src/main/java/com/gxuwz/ccsa/ui/resident/NotificationActivity.java
package com.gxuwz.ccsa.ui.resident;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.NotificationAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Notification;
import com.gxuwz.ccsa.model.User;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.OnItemClickListener {
    private static final String TAG = "NotificationActivity";
    private User currentUser;
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // 获取用户信息（优化提示文案，保持逻辑一致性）
        currentUser = (User) getIntent().getSerializableExtra("user");
        if (currentUser == null) {
            Log.e(TAG, "用户信息获取失败");
            Toast.makeText(this, "用户信息错误，请重新登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadNotifications();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this, null, this);
        recyclerView.setAdapter(adapter);
    }

    // 加载通知数据（已关联当前用户，查询用户专属通知）
    private void loadNotifications() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // 根据当前用户手机号查询关联通知
                List<Notification> notifications = AppDatabase.getInstance(this)
                        .notificationDao()
                        .getByRecipientPhone(currentUser.getPhone());

                runOnUiThread(() -> {
                    if (notifications != null && !notifications.isEmpty()) {
                        adapter.updateData(notifications);
                    } else {
                        Toast.makeText(this, "暂无通知", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "加载通知失败", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "加载通知失败：" + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } finally {
                executor.shutdown();
            }
        });
    }

    // 通知点击事件，显示详情
    @Override
    public void onItemClick(Notification notification) {
        if (notification == null) return;

        // 标记为已读
        markAsRead(notification.getId());

        // 显示详情对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_notification_detail, null);
        builder.setView(view);
        builder.setTitle("通知详情");
        builder.setPositiveButton("确定", null);

        // 设置详情内容
        TextView tvTitle = view.findViewById(R.id.tv_detail_title);
        TextView tvContent = view.findViewById(R.id.tv_detail_content);
        TextView tvTime = view.findViewById(R.id.tv_detail_time);

        tvTitle.setText(notification.getTitle());
        tvContent.setText(notification.getContent());
        tvTime.setText(sdf.format(notification.getCreateTime()));

        builder.show();
    }

    // 标记通知为已读
    private void markAsRead(long notificationId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                AppDatabase.getInstance(this).notificationDao().markAsRead(notificationId);
            } catch (Exception e) {
                Log.e(TAG, "标记通知为已读失败", e);
            } finally {
                executor.shutdown();
            }
        });
    }
}
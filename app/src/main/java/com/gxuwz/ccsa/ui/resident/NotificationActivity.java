package com.gxuwz.ccsa.ui.resident;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.NotificationAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.ChatMessage;
import com.gxuwz.ccsa.model.Notification;
import com.gxuwz.ccsa.model.UnifiedMessage;
import com.gxuwz.ccsa.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.OnItemClickListener {
    private static final String TAG = "NotificationActivity";
    private User currentUser;
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private AppDatabase db;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        currentUser = (User) getIntent().getSerializableExtra("user");
        if (currentUser == null) {
            Toast.makeText(this, "用户信息错误，请重新登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = AppDatabase.getInstance(this);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次回到页面时重新加载，以刷新最新消息
        loadUnifiedData();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // 设置标题栏返回
        findViewById(R.id.btn_back).setOnClickListener(v -> finish()); // 假设你的布局有返回按钮
    }

    // 核心方法：加载并合并数据
    private void loadUnifiedData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                List<UnifiedMessage> unifiedList = new ArrayList<>();

                // 1. 获取系统通知 (原有逻辑)
                List<Notification> systemNotifications = db.notificationDao()
                        .getByRecipientPhone(currentUser.getPhone());
                for (Notification n : systemNotifications) {
                    unifiedList.add(new UnifiedMessage(n));
                }

                // 2. 获取聊天会话 (类似于 MessageListActivity 的逻辑)
                // 获取所有与我有关的消息
                List<ChatMessage> allChatMsgs = db.chatDao().getAllMyMessages(currentUser.getId());
                Map<Integer, ChatMessage> latestMsgMap = new HashMap<>();

                // 过滤出每个会话的最新一条
                for (ChatMessage msg : allChatMsgs) {
                    int otherId = (msg.senderId == currentUser.getId()) ? msg.receiverId : msg.senderId;
                    if (!latestMsgMap.containsKey(otherId)) {
                        latestMsgMap.put(otherId, msg);
                    }
                }

                // 将最新的一条聊天转换为 UnifiedMessage
                for (Map.Entry<Integer, ChatMessage> entry : latestMsgMap.entrySet()) {
                    int targetId = entry.getKey();
                    ChatMessage msg = entry.getValue();

                    // 查询对方用户信息以获取名字和头像
                    User targetUser = db.userDao().getUserById(targetId);
                    String titleName = (targetUser != null) ? targetUser.getName() : "未知用户";
                    String avatar = (targetUser != null) ? targetUser.getAvatar() : null;

                    unifiedList.add(new UnifiedMessage(msg, titleName, targetId, avatar));
                }

                // 3. 统一按时间倒序排序
                Collections.sort(unifiedList);

                // 4. 更新UI
                runOnUiThread(() -> {
                    if (!unifiedList.isEmpty()) {
                        adapter.updateData(unifiedList);
                    } else {
                        Toast.makeText(this, "暂无消息", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "加载数据失败", e);
            } finally {
                executor.shutdown();
            }
        });
    }

    // 点击事件分发
    @Override
    public void onItemClick(UnifiedMessage message) {
        if (message.getType() == UnifiedMessage.TYPE_CHAT_MESSAGE) {
            // ---> 情况A：如果是聊天，跳转到 ChatActivity
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("currentUser", currentUser);
            intent.putExtra("targetUserId", message.getChatTargetId()); // 传递对方ID
            startActivity(intent);

        } else if (message.getType() == UnifiedMessage.TYPE_SYSTEM_NOTICE) {
            // ---> 情况B：如果是系统通知，显示详情弹窗
            showSystemNotificationDetail((Notification) message.getData());
        }
    }

    // 显示系统通知详情
    private void showSystemNotificationDetail(Notification notification) {
        // 标记为已读
        new Thread(() -> db.notificationDao().markAsRead(notification.getId())).start();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_notification_detail, null);
        builder.setView(view);
        builder.setTitle("通知详情");
        builder.setPositiveButton("确定", null);

        TextView tvTitle = view.findViewById(R.id.tv_detail_title);
        TextView tvContent = view.findViewById(R.id.tv_detail_content);
        TextView tvTime = view.findViewById(R.id.tv_detail_time);

        tvTitle.setText(notification.getTitle());
        tvContent.setText(notification.getContent());
        if (notification.getCreateTime() != null) {
            tvTime.setText(sdf.format(notification.getCreateTime()));
        }

        builder.show();
    }
}
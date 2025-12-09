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
        setContentView(R.layout.activity_notification); // 加载修复后的布局

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
        loadUnifiedData(); // 每次进入页面刷新数据
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 初始化适配器
        adapter = new NotificationAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // 修复崩溃点：现在 activity_notification.xml 中已经有了 btn_back
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        } else {
            Log.e(TAG, "未找到 btn_back，请检查 XML 布局");
        }
    }

    // 核心功能：加载并合并 系统通知 和 聊天消息
    private void loadUnifiedData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                List<UnifiedMessage> unifiedList = new ArrayList<>();

                // 1. 获取系统通知
                List<Notification> systemNotifications = db.notificationDao()
                        .getByRecipientPhone(currentUser.getPhone());
                if (systemNotifications != null) {
                    for (Notification n : systemNotifications) {
                        unifiedList.add(new UnifiedMessage(n));
                    }
                }

                // 2. 获取邻里互助聊天会话 (显示每个人最新的一条)
                List<ChatMessage> allChatMsgs = db.chatDao().getAllMyMessages(currentUser.getId());
                if (allChatMsgs != null) {
                    Map<Integer, ChatMessage> latestMsgMap = new HashMap<>();
                    for (ChatMessage msg : allChatMsgs) {
                        // 确定聊天对象ID
                        int otherId = (msg.senderId == currentUser.getId()) ? msg.receiverId : msg.senderId;
                        // 因为 allChatMsgs 通常是按时间倒序查的，第一次遇到就是最新的
                        if (!latestMsgMap.containsKey(otherId)) {
                            latestMsgMap.put(otherId, msg);
                        }
                    }

                    // 转换为 UnifiedMessage
                    for (Map.Entry<Integer, ChatMessage> entry : latestMsgMap.entrySet()) {
                        int targetId = entry.getKey();
                        ChatMessage msg = entry.getValue();

                        // 查询对方名字和头像
                        User targetUser = db.userDao().getUserById(targetId);
                        String titleName = (targetUser != null) ? targetUser.getName() : "未知用户";
                        String avatar = (targetUser != null) ? targetUser.getAvatar() : null;

                        unifiedList.add(new UnifiedMessage(msg, titleName, targetId, avatar));
                    }
                }

                // 3. 统一按时间倒序排序 (实现将新消息排在前面)
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

    // 处理点击事件：根据类型跳转
    @Override
    public void onItemClick(UnifiedMessage message) {
        if (message.getType() == UnifiedMessage.TYPE_CHAT_MESSAGE) {
            // ---> 情况A：如果是邻里互助聊天，跳转到聊天页面
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
        // 异步标记为已读
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
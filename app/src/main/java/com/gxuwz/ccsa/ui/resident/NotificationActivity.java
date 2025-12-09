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

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.OnItemClickListener, NotificationAdapter.OnItemLongClickListener {
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
        // 每次页面可见时刷新数据，确保头像和名字同步最新
        loadUnifiedData();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 初始化适配器，传入 点击监听 和 长按监听
        adapter = new NotificationAdapter(this, new ArrayList<>(), this, this);
        recyclerView.setAdapter(adapter);

        // 返回按钮处理
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    // 加载并合并 系统通知 和 聊天消息
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

                // 2. 获取聊天会话
                // 此方法需确保在 DAO 中实现：SELECT * FROM chat_message WHERE senderId = :myId OR receiverId = :myId ORDER BY createTime DESC
                List<ChatMessage> allChatMsgs = db.chatDao().getAllMyMessages(currentUser.getId());

                if (allChatMsgs != null) {
                    Map<Integer, ChatMessage> latestMsgMap = new HashMap<>();

                    for (ChatMessage msg : allChatMsgs) {
                        // 核心修复：准确计算对方ID
                        // 如果我是发送者，对方就是receiverId；如果我是接收者，对方就是senderId
                        int otherId = (msg.senderId == currentUser.getId()) ? msg.receiverId : msg.senderId;

                        // 因为查询结果是按时间倒序的，所以第一次遇到的 otherId 对应的消息就是最新一条
                        if (!latestMsgMap.containsKey(otherId)) {
                            latestMsgMap.put(otherId, msg);
                        }
                    }

                    // 将最新的一条聊天转换为 UnifiedMessage
                    for (Map.Entry<Integer, ChatMessage> entry : latestMsgMap.entrySet()) {
                        int targetId = entry.getKey();
                        ChatMessage msg = entry.getValue();

                        // 查询对方用户信息以获取名字和头像 (实现“绑定”功能)
                        User targetUser = db.userDao().getUserById(targetId);

                        // 默认值处理，防止“未知用户”
                        String titleName = (targetUser != null) ? targetUser.getName() : "邻居 (ID:" + targetId + ")";
                        String avatar = (targetUser != null) ? targetUser.getAvatar() : null;

                        unifiedList.add(new UnifiedMessage(msg, titleName, targetId, avatar));
                    }
                }

                // 3. 统一按时间倒序排序
                Collections.sort(unifiedList);

                // 4. 更新UI
                runOnUiThread(() -> {
                    if (adapter != null) {
                        adapter.updateData(unifiedList);
                    }
                    if (unifiedList.isEmpty()) {
                        // 可以选择显示空状态视图
                        // Toast.makeText(this, "暂无消息", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "加载数据失败", e);
                runOnUiThread(() -> Toast.makeText(this, "数据加载异常", Toast.LENGTH_SHORT).show());
            } finally {
                executor.shutdown();
            }
        });
    }

    // 单击跳转
    @Override
    public void onItemClick(UnifiedMessage message) {
        if (message.getType() == UnifiedMessage.TYPE_CHAT_MESSAGE) {
            // 跳转到聊天页面
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("currentUser", currentUser);
            intent.putExtra("targetUserId", message.getChatTargetId());
            startActivity(intent);

        } else if (message.getType() == UnifiedMessage.TYPE_SYSTEM_NOTICE) {
            // 显示系统通知详情
            showSystemNotificationDetail((Notification) message.getData());
        }
    }

    // 新增：长按删除功能
    @Override
    public void onItemLongClick(UnifiedMessage message) {
        if (message.getType() == UnifiedMessage.TYPE_CHAT_MESSAGE) {
            // 弹出确认删除对话框
            new AlertDialog.Builder(this)
                    .setTitle("删除聊天")
                    .setMessage("确定要删除与 " + message.getTitle() + " 的所有聊天记录吗？")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确认", (dialog, which) -> {
                        // 执行删除操作
                        deleteConversation(message.getChatTargetId());
                    })
                    .show();
        } else {
            // 系统通知长按也可以做删除处理，这里暂时只做聊天的
            Toast.makeText(this, "系统通知暂不支持删除", Toast.LENGTH_SHORT).show();
        }
    }

    // 执行数据库删除并刷新
    private void deleteConversation(int targetId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // 调用 DAO 删除会话 (需确保 ChatDao 中有 deleteConversation 方法)
                db.chatDao().deleteConversation(currentUser.getId(), targetId);

                runOnUiThread(() -> {
                    Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
                    // 刷新列表
                    loadUnifiedData();
                });
            } catch (Exception e) {
                Log.e(TAG, "删除失败", e);
                runOnUiThread(() -> Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show());
            } finally {
                executor.shutdown();
            }
        });
    }

    // 显示系统通知详情弹窗
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
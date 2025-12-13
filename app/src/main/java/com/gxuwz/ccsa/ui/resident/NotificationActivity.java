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
import com.gxuwz.ccsa.model.Merchant;
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
                // 【修复1】这里传入 "RESIDENT" 作为当前用户的角色
                List<ChatMessage> allChatMsgs = db.chatDao().getAllMyMessages(currentUser.getId(), "RESIDENT");

                if (allChatMsgs != null) {
                    // 使用 String Key (Role + "_" + ID) 防止 ID 冲突
                    Map<String, ChatMessage> latestMsgMap = new HashMap<>();

                    for (ChatMessage msg : allChatMsgs) {
                        // 确定对方是谁
                        int otherId;
                        String otherRole;

                        // 如果我是发送者(且角色匹配)，对方就是接收者；反之亦然
                        if (msg.senderId == currentUser.getId() && "RESIDENT".equals(msg.senderRole)) {
                            otherId = msg.receiverId;
                            otherRole = msg.receiverRole;
                        } else {
                            otherId = msg.senderId;
                            otherRole = msg.senderRole;
                        }

                        // 组合Key
                        String key = otherRole + "_" + otherId;

                        // 因为查询结果是按时间倒序的，所以第一次遇到的就是最新一条
                        if (!latestMsgMap.containsKey(key)) {
                            latestMsgMap.put(key, msg);
                        }
                    }

                    // 将最新的一条聊天转换为 UnifiedMessage
                    for (ChatMessage msg : latestMsgMap.values()) {
                        // 重新解析出对方的 ID 和 Role
                        int targetId;
                        String targetRole;

                        if (msg.senderId == currentUser.getId() && "RESIDENT".equals(msg.senderRole)) {
                            targetId = msg.receiverId;
                            targetRole = msg.receiverRole;
                        } else {
                            targetId = msg.senderId;
                            targetRole = msg.senderRole;
                        }

                        String titleName = "未知用户";
                        String avatar = null;

                        // 【新增】根据角色去查不同的表
                        if ("MERCHANT".equals(targetRole)) {
                            Merchant m = db.merchantDao().findById(targetId);
                            if (m != null) {
                                titleName = m.getMerchantName();
                                avatar = m.getAvatar();
                            }
                        } else {
                            // 默认为居民
                            User u = db.userDao().getUserById(targetId);
                            if (u != null) {
                                titleName = u.getName();
                                avatar = u.getAvatar();
                            }
                        }

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
            // 获取原始消息以判断目标角色
            ChatMessage msg = (ChatMessage) message.getData();

            int targetId;
            String targetRole;

            if (msg.senderId == currentUser.getId() && "RESIDENT".equals(msg.senderRole)) {
                targetId = msg.receiverId;
                targetRole = msg.receiverRole;
            } else {
                targetId = msg.senderId;
                targetRole = msg.senderRole;
            }

            // 跳转到通用聊天页面
            Intent intent = new Intent(this, ChatActivity.class);
            // 传递我的信息
            intent.putExtra("myId", currentUser.getId());
            intent.putExtra("myRole", "RESIDENT");

            // 传递对方信息
            intent.putExtra("targetId", targetId);
            intent.putExtra("targetRole", targetRole);

            // 传递显示信息（优化体验）
            intent.putExtra("targetName", message.getTitle());
            // intent.putExtra("targetAvatar", ...); // 如果 UnifiedMessage 里有也可以传

            startActivity(intent);

        } else if (message.getType() == UnifiedMessage.TYPE_SYSTEM_NOTICE) {
            // 显示系统通知详情
            showSystemNotificationDetail((Notification) message.getData());
        }
    }

    // 长按删除功能
    @Override
    public void onItemLongClick(UnifiedMessage message) {
        if (message.getType() == UnifiedMessage.TYPE_CHAT_MESSAGE) {
            ChatMessage msg = (ChatMessage) message.getData();

            // 解析目标信息
            int targetId;
            String targetRole;
            if (msg.senderId == currentUser.getId() && "RESIDENT".equals(msg.senderRole)) {
                targetId = msg.receiverId;
                targetRole = msg.receiverRole;
            } else {
                targetId = msg.senderId;
                targetRole = msg.senderRole;
            }

            // 需要 final 变量传入 lambda
            final int tId = targetId;
            final String tRole = targetRole;

            // 弹出确认删除对话框
            new AlertDialog.Builder(this)
                    .setTitle("删除聊天")
                    .setMessage("确定要删除与 " + message.getTitle() + " 的所有聊天记录吗？")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确认", (dialog, which) -> {
                        // 执行删除操作
                        deleteConversation(tId, tRole);
                    })
                    .show();
        } else {
            Toast.makeText(this, "系统通知暂不支持删除", Toast.LENGTH_SHORT).show();
        }
    }

    // 【修复2】执行数据库删除并刷新，增加 targetRole 参数
    private void deleteConversation(int targetId, String targetRole) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // 调用 DAO 删除会话，传入我的 ID 和 Role，以及对方的 ID 和 Role
                db.chatDao().deleteConversation(currentUser.getId(), "RESIDENT", targetId, targetRole);

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
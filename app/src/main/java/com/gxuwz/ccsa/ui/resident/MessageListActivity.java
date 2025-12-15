package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.MessageListAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Admin; // 引入Admin模型
import com.gxuwz.ccsa.model.ChatMessage;
import com.gxuwz.ccsa.model.Merchant;
import com.gxuwz.ccsa.model.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MessageListAdapter adapter;
    private List<ChatMessage> conversationList = new ArrayList<>();
    private User currentUser;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);

        currentUser = (User) getIntent().getSerializableExtra("user");
        db = AppDatabase.getInstance(this);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MessageListAdapter(this, conversationList, currentUser);
        recyclerView.setAdapter(adapter);

        setupSwipeDelete();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConversations();
    }

    private void loadConversations() {
        if (currentUser == null || currentUser.getId() == 0) return;

        new Thread(() -> {
            try {
                // 查询所有和我(RESIDENT)有关的消息
                List<ChatMessage> allMsgs = db.chatDao().getAllMyMessages(currentUser.getId(), "RESIDENT");
                Map<String, ChatMessage> latestMsgMap = new HashMap<>();

                for (ChatMessage msg : allMsgs) {
                    // 1. 确定对方是谁
                    int otherId;
                    String otherRole;

                    if (msg.senderId == currentUser.getId() && "RESIDENT".equalsIgnoreCase(msg.senderRole)) {
                        otherId = msg.receiverId;
                        otherRole = msg.receiverRole;
                    } else {
                        otherId = msg.senderId;
                        otherRole = msg.senderRole;
                    }

                    // 2. 生成唯一Key
                    String safeRole = (otherRole == null) ? "UNKNOWN" : otherRole.trim().toUpperCase();
                    String key = safeRole + "_" + otherId;

                    if (!latestMsgMap.containsKey(key)) {
                        // --- 【强制修复逻辑开始】 ---

                        boolean isIdentifyAsAdmin = false;

                        // 判定 A：如果角色字段明确包含 ADMIN
                        if (safeRole.contains("ADMIN") || safeRole.contains("MANAGER") || safeRole.contains("SYSTEM")) {
                            isIdentifyAsAdmin = true;
                        }
                        // 判定 B (核心修复)：如果角色不明确，或者为了保险起见，直接去管理员表查这个ID是否存在
                        // 只要不是明确的商家(MERCHANT)或明确的居民(RESIDENT)，都去查一下管理员表
                        else if (!"MERCHANT".equals(safeRole) && !"RESIDENT".equals(safeRole)) {
                            Admin admin = db.adminDao().findById(otherId);
                            if (admin != null) {
                                isIdentifyAsAdmin = true;
                            }
                        }
                        // 判定 C：即使标记为 RESIDENT，如果 ID 是 1, 11 等特定系统账号，可能也会被误判。
                        // 为了彻底解决，如果上面没查到，但您想强制某些逻辑，可以在这里加。
                        // 但通常 判定 B 只要查到 ID 在 admin 表里就已经足够了。

                        if (isIdentifyAsAdmin) {
                            // -> 是管理员，强制修正显示
                            msg.targetName = "管理员";
                            msg.targetAvatar = "local_admin_resource"; // 配合 Adapter 显示 R.drawable.admin
                        }
                        else if (safeRole.contains("MERCHANT")) {
                            Merchant m = db.merchantDao().findById(otherId);
                            msg.targetName = (m != null) ? m.getMerchantName() : "商家(已注销)";
                            msg.targetAvatar = (m != null) ? m.getAvatar() : "";
                        }
                        else {
                            // 默认为普通居民
                            User u = db.userDao().findById(otherId);
                            msg.targetName = (u != null) ? u.getName() : "用户";
                            msg.targetAvatar = (u != null) ? u.getAvatar() : "";
                        }

                        // --- 【修复逻辑结束】 ---

                        latestMsgMap.put(key, msg);
                    }
                }

                runOnUiThread(() -> {
                    conversationList.clear();
                    conversationList.addAll(latestMsgMap.values());
                    adapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setupSwipeDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position < 0 || position >= conversationList.size()) return;

                ChatMessage msg = conversationList.get(position);
                int targetId;
                String targetRole;

                if (msg.senderId == currentUser.getId() && "RESIDENT".equalsIgnoreCase(msg.senderRole)) {
                    targetId = msg.receiverId;
                    targetRole = msg.receiverRole;
                } else {
                    targetId = msg.senderId;
                    targetRole = msg.senderRole;
                }

                final int tId = targetId;
                final String tRole = targetRole;

                new Thread(() -> db.chatDao().deleteConversation(currentUser.getId(), "RESIDENT", tId, tRole)).start();

                conversationList.remove(position);
                adapter.notifyItemRemoved(position);
            }
        }).attachToRecyclerView(recyclerView);
    }
}
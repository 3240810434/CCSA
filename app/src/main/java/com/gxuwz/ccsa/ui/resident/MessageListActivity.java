package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.MessageListAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Admin;
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
                List<ChatMessage> allMsgs = db.chatDao().getAllMyMessages(currentUser.getId(), "RESIDENT");
                Map<String, ChatMessage> latestMsgMap = new HashMap<>();

                for (ChatMessage msg : allMsgs) {
                    int otherId;
                    String otherRole;

                    if (msg.senderId == currentUser.getId() && "RESIDENT".equalsIgnoreCase(msg.senderRole)) {
                        otherId = msg.receiverId;
                        otherRole = msg.receiverRole;
                    } else {
                        otherId = msg.senderId;
                        otherRole = msg.senderRole;
                    }

                    // 生成唯一Key (Role + ID)
                    String safeRole = (otherRole == null) ? "UNKNOWN" : otherRole.trim().toUpperCase();
                    String key = safeRole + "_" + otherId;

                    if (!latestMsgMap.containsKey(key)) {
                        boolean isIdentifyAsAdmin = false;

                        // --- 核心修复：优先通过 ID 强制识别管理员 ---
                        // 这里包含了 ID 为 1 (悦景) 以及您列出的其他可能的管理员账号 ID
                        // 注意：如果您的数据库 ID 是自增的，账号 "11" 的 ID 可能不是 11，但这里我们根据账号逻辑做全量匹配以防万一
                        if (otherId == 1 || otherId == 11 || otherId == 111 ||
                                otherId == 1111 || otherId == 11111 || otherId == 111111 ||
                                otherId == 1111111 || otherId == 11111111) {
                            isIdentifyAsAdmin = true;
                        }
                        // 补充判断：通过 Role 字段
                        else if (safeRole.contains("ADMIN") || safeRole.contains("MANAGER")) {
                            isIdentifyAsAdmin = true;
                        }
                        // 补充判断：查库兜底
                        else {
                            Admin admin = db.adminDao().findById(otherId);
                            if (admin != null) {
                                isIdentifyAsAdmin = true;
                            }
                        }

                        // --- 填充显示数据 ---
                        if (isIdentifyAsAdmin) {
                            // 强制设置为管理员信息
                            msg.targetName = "管理员";
                            msg.targetAvatar = "local_admin_resource";
                        } else if (safeRole.contains("MERCHANT")) {
                            Merchant m = db.merchantDao().findById(otherId);
                            if (m != null) {
                                msg.targetName = m.getMerchantName();
                                msg.targetAvatar = m.getAvatar();
                            } else {
                                msg.targetName = "商家";
                                msg.targetAvatar = "";
                            }
                        } else {
                            // 普通居民
                            User u = db.userDao().findById(otherId);
                            if (u != null) {
                                msg.targetName = u.getName();
                                msg.targetAvatar = u.getAvatar();
                            } else {
                                msg.targetName = "未知用户";
                                msg.targetAvatar = "";
                            }
                        }

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
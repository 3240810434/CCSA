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
                // 1. 为了保险起见，重新查询一次当前用户的最新信息（确保小区字段准确）
                User freshUser = db.userDao().findById(currentUser.getId());
                if (freshUser == null) freshUser = currentUser;

                // 2. 【核心修复】获取当前小区对应的物业管理员 ID
                // 不依赖消息里的 Role 字符串，直接查人
                int propertyAdminId = -1; // -1 代表未找到或无效
                if (freshUser.getCommunity() != null) {
                    Admin admin = db.adminDao().findByCommunity(freshUser.getCommunity());
                    if (admin != null) {
                        propertyAdminId = admin.getId();
                        Log.d("MessageList", "Found Property Admin ID to hide: " + propertyAdminId);
                    }
                }

                // 3. 获取所有消息
                List<ChatMessage> allMsgs = db.chatDao().getAllMyMessages(currentUser.getId(), "RESIDENT");
                Map<String, ChatMessage> latestMsgMap = new HashMap<>();

                for (ChatMessage msg : allMsgs) {
                    int otherId;
                    String otherRole;

                    // 判断对方是谁
                    if (msg.senderId == currentUser.getId() && "RESIDENT".equalsIgnoreCase(msg.senderRole)) {
                        otherId = msg.receiverId;
                        otherRole = msg.receiverRole;
                    } else {
                        otherId = msg.senderId;
                        otherRole = msg.senderRole;
                    }

                    // 防止空指针
                    String safeRole = (otherRole == null) ? "UNKNOWN" : otherRole.trim().toUpperCase();

                    // ============================================================
                    // 【强力过滤逻辑】
                    // ============================================================

                    // A. ID 精准匹配：如果对方ID等于该小区的物业管理员ID，直接隐藏
                    if (propertyAdminId != -1 && otherId == propertyAdminId) {
                        continue;
                    }

                    // B. 角色字符串匹配：如果角色包含 ADMIN，隐藏
                    if (safeRole.contains("ADMIN")) {
                        continue;
                    }

                    // C. 常见管理员ID硬编码过滤（兜底）
                    if (otherId == 1 || otherId == 11 || otherId == 11111) {
                        continue;
                    }

                    // ============================================================
                    // 通过过滤后，才添加到显示列表
                    // ============================================================

                    String key = safeRole + "_" + otherId;

                    if (!latestMsgMap.containsKey(key)) {
                        // 补充显示信息
                        if (safeRole.contains("MERCHANT")) {
                            Merchant m = db.merchantDao().findById(otherId);
                            if (m != null) {
                                msg.targetName = m.getMerchantName();
                                msg.targetAvatar = m.getAvatar();
                            } else {
                                msg.targetName = "商家";
                            }
                        } else {
                            // 默认为普通居民
                            User u = db.userDao().findById(otherId);
                            if (u != null) {
                                msg.targetName = u.getName();
                                msg.targetAvatar = u.getAvatar();
                            } else {
                                msg.targetName = "未知用户";
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

                // 解析删除对象
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
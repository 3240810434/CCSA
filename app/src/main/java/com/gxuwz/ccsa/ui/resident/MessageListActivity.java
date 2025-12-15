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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                // =================================================================
                // 【核心修复：构建终极屏蔽黑名单】
                // =================================================================
                Set<Integer> adminIdBlacklist = new HashSet<>();

                // 1. 尝试从数据库获取所有管理员 (不区分小区，全部拉取)
                try {
                    List<Admin> allAdmins = db.adminDao().getAll();
                    if (allAdmins != null) {
                        for (Admin a : allAdmins) {
                            adminIdBlacklist.add(a.getId());
                        }
                    }
                } catch (Exception e) {
                    Log.e("MessageList", "获取管理员列表失败", e);
                }

                // 2. 【双重保险】手动添加已知的默认管理员ID (对应 App.java 中的初始化)
                // 即使数据库没查到，这些 ID 也会被强制屏蔽
                adminIdBlacklist.add(1);          // 悦景
                adminIdBlacklist.add(11);         // 梧桐
                adminIdBlacklist.add(111);        // 阳光
                adminIdBlacklist.add(1111);       // 锦园
                adminIdBlacklist.add(11111);      // 幸福
                adminIdBlacklist.add(111111);     // 芳邻
                adminIdBlacklist.add(1111111);    // 逸景
                adminIdBlacklist.add(11111111);   // 康城

                Log.d("MessageList", "屏蔽名单ID数量: " + adminIdBlacklist.size());

                // =================================================================
                // 3. 获取消息并过滤
                // =================================================================
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

                    // 规范化角色字符串
                    String safeRole = (otherRole == null) ? "UNKNOWN" : otherRole.trim().toUpperCase();

                    // -------------------------------------------------------------
                    // 【过滤条件 A】按角色名屏蔽 (如果数据库存了 ADMIN)
                    // -------------------------------------------------------------
                    if (safeRole.contains("ADMIN") || safeRole.contains("PROPERTY")) {
                        continue;
                    }

                    // -------------------------------------------------------------
                    // 【过滤条件 B】按 ID 黑名单屏蔽 (核弹级过滤)
                    // 逻辑：如果对方ID在管理员名单里，且对方不是明确的 居民/商家，则直接视为管理员并隐藏
                    // -------------------------------------------------------------
                    boolean isExplicitlySafe = safeRole.contains("RESIDENT") || safeRole.contains("MERCHANT");

                    if (adminIdBlacklist.contains(otherId) && !isExplicitlySafe) {
                        Log.d("MessageList", "隐藏了管理员消息 ID: " + otherId);
                        continue;
                    }

                    // -------------------------------------------------------------
                    // 通过过滤，添加到显示列表
                    // -------------------------------------------------------------
                    String key = safeRole + "_" + otherId;

                    if (!latestMsgMap.containsKey(key)) {
                        // 补充头像和名称
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
                                msg.targetName = "用户 " + otherId;
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
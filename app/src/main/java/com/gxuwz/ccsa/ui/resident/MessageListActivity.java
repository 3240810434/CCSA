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
                // 1. 获取当前用户的最新信息
                User freshUser = db.userDao().findById(currentUser.getId());
                if (freshUser == null) freshUser = currentUser;

                // 2. 获取当前小区所有物业管理员ID
                List<Integer> propertyAdminIds = new ArrayList<>();
                if (freshUser.getCommunity() != null) {
                    // 查询该小区的所有管理员
                    List<Admin> admins = db.adminDao().findAllByCommunity(freshUser.getCommunity());
                    if (admins != null && !admins.isEmpty()) {
                        for (Admin admin : admins) {
                            propertyAdminIds.add(admin.getId());
                            Log.d("MessageList", "Found Property Admin ID to hide: " + admin.getId());
                        }
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

                    // 过滤逻辑：如果是管理员或物业相关账号，不显示在消息中心
                    boolean isAdmin = safeRole.contains("ADMIN") ||
                            safeRole.contains("PROPERTY") ||
                            propertyAdminIds.contains(otherId);

                    // 如果是管理员，则跳过不添加到列表
                    if (isAdmin) {
                        continue;
                    }

                    // 通过过滤后，添加到显示列表
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
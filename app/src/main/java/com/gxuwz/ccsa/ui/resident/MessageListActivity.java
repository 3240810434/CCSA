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
                // 获取所有与我相关的消息
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

                    // 生成用于判断角色的字符串
                    String safeRole = (otherRole == null) ? "UNKNOWN" : otherRole.trim().toUpperCase();

                    // ============================================================
                    // 【核心修改】过滤逻辑：如果对方是管理员，直接跳过 (continue)
                    // ============================================================

                    // 1. 检查角色字段是否包含 ADMIN
                    if (safeRole.contains("ADMIN")) {
                        continue; // 直接跳过，不显示
                    }

                    // 2. 检查 ID 是否在已知的管理员 ID 列表中 (保留您原有的硬编码逻辑以防万一)
                    if (otherId == 1 || otherId == 11 || otherId == 111 ||
                            otherId == 1111 || otherId == 11111 || otherId == 111111 ||
                            otherId == 1111111 || otherId == 11111111) {
                        continue; // 直接跳过，不显示
                    }

                    // 3. (可选) 检查角色是否包含 MANAGER
                    if (safeRole.contains("MANAGER")) {
                        continue; // 直接跳过
                    }

                    // 4. (兜底) 查询 Admin 数据库表，确认是否为管理员
                    // 这一步比较耗时，前面的检查如果能拦截最好，拦截不到再查库
                    Admin adminCheck = db.adminDao().findById(otherId);
                    if (adminCheck != null) {
                        continue; // 是管理员，跳过
                    }

                    // ============================================================
                    // 只有通过了上面的过滤（不是管理员），才添加到显示列表
                    // ============================================================

                    String key = safeRole + "_" + otherId;

                    if (!latestMsgMap.containsKey(key)) {
                        // 处理普通用户或商家的显示信息
                        if (safeRole.contains("MERCHANT")) {
                            Merchant m = db.merchantDao().findById(otherId);
                            if (m != null) {
                                msg.targetName = m.getMerchantName();
                                msg.targetAvatar = m.getAvatar();
                            } else {
                                msg.targetName = "商家";
                                msg.targetAvatar = "";
                            }
                        } else {
                            // 默认为普通居民 (RESIDENT)
                            User u = db.userDao().findById(otherId);
                            if (u != null) {
                                msg.targetName = u.getName();
                                msg.targetAvatar = u.getAvatar();
                            } else {
                                msg.targetName = "未知用户";
                                msg.targetAvatar = "";
                            }
                        }

                        // 加入到 Map 中以去重（只保留最新的一条）
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
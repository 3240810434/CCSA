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

                    // 严谨的判断逻辑
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
                        // --- 【修复核心】 ---

                        // 1. 优先判断管理员：使用 contains 放宽条件，防止角色名差异（如 "ADMINISTRATOR" vs "ADMIN"）
                        if (safeRole.contains("ADMIN") || safeRole.contains("MANAGER") || safeRole.contains("SYSTEM")) {
                            msg.targetName = "管理员";
                            msg.targetAvatar = "local_admin_resource"; // 设置特殊标记
                        }
                        // 2. 其次判断商家
                        else if (safeRole.contains("MERCHANT")) {
                            Merchant m = db.merchantDao().findById(otherId);
                            msg.targetName = (m != null) ? m.getMerchantName() : "商家(已注销)";
                            msg.targetAvatar = (m != null) ? m.getAvatar() : "";
                        }
                        // 3. 最后是普通用户
                        else {
                            User u = db.userDao().findById(otherId);
                            msg.targetName = (u != null) ? u.getName() : "用户";
                            msg.targetAvatar = (u != null) ? u.getAvatar() : "";
                        }
                        // --- 【修复结束】 ---

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
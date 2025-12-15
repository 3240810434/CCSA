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

                    // 严谨的判断逻辑：如果是我发的(ID相同且角色是RESIDENT)，那对方就是接收者；否则对方是发送者
                    if (msg.senderId == currentUser.getId() && "RESIDENT".equalsIgnoreCase(msg.senderRole)) {
                        otherId = msg.receiverId;
                        otherRole = msg.receiverRole;
                    } else {
                        otherId = msg.senderId;
                        otherRole = msg.senderRole;
                    }

                    // 2. 生成唯一Key，区分不同角色的同ID用户
                    // 【Bug修复】增加 trim() 去除空格，防止数据库存储 "ADMIN " 导致匹配失败
                    String safeRole = (otherRole == null) ? "UNKNOWN" : otherRole.trim().toUpperCase();
                    String key = safeRole + "_" + otherId;

                    if (!latestMsgMap.containsKey(key)) {
                        // --- 【强制修复核心】 ---

                        // 优先判断：如果是管理员，直接写死，不需要查库！
                        if ("ADMIN".equals(safeRole) || "ADMINISTRATOR".equals(safeRole)) {
                            msg.targetName = "管理员";
                            // 设置一个特殊标记，Adapter 里识别这个标记
                            msg.targetAvatar = "local_admin_resource";
                        }
                        // 其次判断：如果是商家
                        else if ("MERCHANT".equals(safeRole)) {
                            Merchant m = db.merchantDao().findById(otherId);
                            msg.targetName = (m != null) ? m.getMerchantName() : "商家(已注销)";
                            msg.targetAvatar = (m != null) ? m.getAvatar() : "";
                        }
                        // 最后：既不是管理员也不是商家，那肯定是普通用户（RESIDENT）
                        else {
                            User u = db.userDao().findById(otherId);
                            // 如果查到了，用查到的名字；查不到（可能是数据错误）就显示“用户”
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
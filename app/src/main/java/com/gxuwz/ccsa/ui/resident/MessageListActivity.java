package com.gxuwz.ccsa.ui.resident;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

        // 传递 User 对象作为 "我的身份标识"，在 Adapter 内部会将其识别为 ID=user.id, Role="RESIDENT"
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
            // 查询所有和我(RESIDENT)有关的消息
            List<ChatMessage> allMsgs = db.chatDao().getAllMyMessages(currentUser.getId(), "RESIDENT");
            Map<String, ChatMessage> latestMsgMap = new HashMap<>();

            for (ChatMessage msg : allMsgs) {
                // 确定对方是谁
                int otherId;
                String otherRole;

                if (msg.senderId == currentUser.getId() && "RESIDENT".equals(msg.senderRole)) {
                    // 我发的，对方是 Receiver
                    otherId = msg.receiverId;
                    otherRole = msg.receiverRole;
                } else {
                    // 别人发的，对方是 Sender
                    otherId = msg.senderId;
                    otherRole = msg.senderRole;
                }

                // 组合 Key 避免不同 Role 的 ID 冲突
                String key = otherRole + "_" + otherId;

                if (!latestMsgMap.containsKey(key)) {
                    // 查询对方详细信息
                    if ("MERCHANT".equals(otherRole)) {
                        Merchant m = db.merchantDao().findById(otherId);
                        msg.targetName = (m != null) ? m.getMerchantName() : "商家(已注销)";
                        msg.targetAvatar = (m != null) ? m.getAvatar() : "";
                    } else {
                        User u = db.userDao().findById(otherId);
                        msg.targetName = (u != null) ? u.getName() : "用户";
                        msg.targetAvatar = (u != null) ? u.getAvatar() : "";
                    }
                    latestMsgMap.put(key, msg);
                }
            }

            runOnUiThread(() -> {
                conversationList.clear();
                conversationList.addAll(latestMsgMap.values());
                adapter.notifyDataSetChanged();
            });
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

                // 计算目标ID和角色，用于删除
                int targetId;
                String targetRole;
                if (msg.senderId == currentUser.getId() && "RESIDENT".equals(msg.senderRole)) {
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
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

        // 这里的 adapter 不需要改动，沿用你原来的
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
        new Thread(() -> {
            // Dao 已经修改为只返回未标记删除的消息
            List<ChatMessage> allMsgs = db.chatDao().getAllMyMessages(currentUser.getId());
            Map<Integer, ChatMessage> latestMsgMap = new HashMap<>();

            // 过滤出每个会话的最新一条消息
            for (ChatMessage msg : allMsgs) {
                int otherId = (msg.senderId == currentUser.getId()) ? msg.receiverId : msg.senderId;

                // 因为 list 是按时间倒序的 (DESC)，所以遇到的第一个就是最新的
                if (!latestMsgMap.containsKey(otherId)) {
                    // 补充对方用户信息
                    User target = db.userDao().getUserById(otherId);
                    if (target != null) {
                        msg.targetName = target.getName();
                        msg.targetAvatar = target.getAvatar();
                        latestMsgMap.put(otherId, msg);
                    } else {
                        // 如果找不到用户，给默认值防止崩溃
                        msg.targetName = "未知用户";
                    }
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

                // 防止数组越界（快速滑动时可能发生）
                if (position < 0 || position >= conversationList.size()) return;

                ChatMessage msg = conversationList.get(position);
                int targetId = (msg.senderId == currentUser.getId()) ? msg.receiverId : msg.senderId;

                // 数据库逻辑删除（只对我不可见）
                new Thread(() -> db.chatDao().deleteConversation(currentUser.getId(), targetId)).start();

                // UI移除
                conversationList.remove(position);
                adapter.notifyItemRemoved(position);
            }
        }).attachToRecyclerView(recyclerView);
    }
}
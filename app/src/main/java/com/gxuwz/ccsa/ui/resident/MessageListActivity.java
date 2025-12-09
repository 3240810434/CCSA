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
        // 如果当前用户ID有问题，直接返回，避免查询错误
        if (currentUser == null || currentUser.getId() == 0) return;

        new Thread(() -> {
            List<ChatMessage> allMsgs = db.chatDao().getAllMyMessages(currentUser.getId());
            Map<Integer, ChatMessage> latestMsgMap = new HashMap<>();

            for (ChatMessage msg : allMsgs) {
                // 排除无效ID (ID=0)
                if (msg.senderId == 0 || msg.receiverId == 0) continue;

                int otherId = (msg.senderId == currentUser.getId()) ? msg.receiverId : msg.senderId;

                if (!latestMsgMap.containsKey(otherId)) {
                    User target = db.userDao().getUserById(otherId);
                    if (target != null) {
                        msg.targetName = target.getName();
                        msg.targetAvatar = target.getAvatar();
                        latestMsgMap.put(otherId, msg);
                    } else {
                        // 处理已删除或未找到的用户
                        msg.targetName = "未知用户";
                        // 可以选择不添加到列表，或者显示为未知
                        latestMsgMap.put(otherId, msg);
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
                if (position < 0 || position >= conversationList.size()) return;

                ChatMessage msg = conversationList.get(position);
                int targetId = (msg.senderId == currentUser.getId()) ? msg.receiverId : msg.senderId;

                new Thread(() -> db.chatDao().deleteConversation(currentUser.getId(), targetId)).start();

                conversationList.remove(position);
                adapter.notifyItemRemoved(position);
            }
        }).attachToRecyclerView(recyclerView);
    }
}
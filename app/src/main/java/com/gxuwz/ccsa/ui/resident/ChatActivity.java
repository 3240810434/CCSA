package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.ChatAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.ChatMessage;
import com.gxuwz.ccsa.model.User;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText etInput;
    private Button btnSend;
    private ImageView ivBack, ivReport, ivHeaderAvatar;
    private TextView tvHeaderName;

    private User currentUser;
    private int targetUserId;
    private User targetUser;

    private ChatAdapter adapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = AppDatabase.getInstance(this);
        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        targetUserId = getIntent().getIntExtra("targetUserId", -1);

        initView();
        loadTargetUserInfo();
    }

    private void initView() {
        // 顶部栏
        ivBack = findViewById(R.id.iv_back);
        ivReport = findViewById(R.id.iv_report); // report.png
        ivHeaderAvatar = findViewById(R.id.iv_header_avatar);
        tvHeaderName = findViewById(R.id.tv_header_name);

        ivBack.setOnClickListener(v -> finish());
        ivReport.setOnClickListener(v -> {}); // 暂不实现

        // 聊天区
        recyclerView = findViewById(R.id.recycler_view);
        etInput = findViewById(R.id.et_input);
        btnSend = findViewById(R.id.btn_send);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // layoutManager.setStackFromEnd(true); // 让列表从底部开始
        recyclerView.setLayoutManager(layoutManager);

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadTargetUserInfo() {
        new Thread(() -> {
            targetUser = db.userDao().getUserById(targetUserId);
            runOnUiThread(() -> {
                if (targetUser != null) {
                    tvHeaderName.setText(targetUser.getName());
                    Glide.with(this).load(targetUser.getAvatar())
                            .placeholder(R.drawable.lan).into(ivHeaderAvatar);

                    // 初始化 Adapter
                    adapter = new ChatAdapter(this, messageList, currentUser, targetUser);
                    recyclerView.setAdapter(adapter);
                    loadMessages();
                }
            });
        }).start();
    }

    private void loadMessages() {
        new Thread(() -> {
            List<ChatMessage> msgs = db.chatDao().getChatHistory(currentUser.getId(), targetUserId);
            runOnUiThread(() -> {
                messageList.clear();
                messageList.addAll(msgs);
                adapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) {
                    recyclerView.scrollToPosition(messageList.size() - 1);
                }
            });
        }).start();
    }

    private void sendMessage() {
        String content = etInput.getText().toString().trim();
        if (TextUtils.isEmpty(content)) return;

        ChatMessage msg = new ChatMessage();
        msg.senderId = currentUser.getId();
        msg.receiverId = targetUserId;
        msg.content = content;
        msg.createTime = System.currentTimeMillis();

        new Thread(() -> {
            db.chatDao().insertMessage(msg);
            etInput.setText("");
            loadMessages(); // 重新加载刷新
        }).start();
    }
}
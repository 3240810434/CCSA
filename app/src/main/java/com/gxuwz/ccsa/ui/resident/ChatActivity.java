package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
        // 获取传递过来的数据
        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        targetUserId = getIntent().getIntExtra("targetUserId", -1);

        if (currentUser == null || targetUserId == -1) {
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initView();
        loadTargetUserInfo();
    }

    private void initView() {
        // 顶部栏
        ivBack = findViewById(R.id.iv_back);
        ivReport = findViewById(R.id.iv_report);
        ivHeaderAvatar = findViewById(R.id.iv_header_avatar);
        tvHeaderName = findViewById(R.id.tv_header_name);

        ivBack.setOnClickListener(v -> finish());
        ivReport.setOnClickListener(v -> Toast.makeText(this, "举报功能开发中", Toast.LENGTH_SHORT).show());

        // 聊天区
        recyclerView = findViewById(R.id.recycler_view);
        etInput = findViewById(R.id.et_input);
        btnSend = findViewById(R.id.btn_send);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // layoutManager.setStackFromEnd(true); // 如果需要让列表从底部开始显示可开启此行
        recyclerView.setLayoutManager(layoutManager);

        btnSend.setOnClickListener(v -> sendMessage());
    }

    // 加载目标用户信息（异步）
    private void loadTargetUserInfo() {
        new Thread(() -> {
            targetUser = db.userDao().getUserById(targetUserId);
            runOnUiThread(() -> {
                if (targetUser != null) {
                    tvHeaderName.setText(targetUser.getName());
                    Glide.with(this)
                            .load(targetUser.getAvatar())
                            .placeholder(R.drawable.lan)
                            .into(ivHeaderAvatar);

                    // 1. 在这里初始化 Adapter
                    adapter = new ChatAdapter(this, messageList, currentUser, targetUser);
                    recyclerView.setAdapter(adapter);

                    // 2. Adapter 就绪后，再加载消息
                    loadMessages();
                } else {
                    Toast.makeText(this, "未找到目标用户", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    // 加载聊天记录
    private void loadMessages() {
        new Thread(() -> {
            List<ChatMessage> msgs = db.chatDao().getChatHistory(currentUser.getId(), targetUserId);
            runOnUiThread(() -> {
                messageList.clear();
                if (msgs != null) {
                    messageList.addAll(msgs);
                }

                // 【核心修复】：增加判空，防止 adapter 为空导致 Crash
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                    if (!messageList.isEmpty()) {
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    }
                }
            });
        }).start();
    }

    private void sendMessage() {
        String content = etInput.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
            return;
        }

        // 如果目标用户还没加载出来，暂时不允许发送，防止数据错乱
        if (targetUser == null) {
            Toast.makeText(this, "正在加载用户信息...", Toast.LENGTH_SHORT).show();
            return;
        }

        ChatMessage msg = new ChatMessage();
        msg.senderId = currentUser.getId();
        msg.receiverId = targetUserId;
        msg.content = content;
        msg.createTime = System.currentTimeMillis();

        new Thread(() -> {
            // 插入数据库
            db.chatDao().insertMessage(msg);

            // 【修复】：UI 操作必须在主线程
            runOnUiThread(() -> {
                etInput.setText(""); // 清空输入框
                loadMessages();      // 刷新列表
            });
        }).start();
    }
}
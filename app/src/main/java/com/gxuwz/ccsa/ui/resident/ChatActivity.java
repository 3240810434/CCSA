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
        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        targetUserId = getIntent().getIntExtra("targetUserId", -1);

        if (currentUser == null || targetUserId == -1) {
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 【核心BUG修复】：检测 currentUser 的 ID 是否丢失（为0）
        // 这种情况常发生在注册后直接使用，或者 User 对象传递过程中数据不完整
        if (currentUser.getId() == 0) {
            // 尝试通过手机号在后台重新获取完整的 User 对象
            recoverUserIdentity();
        }

        initView();
        loadTargetUserInfo();
    }

    private void recoverUserIdentity() {
        new Thread(() -> {
            // 假设 User 模型中有 getPhone() 方法，且手机号唯一
            // 如果没有 phone 字段，请确保有其他唯一标识
            if (!TextUtils.isEmpty(currentUser.getPhone())) {
                User validUser = db.userDao().findByPhone(currentUser.getPhone());
                if (validUser != null) {
                    currentUser = validUser; // 修正 currentUser，此时 ID 应该 > 0

                    // 修正后刷新 Adapter（如果已经初始化）
                    runOnUiThread(() -> {
                        if (adapter != null) {
                            // ChatAdapter 需要有 setCurrentUser 方法，或者重建 Adapter
                            // 简单起见，这里不需要额外操作，因为发送时会使用最新的 currentUser.getId()
                        }
                    });
                }
            }
        }).start();
    }

    private void initView() {
        ivBack = findViewById(R.id.iv_back);
        ivReport = findViewById(R.id.iv_report);
        ivHeaderAvatar = findViewById(R.id.iv_header_avatar);
        tvHeaderName = findViewById(R.id.tv_header_name);

        ivBack.setOnClickListener(v -> finish());
        ivReport.setOnClickListener(v -> Toast.makeText(this, "举报功能开发中", Toast.LENGTH_SHORT).show());

        recyclerView = findViewById(R.id.recycler_view);
        etInput = findViewById(R.id.et_input);
        btnSend = findViewById(R.id.btn_send);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        btnSend.setOnClickListener(v -> sendMessage());
    }

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

                    // 初始化 Adapter
                    adapter = new ChatAdapter(this, messageList, currentUser, targetUser);
                    recyclerView.setAdapter(adapter);
                    loadMessages();
                } else {
                    Toast.makeText(this, "未找到目标用户", Toast.LENGTH_SHORT).show();
                    // 可以在这里禁用发送按钮
                    btnSend.setEnabled(false);
                }
            });
        }).start();
    }

    private void loadMessages() {
        // 确保使用最新的 ID 查询
        int myId = currentUser.getId();
        if (myId == 0) return; // 如果 ID 还没恢复，暂时不查，避免查出错误数据

        new Thread(() -> {
            List<ChatMessage> msgs = db.chatDao().getChatHistory(myId, targetUserId);
            runOnUiThread(() -> {
                messageList.clear();
                if (msgs != null) {
                    messageList.addAll(msgs);
                }
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

        if (targetUser == null) {
            Toast.makeText(this, "正在加载用户信息...", Toast.LENGTH_SHORT).show();
            return;
        }

        // 【核心防御】：再次检查 ID，防止发送 senderId=0 的消息
        if (currentUser.getId() == 0) {
            Toast.makeText(this, "用户信息同步中，请稍后再试", Toast.LENGTH_SHORT).show();
            // 再次尝试恢复
            recoverUserIdentity();
            return;
        }

        ChatMessage msg = new ChatMessage();
        msg.senderId = currentUser.getId(); // 此时 ID 应该是正确的
        msg.receiverId = targetUserId;
        msg.content = content;
        msg.createTime = System.currentTimeMillis();

        new Thread(() -> {
            db.chatDao().insertMessage(msg);
            runOnUiThread(() -> {
                etInput.setText("");
                loadMessages();
            });
        }).start();
    }
}
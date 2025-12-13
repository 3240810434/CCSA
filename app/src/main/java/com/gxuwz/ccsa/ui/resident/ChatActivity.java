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
import com.gxuwz.ccsa.model.Merchant;
import com.gxuwz.ccsa.model.User;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText etInput;
    private Button btnSend;
    private ImageView ivBack, ivReport, ivHeaderAvatar;
    private TextView tvHeaderName;

    private int myId;
    private String myRole; // "RESIDENT" or "MERCHANT"
    private int targetId;
    private String targetRole; // "RESIDENT" or "MERCHANT"

    // 缓存的头像 URL
    private String myAvatarUrl = "";
    private String targetAvatarUrl = "";
    private String targetNameStr = "";

    private ChatAdapter adapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = AppDatabase.getInstance(this);

        // 获取传递的参数
        myId = getIntent().getIntExtra("myId", -1);
        myRole = getIntent().getStringExtra("myRole");
        targetId = getIntent().getIntExtra("targetId", -1);
        targetRole = getIntent().getStringExtra("targetRole");

        // 尝试获取预传的名称和头像，优化体验
        if (getIntent().hasExtra("targetName")) {
            targetNameStr = getIntent().getStringExtra("targetName");
        }
        if (getIntent().hasExtra("targetAvatar")) {
            targetAvatarUrl = getIntent().getStringExtra("targetAvatar");
        }

        if (myId == -1 || targetId == -1 || TextUtils.isEmpty(myRole) || TextUtils.isEmpty(targetRole)) {
            Toast.makeText(this, "聊天参数错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initView();
        initData();
    }

    private void initView() {
        ivBack = findViewById(R.id.iv_back);
        ivReport = findViewById(R.id.iv_report);
        ivHeaderAvatar = findViewById(R.id.iv_header_avatar);
        tvHeaderName = findViewById(R.id.tv_header_name);

        // 设置初始标题
        tvHeaderName.setText(targetNameStr);
        if (!TextUtils.isEmpty(targetAvatarUrl)) {
            Glide.with(this).load(targetAvatarUrl).placeholder(R.drawable.ic_avatar).circleCrop().into(ivHeaderAvatar);
        }

        ivBack.setOnClickListener(v -> finish());
        ivReport.setOnClickListener(v -> Toast.makeText(this, "举报", Toast.LENGTH_SHORT).show());

        recyclerView = findViewById(R.id.recycler_view);
        etInput = findViewById(R.id.et_input);
        btnSend = findViewById(R.id.btn_send);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // 让消息从底部开始堆叠
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ChatAdapter(this, messageList);
        recyclerView.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void initData() {
        new Thread(() -> {
            // 1. 查询我的最新信息（为了头像）
            if ("MERCHANT".equals(myRole)) {
                Merchant me = db.merchantDao().findById(myId);
                if (me != null) myAvatarUrl = me.getAvatar();
            } else {
                User me = db.userDao().findById(myId);
                if (me != null) myAvatarUrl = me.getAvatar();
            }

            // 2. 查询对方最新信息（为了头像和名字）
            if ("MERCHANT".equals(targetRole)) {
                Merchant target = db.merchantDao().findById(targetId);
                if (target != null) {
                    targetNameStr = target.getMerchantName();
                    targetAvatarUrl = target.getAvatar();
                }
            } else {
                User target = db.userDao().findById(targetId);
                if (target != null) {
                    targetNameStr = target.getName();
                    targetAvatarUrl = target.getAvatar();
                }
            }

            runOnUiThread(() -> {
                tvHeaderName.setText(targetNameStr);
                Glide.with(this).load(targetAvatarUrl).placeholder(R.drawable.ic_avatar).circleCrop().into(ivHeaderAvatar);

                // 更新Adapter的配置
                adapter.setUserInfo(myId, myRole, myAvatarUrl, targetAvatarUrl);

                // 3. 加载消息
                loadMessages();
            });
        }).start();
    }

    private void loadMessages() {
        new Thread(() -> {
            List<ChatMessage> msgs = db.chatDao().getChatHistory(myId, myRole, targetId, targetRole);
            runOnUiThread(() -> {
                messageList.clear();
                if (msgs != null) {
                    messageList.addAll(msgs);
                }
                adapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) {
                    recyclerView.scrollToPosition(messageList.size() - 1);
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

        ChatMessage msg = new ChatMessage();
        msg.senderId = myId;
        msg.senderRole = myRole;
        msg.receiverId = targetId;
        msg.receiverRole = targetRole;
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
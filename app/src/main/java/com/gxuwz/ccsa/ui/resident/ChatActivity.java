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
    private String myRole; // "RESIDENT", "MERCHANT", "ADMIN"
    private int targetId;
    private String targetRole;

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

        // 尝试获取预传的名称和头像
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

        // 设置标题栏显示的对方信息
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
            } else if ("ADMIN".equals(myRole)) {
                // 【修复点1】如果是管理员，不查数据库，直接标记为空或特定值
                // 管理员头像是本地资源，不需要 URL
                myAvatarUrl = "";
            } else {
                // 只有不是商家且不是管理员时，才去查居民表
                User me = db.userDao().findById(myId);
                if (me != null) myAvatarUrl = me.getAvatar();
            }

            // 2. 查询对方最新信息（为了标题栏头像和名字）
            // 注意：如果对方是管理员，也需要类似处理，但目前这里主要是管理员发给居民
            if ("MERCHANT".equals(targetRole)) {
                Merchant target = db.merchantDao().findById(targetId);
                if (target != null) {
                    targetNameStr = target.getMerchantName();
                    targetAvatarUrl = target.getAvatar();
                }
            } else if ("RESIDENT".equals(targetRole)) {
                User target = db.userDao().findById(targetId);
                if (target != null) {
                    targetNameStr = target.getName();
                    targetAvatarUrl = target.getAvatar();
                }
            }

            runOnUiThread(() -> {
                // 再次更新标题栏（以防数据库信息比Intent传过来的新）
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
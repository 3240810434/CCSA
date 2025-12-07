// CCSA/app/src/main/java/com/gxuwz/ccsa/ui/admin/CreateVoteActivity.java
package com.gxuwz.ccsa.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Vote;
import com.gxuwz.ccsa.util.NotificationUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateVoteActivity extends AppCompatActivity {
    private EditText etTitle, etContent;
    private Button btnPublish;
    private String community;
    private String adminAccount;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_vote);

        // 获取参数
        community = getIntent().getStringExtra("community");
        adminAccount = getIntent().getStringExtra("adminAccount");
        db = AppDatabase.getInstance(this);

        // 初始化控件
        etTitle = findViewById(R.id.et_vote_title);
        etContent = findViewById(R.id.et_vote_content);
        btnPublish = findViewById(R.id.btn_publish_vote);

        // 发布按钮点击事件
        btnPublish.setOnClickListener(v -> publishVote());
    }

    private void publishVote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        // 输入验证
        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "请填写标题和内容", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建投票对象
        Vote vote = new Vote(
                title,
                content,
                community,
                adminAccount,
                System.currentTimeMillis()
        );

        // 保存到数据库
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            db.voteDao().insert(vote);

            // 发送通知给所有用户
            runOnUiThread(() -> {
                NotificationUtil.sendVoteNotification(
                        this,
                        "新的小区投票",
                        title
                );
                Toast.makeText(this, "投票发布成功", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}

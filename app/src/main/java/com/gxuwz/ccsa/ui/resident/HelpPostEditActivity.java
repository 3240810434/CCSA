package com.gxuwz.ccsa.ui.resident;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.HelpPost;
import com.gxuwz.ccsa.model.HelpPostMedia;
import com.gxuwz.ccsa.model.User;
import java.util.ArrayList;
import java.util.List;

public class HelpPostEditActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private ImageView ivPublishBtn;
    private User currentUser;
    private List<String> selectedMediaPaths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_post_edit);

        currentUser = (User) getIntent().getSerializableExtra("user");
        // 安全检查
        if (currentUser == null) {
            Toast.makeText(this, "用户信息错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        ivPublishBtn = findViewById(R.id.iv_publish_btn);

        ivPublishBtn.setOnClickListener(v -> attemptPublish());
    }

    private void attemptPublish() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "标题和内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("是否确认发布该求助帖子？")
                .setPositiveButton("确定", (dialog, which) -> saveToDb(title, content))
                .setNegativeButton("取消", null)
                .show();
    }

    private void saveToDb(String title, String content) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);

            // 【核心修复】：如果当前用户ID为0（通常发生在注册后直接跳转），尝试从数据库重新获取
            if (currentUser.getId() == 0 && !TextUtils.isEmpty(currentUser.getPhone())) {
                User dbUser = db.userDao().findByPhone(currentUser.getPhone());
                if (dbUser != null) {
                    currentUser = dbUser; // 更新为包含正确ID的用户对象
                }
            }

            // 再次检查，如果还是0，说明数据有问题，拦截
            if (currentUser.getId() == 0) {
                runOnUiThread(() -> Toast.makeText(this, "用户状态异常，请重新登录", Toast.LENGTH_SHORT).show());
                return;
            }

            HelpPost post = new HelpPost();
            post.userId = currentUser.getId(); // 此时 ID 应该是正确的
            post.title = title;
            post.content = content;
            post.createTime = System.currentTimeMillis();
            post.type = selectedMediaPaths.isEmpty() ? 0 : 1;

            long postId = db.helpPostDao().insertPost(post);

            // 保存媒体
            if (!selectedMediaPaths.isEmpty()) {
                List<HelpPostMedia> mediaList = new ArrayList<>();
                for (String path : selectedMediaPaths) {
                    HelpPostMedia media = new HelpPostMedia();
                    media.helpPostId = (int) postId;
                    media.url = path;
                    media.type = 1;
                    mediaList.add(media);
                }
                db.helpPostDao().insertMediaList(mediaList);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}
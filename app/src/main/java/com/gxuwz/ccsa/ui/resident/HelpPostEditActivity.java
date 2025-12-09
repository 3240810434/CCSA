package com.gxuwz.ccsa.ui.resident;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
    private ImageView ivPublishBtn; // fa_bu3.png
    private User currentUser;
    // 简化处理：这里假设你也复用了 MediaSelectActivity 来获取媒体路径
    // 如果没有，你需要实现一个简单的图片选择器
    private List<String> selectedMediaPaths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_post_edit);

        currentUser = (User) getIntent().getSerializableExtra("user");

        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        ivPublishBtn = findViewById(R.id.iv_publish_btn);

        // 发布点击事件
        ivPublishBtn.setOnClickListener(v -> attemptPublish());

        // TODO: 这里需要添加点击选择图片/视频的逻辑，以及展示已选缩略图的 UI
        // 参考 PostEditActivity 的实现
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
            HelpPost post = new HelpPost();
            post.userId = currentUser.getId();
            post.title = title;
            post.content = content;
            post.createTime = System.currentTimeMillis();
            post.type = selectedMediaPaths.isEmpty() ? 0 : 1; // 简化判断：0纯文，1带图

            AppDatabase db = AppDatabase.getInstance(this);
            long postId = db.helpPostDao().insertPost(post);

            // 保存媒体
            if (!selectedMediaPaths.isEmpty()) {
                List<HelpPostMedia> mediaList = new ArrayList<>();
                for (String path : selectedMediaPaths) {
                    HelpPostMedia media = new HelpPostMedia();
                    media.helpPostId = (int) postId;
                    media.url = path;
                    media.type = 1; // 假设是图片
                    mediaList.add(media);
                }
                db.helpPostDao().insertMediaList(mediaList);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT).show();
                finish(); // 返回上一页
            });
        }).start();
    }
}

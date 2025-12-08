package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Post;
import com.gxuwz.ccsa.model.PostMedia;
import com.gxuwz.ccsa.model.User;
import java.util.ArrayList;
import java.util.List;

public class PostEditActivity extends AppCompatActivity {
    private EditText etContent;
    private RecyclerView rvPreview;
    private List<PostMedia> mediaList;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_edit);

        mediaList = (ArrayList<PostMedia>) getIntent().getSerializableExtra("selected_media");
        currentUser = (User) getIntent().getSerializableExtra("user"); // 接收用户数据

        etContent = findViewById(R.id.et_content);
        rvPreview = findViewById(R.id.rv_preview);

        if (mediaList != null && !mediaList.isEmpty()) {
            rvPreview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            // 简单预览逻辑，此处省略Adapter设置，保持原有逻辑即可
        }

        findViewById(R.id.btn_publish).setOnClickListener(v -> publishPost());
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
    }

    private void publishPost() {
        String content = etContent.getText().toString().trim();
        if (TextUtils.isEmpty(content) && (mediaList == null || mediaList.isEmpty())) {
            Toast.makeText(this, "不可以发布空帖子", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            Post post = new Post();
            // 使用真实用户数据
            if (currentUser != null) {
                post.userId = currentUser.getId();
                post.userName = currentUser.getName();
                post.userAvatar = currentUser.getAvatar();
            } else {
                // 异常兜底
                post.userId = 0;
                post.userName = "未知用户";
                post.userAvatar = "";
            }

            post.content = content;
            post.createTime = System.currentTimeMillis();
            post.type = (mediaList != null && !mediaList.isEmpty()) ? mediaList.get(0).type : 0;

            long postId = AppDatabase.getInstance(this).postDao().insertPost(post);

            if (mediaList != null) {
                for (PostMedia media : mediaList) {
                    media.postId = (int) postId;
                }
                AppDatabase.getInstance(this).postDao().insertMedia(mediaList);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT).show();
                finish(); // 回到 LifeDynamicsFragment
            });
        }).start();
    }
}
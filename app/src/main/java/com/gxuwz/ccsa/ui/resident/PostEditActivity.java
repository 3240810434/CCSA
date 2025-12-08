package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.ImageGridAdapter; // 复用已有的或新建简单Adapter用于预览
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Post;
import com.gxuwz.ccsa.model.PostMedia;
import java.util.ArrayList;
import java.util.List;

public class PostEditActivity extends AppCompatActivity {
    private EditText etContent;
    private RecyclerView rvPreview;
    private List<PostMedia> mediaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_edit);

        mediaList = (ArrayList<PostMedia>) getIntent().getSerializableExtra("selected_media");
        etContent = findViewById(R.id.et_content);
        rvPreview = findViewById(R.id.rv_preview);

        // 简单的横向预览
        if (mediaList != null && !mediaList.isEmpty()) {
            rvPreview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            // 这里为了省事直接用MediaGridAdapter，实际建议写个简单的PreviewAdapter
            // adapter代码略，逻辑同上，只是显示图片
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
            post.userId = 1; // 假定当前登录用户ID
            post.userName = "居民小王";
            post.userAvatar = "";
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
                finish(); // 回到上级，需要关闭MediaSelectActivity建议使用flag
            });
        }).start();
    }
}

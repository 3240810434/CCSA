package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.CommentAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Comment;
import com.gxuwz.ccsa.model.Post;
import com.gxuwz.ccsa.model.PostMedia;
import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {
    private Post post;
    private RecyclerView rvComments;
    private CommentAdapter adapter;
    private List<Comment> commentList = new ArrayList<>();
    private EditText etComment;

    // 帖子详情控件
    private TextView tvName, tvContent;
    private LinearLayout mediaContainer;
    private ImageView ivAvatar, ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail_custom);

        post = (Post) getIntent().getSerializableExtra("post");

        // 初始化控件
        ivBack = findViewById(R.id.iv_back);
        rvComments = findViewById(R.id.rv_comments);
        etComment = findViewById(R.id.et_comment);
        Button btnSend = findViewById(R.id.btn_send); // 修正引用

        tvName = findViewById(R.id.detail_name);
        tvContent = findViewById(R.id.detail_content);
        mediaContainer = findViewById(R.id.detail_media_container);
        ivAvatar = findViewById(R.id.detail_avatar);

        // 显示帖子内容
        if (post != null) {
            tvName.setText(post.userName);
            tvContent.setText(post.content);
            // 这里可以加 Glide 加载头像
            // Glide.with(this).load(post.userAvatar).into(ivAvatar);

            // 动态添加媒体视图
            if (post.mediaList != null && !post.mediaList.isEmpty()) {
                if (post.type == 2) { // 视频
                    VideoView videoView = new VideoView(this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 600);
                    videoView.setLayoutParams(params);
                    videoView.setVideoPath(post.mediaList.get(0).url);
                    mediaContainer.addView(videoView);
                    videoView.start(); // 自动播放
                    videoView.setOnClickListener(v -> {
                        if (videoView.isPlaying()) videoView.pause();
                        else videoView.start();
                    });
                } else { // 图片
                    for (PostMedia media : post.mediaList) {
                        ImageView imageView = new ImageView(this);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.bottomMargin = 20;
                        imageView.setLayoutParams(params);
                        imageView.setAdjustViewBounds(true);
                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        mediaContainer.addView(imageView);
                        Glide.with(this).load(media.url).into(imageView);
                    }
                }
            }
        }

        ivBack.setOnClickListener(v -> finish());

        // 初始化 Adapter
        adapter = new CommentAdapter(this, commentList);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(adapter);

        // 发送评论
        btnSend.setOnClickListener(v -> {
            String content = etComment.getText().toString();
            if (!TextUtils.isEmpty(content)) {
                sendComment(content);
            } else {
                Toast.makeText(this, "请输入评论内容", Toast.LENGTH_SHORT).show();
            }
        });

        loadComments();
    }

    private void loadComments() {
        if (post == null) return;
        new Thread(() -> {
            List<Comment> comments = AppDatabase.getInstance(this).postDao().getCommentsForPost(post.id);
            runOnUiThread(() -> {
                commentList.clear();
                commentList.addAll(comments);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void sendComment(String content) {
        if (post == null) return;
        new Thread(() -> {
            Comment comment = new Comment();
            comment.postId = post.id;
            comment.userId = 1; // 假定当前用户ID
            comment.userName = "我";
            comment.content = content;
            comment.createTime = System.currentTimeMillis();

            AppDatabase.getInstance(this).postDao().insertComment(comment);

            runOnUiThread(() -> {
                etComment.setText("");
                Toast.makeText(this, "评论成功", Toast.LENGTH_SHORT).show();
                loadComments();
            });
        }).start();
    }
}
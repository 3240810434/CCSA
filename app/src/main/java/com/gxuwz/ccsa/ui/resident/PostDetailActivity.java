package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.CommentAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Comment;
import com.gxuwz.ccsa.model.Post;
import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {
    private Post post;
    private RecyclerView rvComments;
    private CommentAdapter adapter;
    private List<Comment> commentList = new ArrayList<>();
    private EditText etComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail_custom); // 注意布局文件名

        post = (Post) getIntent().getSerializableExtra("post");

        rvComments = findViewById(R.id.rv_comments);
        etComment = findViewById(R.id.et_comment);

        // 初始化Adapter
        adapter = new CommentAdapter(this, commentList);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(adapter);

        // 发送评论
        findViewById(R.id.btn_send).setOnClickListener(v -> {
            String content = etComment.getText().toString();
            if (!TextUtils.isEmpty(content)) {
                sendComment(content);
            }
        });

        loadComments();
    }

    private void loadComments() {
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
        new Thread(() -> {
            Comment comment = new Comment();
            comment.postId = post.id;
            comment.userId = 1; // 当前用户
            comment.userName = "我";
            comment.content = content;
            comment.createTime = System.currentTimeMillis();

            AppDatabase.getInstance(this).postDao().insertComment(comment);

            runOnUiThread(() -> {
                etComment.setText("");
                loadComments();
            });
        }).start();
    }
}

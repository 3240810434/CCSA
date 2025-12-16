package com.gxuwz.ccsa.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.PostAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Notification;
import com.gxuwz.ccsa.model.Post;
import com.gxuwz.ccsa.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminLifeDynamicsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private PostAdapter adapter;
    private List<Post> postList = new ArrayList<>();
    private AppDatabase db;
    private String community;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_list); // 复用通用列表布局

        community = getIntent().getStringExtra("community");
        if (community == null) {
            Toast.makeText(this, "小区信息丢失", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initView();
        initData();
    }

    private void initView() {
        // 设置标题
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("生活动态管理");

        // 返回按钮
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 初始化Adapter，传入null作为currentUser，表示管理员视角
        adapter = new PostAdapter(this, postList, null);

        // 设置删除监听器
        adapter.setDeleteListener(post -> showDeleteConfirmDialog(post));

        recyclerView.setAdapter(adapter);
    }

    private void initData() {
        db = AppDatabase.getInstance(this);
        loadPosts();
    }

    private void loadPosts() {
        // 使用新加的方法，查询本小区的动态
        List<Post> list = db.postDao().getPostsByCommunity(community);
        postList.clear();
        if (list != null && !list.isEmpty()) {
            // 加载媒体资源
            for (Post post : list) {
                post.mediaList = db.postDao().getMediaForPost(post.id);
                post.commentCount = db.postDao().getCommentCount(post.id);
            }
            postList.addAll(list);
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    private void showDeleteConfirmDialog(Post post) {
        new AlertDialog.Builder(this)
                .setTitle("违规处理")
                .setMessage("确定要删除该动态吗？删除后将自动通知该居民。")
                .setPositiveButton("删除", (dialog, which) -> deletePost(post))
                .setNegativeButton("取消", null)
                .show();
    }

    private void deletePost(Post post) {
        // 1. 删除媒体文件
        db.postDao().deletePostMedia(post.id);
        // 2. 删除评论
        db.postDao().deletePostComments(post.id);
        // 3. 删除帖子本体
        db.postDao().deletePost(post);

        // 4. 发送违规通知
        sendViolationNotification(post.userId, "生活动态");

        Toast.makeText(this, "已删除并通知该居民", Toast.LENGTH_SHORT).show();
        loadPosts(); // 刷新列表
    }

    private void sendViolationNotification(int userId, String postType) {
        User user = db.userDao().getUserById(userId);
        if (user != null) {
            Notification notification = new Notification(
                    0, // adminNoticeId (0 for system generated)
                    community,
                    user.getPhone(),
                    "违规内容处理通知",
                    "尊敬的居民，您发布的“" + postType + "”内容因违反小区社区公约，已被管理员移除。请共同维护良好的社区环境。",
                    3, // 类型3：系统/违规通知
                    null,
                    "系统管理员",
                    new Date(),
                    false
            );
            db.notificationDao().insert(notification);
        }
    }
}
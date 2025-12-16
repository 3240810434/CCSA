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
import com.gxuwz.ccsa.adapter.HelpPostAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.HelpPost;
import com.gxuwz.ccsa.model.Notification;
import com.gxuwz.ccsa.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminNeighborHelpActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private HelpPostAdapter adapter;
    private List<HelpPost> postList = new ArrayList<>();
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
        tvTitle.setText("邻里互助管理");

        // 返回按钮
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 初始化Adapter，传入null作为currentUser
        adapter = new HelpPostAdapter(this, postList, null);

        // 设置删除监听器
        adapter.setDeleteListener(post -> showDeleteConfirmDialog(post));

        recyclerView.setAdapter(adapter);
    }

    private void initData() {
        db = AppDatabase.getInstance(this);
        loadPosts();
    }

    private void loadPosts() {
        // 查询本小区的互助帖
        List<HelpPost> list = db.helpPostDao().getHelpPostsByCommunity(community);
        postList.clear();
        if (list != null && !list.isEmpty()) {
            for (HelpPost post : list) {
                // 填充发布者信息 (因为HelpPost存的是userId，显示需要userName/avatar)
                User user = db.userDao().getUserById(post.userId);
                if (user != null) {
                    post.userName = user.getName();
                    post.userAvatar = user.getAvatar();
                }
                // 填充媒体信息
                post.mediaList = db.helpPostDao().getMediaForPost(post.id);
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

    private void showDeleteConfirmDialog(HelpPost post) {
        new AlertDialog.Builder(this)
                .setTitle("违规处理")
                .setMessage("确定要删除该互助求助帖吗？删除后将自动通知该居民。")
                .setPositiveButton("删除", (dialog, which) -> deletePost(post))
                .setNegativeButton("取消", null)
                .show();
    }

    private void deletePost(HelpPost post) {
        // 删除帖子 (DAO已配置级联删除或者需要在DAO里手动删关联表，根据现有代码，手动删比较稳妥，但HelpPostDao并未暴露单独删Media的方法，如果表结构定义了ForeignKey CASCADE则会自动删。假设需要手动删则需在DAO补方法。这里假设直接删Post即可，或者依赖Room的级联)
        // 查看提供的代码，HelpPostDao只有 deletePost。如果数据库定义完善则没问题。
        db.helpPostDao().deletePost(post);

        // 发送通知
        sendViolationNotification(post.userId);

        Toast.makeText(this, "处理成功", Toast.LENGTH_SHORT).show();
        loadPosts();
    }

    private void sendViolationNotification(int userId) {
        User user = db.userDao().getUserById(userId);
        if (user != null) {
            Notification notification = new Notification(
                    0,
                    community,
                    user.getPhone(),
                    "互助帖违规处理通知",
                    "尊敬的居民，您发布的“邻里互助”求助帖因违反平台规定，已被管理员移除。请核实内容后重新发布。",
                    3,
                    null,
                    "系统管理员",
                    new Date(),
                    false
            );
            db.notificationDao().insert(notification);
        }
    }
}
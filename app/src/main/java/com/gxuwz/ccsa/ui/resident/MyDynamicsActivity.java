package com.gxuwz.ccsa.ui.resident;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.PostAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Post;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MyDynamicsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private PostAdapter adapter;
    private List<Post> myPosts = new ArrayList<>();
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_list); // 复用通用的列表布局

        initView();
        loadData();
    }

    private void initView() {
        TextView title = findViewById(R.id.tv_title);
        if(title != null) title.setText("我的动态");

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        currentUser = SharedPreferencesUtil.getUser(this);
        adapter = new PostAdapter(this, myPosts, currentUser);

        // 设置删除监听器
        adapter.setDeleteListener(post -> {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("确定要删除这条动态吗？")
                    .setPositiveButton("删除", (dialog, which) -> deletePost(post))
                    .setNegativeButton("取消", null)
                    .show();
        });

        recyclerView.setAdapter(adapter);
    }

    private void loadData() {
        if (currentUser == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Post> posts = AppDatabase.getInstance(this).postDao().getMyPosts(currentUser.getId());
            // 填充媒体信息
            for (Post post : posts) {
                post.mediaList = AppDatabase.getInstance(this).postDao().getMediaForPost(post.id);
            }

            runOnUiThread(() -> {
                myPosts.clear();
                myPosts.addAll(posts);
                adapter.notifyDataSetChanged();

                if (myPosts.isEmpty() && tvEmpty != null) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("暂无动态");
                    recyclerView.setVisibility(View.GONE);
                } else {
                    if(tvEmpty != null) tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    private void deletePost(Post post) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.postDao().deletePostMedia(post.id);
            db.postDao().deletePostComments(post.id);
            db.postDao().deletePost(post);

            runOnUiThread(() -> {
                Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                myPosts.remove(post);
                adapter.notifyDataSetChanged();
                if (myPosts.isEmpty() && tvEmpty != null) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("暂无动态");
                    recyclerView.setVisibility(View.GONE);
                }
            });
        });
    }
}
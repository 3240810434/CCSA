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
import com.gxuwz.ccsa.adapter.HelpPostAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.HelpPost;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MyHelpActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private HelpPostAdapter adapter;
    private List<HelpPost> myHelpPosts = new ArrayList<>();
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 【关键修改】使用新的通用布局 activity_common_list
        setContentView(R.layout.activity_common_list);

        initView();
        loadData();
    }

    private void initView() {
        // 设置标题
        TextView title = findViewById(R.id.tv_title);
        if (title != null) title.setText("我的互助");

        // 返回按钮
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        currentUser = SharedPreferencesUtil.getUser(this);
        adapter = new HelpPostAdapter(this, myHelpPosts, currentUser);

        // 设置删除监听器
        adapter.setDeleteListener(post -> {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("确定要删除这条求助吗？")
                    .setPositiveButton("删除", (dialog, which) -> deletePost(post))
                    .setNegativeButton("取消", null)
                    .show();
        });

        recyclerView.setAdapter(adapter);
    }

    private void loadData() {
        if (currentUser == null) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            List<HelpPost> posts = AppDatabase.getInstance(this).helpPostDao().getMyHelpPosts(currentUser.getId());

            for (HelpPost post : posts) {
                post.userName = currentUser.getName();
                post.userAvatar = currentUser.getAvatar();
                post.mediaList = AppDatabase.getInstance(this).helpPostDao().getMediaForPost(post.id);
            }

            runOnUiThread(() -> {
                myHelpPosts.clear();
                myHelpPosts.addAll(posts);
                adapter.notifyDataSetChanged();

                if (myHelpPosts.isEmpty() && tvEmpty != null) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("暂无互助贴");
                    recyclerView.setVisibility(View.GONE);
                } else {
                    if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    private void deletePost(HelpPost post) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).helpPostDao().deletePost(post);

            runOnUiThread(() -> {
                Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                myHelpPosts.remove(post);
                adapter.notifyDataSetChanged();

                if (myHelpPosts.isEmpty() && tvEmpty != null) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("暂无互助贴");
                    recyclerView.setVisibility(View.GONE);
                }
            });
        });
    }
}
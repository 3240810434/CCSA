package com.gxuwz.ccsa.ui.resident;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.HelpPostAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.HelpPost;
import com.gxuwz.ccsa.model.User;
import java.util.ArrayList;
import java.util.List;

public class NeighborHelpFragment extends Fragment {

    private RecyclerView recyclerView;
    private ImageView ivAddPost; // 发布按钮
    private HelpPostAdapter adapter;
    private List<HelpPost> postList = new ArrayList<>();
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_neighbor_help, container, false);

        // 获取当前登录用户
        if (getActivity() instanceof ResidentMainActivity) {
            currentUser = ((ResidentMainActivity) getActivity()).getUser();
        }

        initView(view);
        return view;
    }

    private void initView(View view) {
        recyclerView = view.findViewById(R.id.rv_help_posts); // 请确保xml里ID对应
        ivAddPost = view.findViewById(R.id.iv_add_post); // 假设xml有个悬浮按钮或图标用于发布

        // 初始化 RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HelpPostAdapter(getContext(), postList, currentUser);
        recyclerView.setAdapter(adapter);

        // 点击跳转到发布页面
        if (ivAddPost != null) {
            ivAddPost.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), HelpPostEditActivity.class);
                intent.putExtra("user", currentUser);
                startActivity(intent);
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次页面可见时（包括发布回来、修改个人资料回来）都刷新数据
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());

            // 1. 获取所有帖子
            List<HelpPost> posts = db.helpPostDao().getAllHelpPosts();

            // 2. 【核心修复】遍历帖子，手动查询并填充用户信息
            // 解释：因为 userName/avatar 是 @Ignore 字段，数据库不存，必须手动填
            for (HelpPost post : posts) {
                // 根据帖子的 userId 去 User 表里查最新的用户信息
                User author = db.userDao().getUserById(post.userId);
                if (author != null) {
                    post.userName = author.getName();   // 填充最新名字
                    post.userAvatar = author.getAvatar(); // 填充最新头像
                }

                // 顺便加载媒体信息（如果之前没加载的话）
                post.mediaList = db.helpPostDao().getMediaForPost(post.id);
            }

            // 3. 切换回主线程更新 UI
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    postList = posts;
                    // 更新适配器数据
                    if (adapter != null) {
                        adapter.setList(postList);
                    } else {
                        adapter = new HelpPostAdapter(getContext(), postList, currentUser);
                        recyclerView.setAdapter(adapter);
                    }
                });
            }
        }).start();
    }
}
package com.gxuwz.ccsa.ui.resident;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.PostAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Post;
import com.gxuwz.ccsa.model.User;
import java.util.ArrayList;
import java.util.List;

public class LifeDynamicsFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> postList = new ArrayList<>();
    private FloatingActionButton fabAdd;
    private ImageView btnRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_life_dynamics, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        fabAdd = view.findViewById(R.id.fab_add);
        btnRefresh = view.findViewById(R.id.btn_refresh);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostAdapter(getContext(), postList);
        recyclerView.setAdapter(adapter);

        // 点击右下角发布
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MediaSelectActivity.class);
            if (getActivity() instanceof ResidentMainActivity) {
                User currentUser = ((ResidentMainActivity) getActivity()).getUser();
                intent.putExtra("user", currentUser);
            }
            startActivity(intent);
        });

        // 3. 刷新按钮逻辑
        btnRefresh.setOnClickListener(v -> {
            // 顺时针旋转一圈动画
            ObjectAnimator rotate = ObjectAnimator.ofFloat(btnRefresh, "rotation", 0f, 360f);
            rotate.setDuration(800);
            rotate.setInterpolator(new LinearInterpolator());
            rotate.start();

            // 刷新页面并滚动到顶部
            loadPosts();
            recyclerView.smoothScrollToPosition(0);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPosts();
    }

    private void loadPosts() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            // 获取帖子
            List<Post> posts = db.postDao().getAllPosts();

            // 4. 居民修改信息同步：遍历帖子，查询最新的用户信息覆盖旧数据
            for (Post p : posts) {
                // 获取媒体和评论数
                p.mediaList = db.postDao().getMediaForPost(p.id);
                p.commentCount = db.postDao().getCommentCount(p.id);

                // 【关键】根据userId查最新的User信息
                User latestUser = db.userDao().getUserById(p.userId);
                if (latestUser != null) {
                    p.userName = latestUser.nickname; // 使用最新昵称
                    p.userAvatar = latestUser.avatar; // 使用最新头像
                }
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    postList.clear();
                    postList.addAll(posts);
                    adapter.notifyDataSetChanged();
                });
            }
        }).start();
    }
}
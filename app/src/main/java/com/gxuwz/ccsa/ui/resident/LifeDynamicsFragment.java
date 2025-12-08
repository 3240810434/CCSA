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
// import com.google.android.material.floatingactionbutton.FloatingActionButton; // 删除或注释掉这行
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

    // 修改点 1: 将类型从 FloatingActionButton 改为 ImageView
    private ImageView fabAdd;

    private ImageView btnRefresh;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_life_dynamics, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);

        // 修改点 2: 这里 findViewById 获取到的是 ImageView，现在类型匹配了，不会再报错
        fabAdd = view.findViewById(R.id.fab_add);

        btnRefresh = view.findViewById(R.id.btn_refresh);

        // 获取当前登录用户
        if (getActivity() instanceof ResidentMainActivity) {
            currentUser = ((ResidentMainActivity) getActivity()).getUser();
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // 将 currentUser 传递给 Adapter
        adapter = new PostAdapter(getContext(), postList, currentUser);
        recyclerView.setAdapter(adapter);

        // 点击发布
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), MediaSelectActivity.class);
                if (currentUser != null) {
                    intent.putExtra("user", currentUser);
                }
                startActivity(intent);
            });
        }

        // 刷新按钮逻辑：顺时针旋转一圈并刷新
        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> {
                ObjectAnimator rotate = ObjectAnimator.ofFloat(btnRefresh, "rotation", 0f, 360f);
                rotate.setDuration(800);
                rotate.setInterpolator(new LinearInterpolator());
                rotate.start();

                loadPosts();
                recyclerView.smoothScrollToPosition(0);
            });
        }

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
            // 确保 PostDao 中存在 getAllPosts 方法
            List<Post> posts = db.postDao().getAllPosts();

            // 居民修改信息同步：遍历帖子，查询最新的用户信息覆盖旧数据
            for (Post p : posts) {
                p.mediaList = db.postDao().getMediaForPost(p.id);
                p.commentCount = db.postDao().getCommentCount(p.id);

                User latestUser = db.userDao().getUserById(p.userId);
                if (latestUser != null) {
                    p.userName = latestUser.getName();
                    p.userAvatar = latestUser.getAvatar();
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
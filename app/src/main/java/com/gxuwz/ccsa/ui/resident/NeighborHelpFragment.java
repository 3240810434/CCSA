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
    private HelpPostAdapter adapter;
    private List<HelpPost> postList = new ArrayList<>();
    // 修改变量名以匹配 XML 含义，或者保持变量名但修正 R.id
    private ImageView ivPublish;
    private User currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_neighbor_help, container, false);

        // 获取当前用户
        if (getActivity() instanceof ResidentMainActivity) {
            currentUser = ((ResidentMainActivity) getActivity()).getUser();
        }

        // 【核心修复点】：这里必须使用 XML 文件中定义的 ID
        // XML 中是 android:id="@+id/recycler_view"
        recyclerView = view.findViewById(R.id.recycler_view);

        // XML 中是 android:id="@+id/iv_publish"
        ivPublish = view.findViewById(R.id.iv_publish);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HelpPostAdapter(getContext(), postList, currentUser);
        recyclerView.setAdapter(adapter);

        // 跳转发布页面
        if (ivPublish != null) {
            ivPublish.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), HelpPostEditActivity.class);
                intent.putExtra("user", currentUser);
                startActivity(intent);
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次页面可见时刷新数据（解决修改头像后不同步的问题）
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            // 1. 获取所有帖子
            List<HelpPost> posts = db.helpPostDao().getAllHelpPosts();

            // 2. 遍历帖子，手动查询并填充用户信息（解决名字显示“未知用户”问题）
            for (HelpPost p : posts) {
                // 加载媒体
                p.mediaList = db.helpPostDao().getMediaForPost(p.id);

                // 加载用户信息
                User u = db.userDao().getUserById(p.userId); // 需要确保 UserDao 有 getUserById 方法
                if (u != null) {
                    p.userName = u.getName();
                    p.userAvatar = u.getAvatar();
                } else {
                    p.userName = "未知用户";
                }
            }

            // 3. 切换回主线程更新 UI
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    postList.clear();
                    postList.addAll(posts);
                    // 刷新适配器
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }
}
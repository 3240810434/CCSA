package com.gxuwz.ccsa.ui.resident;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    private ImageView ivPublish;
    private User currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_neighbor_help, container, false);

        // 初始化时获取用户
        updateCurrentUser();

        recyclerView = view.findViewById(R.id.recycler_view);
        ivPublish = view.findViewById(R.id.iv_publish);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HelpPostAdapter(getContext(), postList, currentUser);
        recyclerView.setAdapter(adapter);

        if (ivPublish != null) {
            ivPublish.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), HelpPostEditActivity.class);
                intent.putExtra("user", currentUser); // 传递最新的 User
                startActivity(intent);
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次恢复显示时刷新数据和用户信息
        updateCurrentUser();
        loadData();
    }

    // 【关键修复】处理 Fragment 隐藏/显示时的刷新（解决 Tab 切换不走 onResume 的问题）
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            updateCurrentUser();
            loadData();
        }
    }

    // 更新当前用户信息，并同步给 Adapter
    private void updateCurrentUser() {
        if (getActivity() instanceof ResidentMainActivity) {
            currentUser = ((ResidentMainActivity) getActivity()).getUser();
            if (adapter != null) {
                adapter.setCurrentUser(currentUser);
            }
        }
    }

    private void loadData() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            List<HelpPost> posts = db.helpPostDao().getAllHelpPosts();

            for (HelpPost p : posts) {
                // 加载媒体
                p.mediaList = db.helpPostDao().getMediaForPost(p.id);

                // 【关键】：每次都重新根据 userId 查询最新的用户信息（头像、昵称）
                // 这样即使用户修改了资料，帖子列表也会同步更新
                User u = db.userDao().getUserById(p.userId);
                if (u != null) {
                    p.userName = u.getName();
                    p.userAvatar = u.getAvatar();
                } else {
                    p.userName = "未知用户";
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
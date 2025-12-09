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
    private ImageView ivPublish; // 右上角发布按钮
    private User currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_neighbor_help, container, false);

        // 获取当前用户
        if (getActivity() instanceof ResidentMainActivity) {
            currentUser = ((ResidentMainActivity) getActivity()).getUser();
        }

        recyclerView = view.findViewById(R.id.recycler_view);
        ivPublish = view.findViewById(R.id.iv_publish); // fa_bu2.png

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HelpPostAdapter(getContext(), postList, currentUser);
        recyclerView.setAdapter(adapter);

        // 跳转发布页面
        ivPublish.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), HelpPostEditActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            List<HelpPost> posts = db.helpPostDao().getAllHelpPosts();

            // 同步用户信息（头像/名字）
            for (HelpPost p : posts) {
                p.mediaList = db.helpPostDao().getMediaForPost(p.id);
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
package com.gxuwz.ccsa.ui.resident;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_life_dynamics, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        fabAdd = view.findViewById(R.id.fab_add);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostAdapter(getContext(), postList);
        recyclerView.setAdapter(adapter);

        // 点击右下角发布
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MediaSelectActivity.class);
            // 获取当前登录用户信息并传递
            if (getActivity() instanceof ResidentMainActivity) {
                User currentUser = ((ResidentMainActivity) getActivity()).getUser();
                intent.putExtra("user", currentUser);
            }
            startActivity(intent);
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
            // 获取帖子并获取关联的媒体和评论数
            List<Post> posts = AppDatabase.getInstance(getContext()).postDao().getAllPosts();
            for (Post p : posts) {
                p.mediaList = AppDatabase.getInstance(getContext()).postDao().getMediaForPost(p.id);
                p.commentCount = AppDatabase.getInstance(getContext()).postDao().getCommentCount(p.id);
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
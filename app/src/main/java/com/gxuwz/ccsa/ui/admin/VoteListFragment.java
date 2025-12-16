package com.gxuwz.ccsa.ui.admin;

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
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.VoteAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Vote;
import com.gxuwz.ccsa.ui.common.VoteDetailActivity;
import java.util.List;
import java.util.concurrent.Executors;

public class VoteListFragment extends Fragment implements VoteAdapter.OnVoteItemClickListener {
    private String community;
    private int status; // 1 or 0
    private boolean isAdmin;
    private RecyclerView recyclerView;
    private VoteAdapter adapter;

    public static VoteListFragment newInstance(String community, int status, boolean isAdmin) {
        VoteListFragment fragment = new VoteListFragment();
        Bundle args = new Bundle();
        args.putString("community", community);
        args.putInt("status", status);
        args.putBoolean("isAdmin", isAdmin);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            community = getArguments().getString("community");
            status = getArguments().getInt("status");
            isAdmin = getArguments().getBoolean("isAdmin");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 确保 R.layout.fragment_common_list 存在且包含 id 为 recycler_view 的 RecyclerView
        View view = inflater.inflate(R.layout.fragment_common_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        // 如果 Context 为空，不执行加载
        if (getContext() == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            // 安全获取 Context，防止 Fragment 已销毁导致 Crash
            if (getContext() == null) return;

            List<Vote> votes = AppDatabase.getInstance(getContext()).voteDao().getVotesByStatus(community, status);

            // 切换回主线程更新 UI
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (adapter == null) {
                        adapter = new VoteAdapter(getContext(), votes, isAdmin, this);
                        recyclerView.setAdapter(adapter);
                    } else {
                        // 如果 adapter 已存在，最好更新数据而不是重建，这里为了简单重建也可以，但要注意
                        adapter = new VoteAdapter(getContext(), votes, isAdmin, this);
                        recyclerView.setAdapter(adapter);
                    }
                });
            }
        });
    }

    @Override
    public void onItemClick(Vote vote) {
        if (isAdmin && vote.getStatus() == 0) {
            // 管理员点击草稿 -> 去编辑
            Intent intent = new Intent(getContext(), CreateVoteActivity.class);
            intent.putExtra("community", community);
            intent.putExtra("adminAccount", "admin");
            intent.putExtra("vote_id", vote.getId());
            startActivity(intent);
        } else {
            // 点击已发布 -> 去详情
            Intent intent = new Intent(getContext(), VoteDetailActivity.class);
            intent.putExtra("vote", vote);
            intent.putExtra("isAdmin", isAdmin);
            startActivity(intent);
        }
    }

    @Override
    public void onDeleteClick(Vote vote) {
        if (getContext() == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(getContext()).voteDao().delete(vote);
            AppDatabase.getInstance(getContext()).voteDao().deleteAllRecords(vote.getId());

            if (getActivity() != null) {
                getActivity().runOnUiThread(this::loadData);
            }
        });
    }
}
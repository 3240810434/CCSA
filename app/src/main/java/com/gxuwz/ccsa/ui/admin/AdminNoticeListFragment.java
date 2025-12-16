package com.gxuwz.ccsa.ui.admin;

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
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.AdminNotice;
import java.util.List;
import java.util.concurrent.Executors;

public class AdminNoticeListFragment extends Fragment {
    private boolean isPublishedList; // true=Published, false=Draft
    private RecyclerView recyclerView;
    private AdminNoticeAdapter adapter;

    public static AdminNoticeListFragment newInstance(boolean isPublished) {
        AdminNoticeListFragment fragment = new AdminNoticeListFragment();
        Bundle args = new Bundle();
        args.putBoolean("is_published", isPublished);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isPublishedList = getArguments().getBoolean("is_published");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_common_list, container, false); // 复用列表布局，只要有个RecyclerView id=recyclerView即可
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminNoticeAdapter(getContext(), !isPublishedList);
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<AdminNotice> list;
            if (isPublishedList) {
                list = AppDatabase.getInstance(getContext()).adminNoticeDao().getPublishedNotices();
            } else {
                list = AppDatabase.getInstance(getContext()).adminNoticeDao().getDraftNotices();
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> adapter.setData(list));
            }
        });
    }
}
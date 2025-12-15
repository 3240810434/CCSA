package com.gxuwz.ccsa.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.gxuwz.ccsa.R;

public class ManageFragment extends Fragment {
    private static final String TAG = "ManageFragment";
    private String community;
    private Button btnResidentList;
    private Button btnInitiateVote;
    private Button btnMerchantManage; // 新增

    public ManageFragment() {}

    public static ManageFragment newInstance(String community) {
        ManageFragment fragment = new ManageFragment();
        Bundle args = new Bundle();
        args.putString("community", community);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            community = getArguments().getString("community");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_manage, container, false);
        initViews(view);
        setupButtonListeners();
        return view;
    }

    private void initViews(View view) {
        btnResidentList = view.findViewById(R.id.btn_resident_list);
        btnInitiateVote = view.findViewById(R.id.btn_initiate_vote);

    }

    private void setupButtonListeners() {
        if (btnResidentList != null) {
            btnResidentList.setOnClickListener(v -> {
                if (checkState()) {
                    Intent intent = new Intent(getActivity(), com.gxuwz.ccsa.ui.admin.ResidentListActivity.class);
                    intent.putExtra("community", community);
                    startActivity(intent);
                }
            });
        }

        // 新增商家管理按钮监听
        if (btnMerchantManage != null) {
            btnMerchantManage.setOnClickListener(v -> {
                if (checkState()) {
                    Intent intent = new Intent(getActivity(), com.gxuwz.ccsa.ui.admin.MerchantListActivity.class);
                    intent.putExtra("community", community);
                    startActivity(intent);
                }
            });
        }

        if (btnInitiateVote != null) {
            btnInitiateVote.setOnClickListener(v -> {
                Toast.makeText(getContext(), "发起投票功能", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private boolean checkState() {
        if (getActivity() == null) {
            return false;
        }
        if (community == null || community.isEmpty()) {
            Toast.makeText(getActivity(), "未获取到小区信息，请重新登录", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
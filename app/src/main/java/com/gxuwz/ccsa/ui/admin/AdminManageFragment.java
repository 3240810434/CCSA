package com.gxuwz.ccsa.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.gxuwz.ccsa.R;

public class AdminManageFragment extends Fragment {

    private static final String TAG = "AdminManageFragment";
    private String mCommunity;
    private String mAdminAccount;

    // 按钮视图
    private View btnSetFeeStandard;
    private View btnPublishFee;
    private View btnViewStatistics;
    private View btnHandleAppeal;

    private View btnPublishNotice;
    private View btnInitiateVote;
    private View btnResidentList;
    private View btnMerchantList; // 商家管理（图标入口）
    private View btnMerchantAudit;

    private View btnResidentRepair;
    private View btnLifeDynamics;
    private View btnNeighborHelp;

    // 【新增】底部的“商家账号管理”按钮
    private View btnMerchantManage;

    public static AdminManageFragment newInstance(String community, String adminAccount) {
        if (community == null || community.isEmpty() || adminAccount == null || adminAccount.isEmpty()) {
            throw new IllegalArgumentException("小区信息和管理员账号不能为空");
        }
        AdminManageFragment fragment = new AdminManageFragment();
        Bundle args = new Bundle();
        args.putString("community", community);
        args.putString("adminAccount", adminAccount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCommunity = getArguments().getString("community");
            mAdminAccount = getArguments().getString("adminAccount");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_manage, container, false);
        bindButtons(view);
        setupButtonListeners();
        return view;
    }

    private void bindButtons(View view) {
        // 物业服务
        btnSetFeeStandard = view.findViewById(R.id.btn_set_fee_standard);
        btnPublishFee = view.findViewById(R.id.btn_publish_fee);
        btnViewStatistics = view.findViewById(R.id.btn_fee_statistics);
        btnHandleAppeal = view.findViewById(R.id.btn_payment_appeal);

        // 社区治理
        btnPublishNotice = view.findViewById(R.id.btn_publish_notice);
        btnInitiateVote = view.findViewById(R.id.btn_initiate_vote);
        btnResidentList = view.findViewById(R.id.btn_resident_list);
        btnMerchantList = view.findViewById(R.id.btn_merchant_list);
        btnMerchantAudit = view.findViewById(R.id.btn_merchant_audit);

        // 便民服务
        btnResidentRepair = view.findViewById(R.id.btn_resident_repair);
        btnLifeDynamics = view.findViewById(R.id.btn_life_dynamics);
        btnNeighborHelp = view.findViewById(R.id.btn_neighbor_help);

    }

    private void setupButtonListeners() {
        // ... (其他按钮监听保持不变) ...

        setListener(btnSetFeeStandard, v -> {
            Intent intent = new Intent(requireActivity(), SetFeeStandardActivity.class);
            intent.putExtra("community", mCommunity);
            startActivity(intent);
        });

        setListener(btnPublishFee, v -> {
            Intent intent = new Intent(requireActivity(), FeeAnnouncementActivity.class);
            intent.putExtra("community", mCommunity);
            intent.putExtra("adminAccount", mAdminAccount);
            startActivity(intent);
        });

        setListener(btnViewStatistics, v -> {
            Intent intent = new Intent(requireActivity(), PaymentStatisticsActivity.class);
            intent.putExtra("community", mCommunity);
            startActivity(intent);
        });

        setListener(btnHandleAppeal, v -> {
            Intent intent = new Intent(requireActivity(), PaymentAppealListActivity.class);
            intent.putExtra("community", mCommunity);
            intent.putExtra("adminAccount", mAdminAccount);
            startActivity(intent);
        });

        setListener(btnInitiateVote, v -> {
            Intent intent = new Intent(requireActivity(), CreateVoteActivity.class);
            intent.putExtra("community", mCommunity);
            startActivity(intent);
        });

        setListener(btnResidentList, v -> {
            Intent intent = new Intent(requireActivity(), ResidentListActivity.class);
            intent.putExtra("community", mCommunity);
            startActivity(intent);
        });

        setListener(btnResidentRepair, v -> {
            Intent intent = new Intent(requireActivity(), AdminRepairListActivity.class);
            intent.putExtra("community", mCommunity);
            intent.putExtra("adminAccount", mAdminAccount);
            startActivity(intent);
        });

        setListener(btnMerchantAudit, v -> {
            Intent intent = new Intent(requireActivity(), MerchantAuditListActivity.class);
            startActivity(intent);
        });

        // 【修复】商家管理（图标入口）：跳转到商家列表
        setListener(btnMerchantList, v -> {
            Intent intent = new Intent(requireActivity(), MerchantListActivity.class);
            intent.putExtra("community", mCommunity); // 传递小区信息
            startActivity(intent);
        });

        // 【修复】商家账号管理（底部大按钮）：跳转到商家列表
        setListener(btnMerchantManage, v -> {
            Intent intent = new Intent(requireActivity(), MerchantListActivity.class);
            intent.putExtra("community", mCommunity); // 传递小区信息
            startActivity(intent);
        });

        setListener(btnPublishNotice, v -> showToast("发布通知功能开发中"));
        setListener(btnLifeDynamics, v -> showToast("生活动态功能开发中"));
        setListener(btnNeighborHelp, v -> showToast("邻里互助功能开发中"));
    }

    private void setListener(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(v -> {
                if (checkCommunityValid()) {
                    listener.onClick(v);
                }
            });
        }
    }

    private boolean checkCommunityValid() {
        if (mCommunity == null || mCommunity.trim().isEmpty()) {
            Log.e(TAG, "小区信息为空");
            showToast("未获取有效小区信息，请重新登录");
            return false;
        }
        if (!isAdded()) {
            return false;
        }
        return true;
    }

    private void showToast(String msg) {
        if (getContext() != null) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }
}
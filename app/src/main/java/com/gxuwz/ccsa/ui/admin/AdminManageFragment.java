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
// 确保导入了 MerchantAuditListActivity，如果包名不同请自行调整
// import com.gxuwz.ccsa.ui.admin.MerchantAuditListActivity;

public class AdminManageFragment extends Fragment {

    private static final String TAG = "AdminManageFragment";
    private String mCommunity;
    private String mAdminAccount;

    // 使用 View 而不是 Button，适配自定义的 LinearLayout 布局
    private View btnInitiateVote;
    private View btnSetFeeStandard;
    private View btnPublishFee;
    private View btnViewStatistics;
    private View btnHandleAppeal;
    private View btnResidentList;
    private View btnResidentRepair;

    // 新增：绑定剩余的功能按钮，保证代码完整性
    private View btnPublishNotice;
    private View btnMerchantList;
    private View btnMerchantAudit;
    private View btnLifeDynamics;
    private View btnNeighborHelp;

    // 实例化方法
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
            Log.d(TAG, "Fragment初始化 - 小区：" + mCommunity + "，管理员账号：" + mAdminAccount);
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
        // 1. 设置物业费标准
        setListener(btnSetFeeStandard, v -> {
            Intent intent = new Intent(requireActivity(), SetFeeStandardActivity.class);
            intent.putExtra("community", mCommunity);
            startActivity(intent);
        });

        // 2. 相关费用公示
        setListener(btnPublishFee, v -> {
            Intent intent = new Intent(requireActivity(), FeeAnnouncementActivity.class);
            intent.putExtra("community", mCommunity);
            intent.putExtra("adminAccount", mAdminAccount);
            startActivity(intent);
        });

        // 3. 查看缴费统计
        setListener(btnViewStatistics, v -> {
            Intent intent = new Intent(requireActivity(), PaymentStatisticsActivity.class);
            intent.putExtra("community", mCommunity);
            startActivity(intent);
        });

        // 4. 缴费异常申诉处理
        setListener(btnHandleAppeal, v -> {
            Intent intent = new Intent(requireActivity(), PaymentAppealListActivity.class);
            intent.putExtra("community", mCommunity);
            intent.putExtra("adminAccount", mAdminAccount);
            startActivity(intent);
        });

        // 5. 发起小区投票
        setListener(btnInitiateVote, v -> {
            Intent intent = new Intent(requireActivity(), CreateVoteActivity.class);
            intent.putExtra("community", mCommunity);
            startActivity(intent);
        });

        // 6. 居民列表
        setListener(btnResidentList, v -> {
            Intent intent = new Intent(requireActivity(), ResidentListActivity.class);
            intent.putExtra("community", mCommunity);
            startActivity(intent);
        });

        // 7. 居民报修处理
        setListener(btnResidentRepair, v -> {
            Intent intent = new Intent(requireActivity(), AdminRepairListActivity.class);
            intent.putExtra("community", mCommunity);
            intent.putExtra("adminAccount", mAdminAccount);
            startActivity(intent);
        });

        // --- 新增/修改：商家审核跳转逻辑 ---
        setListener(btnMerchantAudit, v -> {
            Intent intent = new Intent(requireActivity(), MerchantAuditListActivity.class);
            startActivity(intent);
        });

        // --- 以下为原代码中未实现逻辑的按钮，保持提示 ---

        setListener(btnPublishNotice, v -> showToast("发布通知功能开发中"));
        setListener(btnMerchantList, v -> showToast("商家列表功能开发中"));
        // btnMerchantAudit 已上方实现
        setListener(btnLifeDynamics, v -> showToast("生活动态功能开发中"));
        setListener(btnNeighborHelp, v -> showToast("邻里互助功能开发中"));
    }

    // 辅助方法：统一设置监听器并检查小区信息
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
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
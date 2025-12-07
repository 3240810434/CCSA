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

public class AdminManageFragment extends Fragment {

    private static final String TAG = "AdminManageFragment";
    private String mCommunity;
    private String mAdminAccount;
    private Button btnInitiateVote;
    private Button btnSetFeeStandard;
    private Button btnPublishFee;
    private Button btnViewStatistics;
    private Button btnHandleAppeal;
    private Button btnResidentList; // 居民列表按钮
    // 新增：声明居民报修按钮
    private Button btnResidentRepair;

    // 实例化方法：确保外部调用时必须传入小区和管理员账号
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
        } else {
            Log.e(TAG, "onCreate: 未获取到参数（community/adminAccount）");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_manage, container, false);
        bindButtons(view);
        checkButtonBindings();
        setupButtonListeners();
        return view;
    }

    private void bindButtons(View view) {
        btnInitiateVote = view.findViewById(R.id.btn_initiate_vote);
        btnSetFeeStandard = view.findViewById(R.id.btn_set_fee_standard);
        btnPublishFee = view.findViewById(R.id.btn_publish_fee);
        btnViewStatistics = view.findViewById(R.id.btn_fee_statistics);
        btnHandleAppeal = view.findViewById(R.id.btn_payment_appeal);
        btnResidentList = view.findViewById(R.id.btn_resident_list);
        // 新增：绑定居民报修按钮
        btnResidentRepair = view.findViewById(R.id.btn_resident_repair);
    }

    private void checkButtonBindings() {
        if (btnSetFeeStandard == null) Log.e(TAG, "布局中未找到ID为 btn_set_fee_standard 的按钮");
        if (btnPublishFee == null) Log.e(TAG, "布局中未找到ID为 btn_publish_fee 的按钮");
        if (btnViewStatistics == null) Log.e(TAG, "布局中未找到ID为 btn_fee_statistics 的按钮");
        if (btnHandleAppeal == null) Log.e(TAG, "布局中未找到ID为 btn_payment_appeal 的按钮");
        if (btnInitiateVote == null) Log.e(TAG, "布局中未找到ID为 btn_initiate_vote 的按钮");
        if (btnResidentList == null) Log.e(TAG, "布局中未找到ID为 btn_resident_list 的按钮");
        // 新增：检查居民报修按钮
        if (btnResidentRepair == null) {
            Log.e(TAG, "布局中未找到ID为 btn_resident_repair 的按钮");
        }

        // 更新错误判断
        boolean hasError = btnSetFeeStandard == null || btnPublishFee == null ||
                btnViewStatistics == null || btnHandleAppeal == null ||
                btnResidentList == null || btnInitiateVote == null || btnResidentRepair == null;
        if (hasError) {
            Toast.makeText(getContext(), "功能按钮加载失败，请检查布局文件", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupButtonListeners() {
        // 核心修改：设置物业费标准按钮（补充日志，确保参数传递可见）
        btnSetFeeStandard.setOnClickListener(v -> {
            Log.d(TAG, "设置物业费标准按钮点击 - 准备传递的小区：" + mCommunity);
            if (checkCommunityValid()) {
                Intent intent = new Intent(requireActivity(), SetFeeStandardActivity.class);
                intent.putExtra("community", mCommunity); // 关键：传递小区信息
                Log.d(TAG, "成功传递小区信息到 SetFeeStandardActivity：" + mCommunity);
                startActivity(intent);
            } else {
                Log.w(TAG, "设置物业费标准失败：小区信息无效");
            }
        });

        btnPublishFee.setOnClickListener(v -> {
            if (checkCommunityValid()) {
                Intent intent = new Intent(requireActivity(), FeeAnnouncementActivity.class);
                intent.putExtra("community", mCommunity);
                intent.putExtra("adminAccount", mAdminAccount);
                startActivity(intent);
            }
        });

        btnViewStatistics.setOnClickListener(v -> {
            if (checkCommunityValid()) {
                Intent intent = new Intent(requireActivity(), PaymentStatisticsActivity.class);
                intent.putExtra("community", mCommunity);
                startActivity(intent);
            }
        });

        btnHandleAppeal.setOnClickListener(v -> {
            if (checkCommunityValid()) {
                Intent intent = new Intent(requireActivity(), PaymentAppealListActivity.class);
                intent.putExtra("community", mCommunity);
                intent.putExtra("adminAccount", mAdminAccount);
                startActivity(intent);
            }
        });

        btnInitiateVote.setOnClickListener(v -> {
            if (checkCommunityValid()) {
                Intent intent = new Intent(requireActivity(), CreateVoteActivity.class);
                intent.putExtra("community", mCommunity);
                startActivity(intent);
            }
        });

        // 居民列表按钮点击事件
        btnResidentList.setOnClickListener(v -> {
            if (checkCommunityValid()) {
                Intent intent = new Intent(requireActivity(), ResidentListActivity.class);
                intent.putExtra("community", mCommunity);
                startActivity(intent);
            }
        });

        // 新增：居民报修按钮点击事件
        btnResidentRepair.setOnClickListener(v -> {
            if (checkCommunityValid()) {
                Intent intent = new Intent(requireActivity(), AdminRepairListActivity.class);
                // 传递管理员负责的小区（用于筛选该小区的报修）和管理员账号
                intent.putExtra("community", mCommunity);
                intent.putExtra("adminAccount", mAdminAccount);
                startActivity(intent);
            }
        });
    }

    /**
     * 检查小区信息有效性（增强校验逻辑）
     */
    private boolean checkCommunityValid() {
        if (mCommunity == null || mCommunity.trim().isEmpty()) {
            Log.e(TAG, "小区信息为空或空白");
            Toast.makeText(getContext(), "未获取有效小区信息，请重新登录", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isAdded()) { // 检查Fragment是否已附着到Activity
            Log.e(TAG, "Fragment未附着到Activity，无法跳转");
            Toast.makeText(getContext(), "页面状态异常，请重试", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}

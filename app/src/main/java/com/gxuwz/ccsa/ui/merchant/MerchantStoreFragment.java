package com.gxuwz.ccsa.ui.merchant;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Merchant;

import java.util.concurrent.Executors;

public class MerchantStoreFragment extends Fragment {

    private ImageView ivStoreAvatar;
    private TextView tvStoreName;
    private ImageView ivStoreStatus;

    // 功能按钮
    private LinearLayout llPendingOrders;
    private LinearLayout llProcessingOrders;
    private LinearLayout llCompletedOrders;
    private LinearLayout llAfterSales;
    private LinearLayout llProductManagement;

    private Merchant currentMerchant;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_merchant_store, container, false);

        // 从 Activity 获取初始 Merchant 对象
        if (getActivity() instanceof MerchantMainActivity) {
            currentMerchant = ((MerchantMainActivity) getActivity()).getCurrentMerchant();
        }

        initViews(view);
        setupListeners();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次页面可见时，刷新商家信息（同步"我的"页面的修改）
        refreshMerchantData();
    }

    private void initViews(View view) {
        ivStoreAvatar = view.findViewById(R.id.iv_store_avatar);
        tvStoreName = view.findViewById(R.id.tv_store_name);
        ivStoreStatus = view.findViewById(R.id.iv_store_status);

        llPendingOrders = view.findViewById(R.id.ll_pending_orders);
        llProcessingOrders = view.findViewById(R.id.ll_processing_orders);
        llCompletedOrders = view.findViewById(R.id.ll_completed_orders);
        llAfterSales = view.findViewById(R.id.ll_after_sales);
        llProductManagement = view.findViewById(R.id.ll_product_management);
    }

    private void setupListeners() {
        // 店铺开关状态点击事件
        ivStoreStatus.setOnClickListener(v -> showToggleStatusDialog());

        // 功能按钮点击跳转
        llPendingOrders.setOnClickListener(v -> startActivity(new Intent(getContext(), PendingOrdersActivity.class)));
        llProcessingOrders.setOnClickListener(v -> startActivity(new Intent(getContext(), ProcessingOrdersActivity.class)));
        llCompletedOrders.setOnClickListener(v -> startActivity(new Intent(getContext(), CompletedOrdersActivity.class)));
        llAfterSales.setOnClickListener(v -> startActivity(new Intent(getContext(), AfterSalesActivity.class)));
        llProductManagement.setOnClickListener(v -> startActivity(new Intent(getContext(), ProductManagementActivity.class)));
    }

    private void refreshMerchantData() {
        if (currentMerchant == null) return;

        // 异步从数据库获取最新信息
        Executors.newSingleThreadExecutor().execute(() -> {
            Merchant updated = AppDatabase.getInstance(getContext())
                    .merchantDao()
                    .findById(currentMerchant.getId());

            if (updated != null) {
                currentMerchant = updated;
                // 更新 UI
                if (getActivity() != null) {
                    getActivity().runOnUiThread(this::updateUI);
                }
            }
        });
    }

    private void updateUI() {
        if (currentMerchant == null) return;

        // 设置头像
        try {
            if (currentMerchant.getAvatar() != null && !currentMerchant.getAvatar().isEmpty()) {
                ivStoreAvatar.setImageURI(Uri.parse(currentMerchant.getAvatar()));
            } else {
                ivStoreAvatar.setImageResource(R.drawable.merchant_picture);
            }
        } catch (Exception e) {
            ivStoreAvatar.setImageResource(R.drawable.merchant_picture);
        }

        // 设置名称
        tvStoreName.setText(currentMerchant.getMerchantName());

        // 设置开关状态
        if (currentMerchant.isOpen()) {
            ivStoreStatus.setImageResource(R.drawable.open);
        } else {
            ivStoreStatus.setImageResource(R.drawable.close);
        }
    }

    private void showToggleStatusDialog() {
        if (currentMerchant == null) return;

        boolean isOpen = currentMerchant.isOpen();
        String message = isOpen ? "是否关闭店铺？" : "是否开启店铺？";

        new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton("确定", (dialog, which) -> toggleStoreStatus(!isOpen))
                .setNegativeButton("取消", null)
                .show();
    }

    private void toggleStoreStatus(boolean newStatus) {
        currentMerchant.setOpen(newStatus);

        // 更新 UI
        updateUI();

        // 保存到数据库
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(getContext()).merchantDao().update(currentMerchant);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    String statusMsg = newStatus ? "店铺已开启" : "店铺已关闭";
                    Toast.makeText(getContext(), statusMsg, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
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

        if (getActivity() instanceof MerchantMainActivity) {
            currentMerchant = ((MerchantMainActivity) getActivity()).getCurrentMerchant();
        }

        initViews(view);
        setupListeners();
        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        syncDataFromActivity();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            syncDataFromActivity();
        }
    }

    private void syncDataFromActivity() {
        if (getActivity() instanceof MerchantMainActivity) {
            Merchant activityMerchant = ((MerchantMainActivity) getActivity()).getCurrentMerchant();
            if (activityMerchant != null) {
                this.currentMerchant = activityMerchant;
                updateUI();
            }
        }
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
        if (ivStoreStatus != null) {
            ivStoreStatus.setOnClickListener(v -> showToggleStatusDialog());
        }

        llPendingOrders.setOnClickListener(v -> startActivity(new Intent(getContext(), PendingOrdersActivity.class)));
        llProcessingOrders.setOnClickListener(v -> startActivity(new Intent(getContext(), ProcessingOrdersActivity.class)));
        llCompletedOrders.setOnClickListener(v -> startActivity(new Intent(getContext(), CompletedOrdersActivity.class)));

        // 【核心修改】这里改为跳转到 MerchantAfterSalesListActivity，才能看到列表
        llAfterSales.setOnClickListener(v -> startActivity(new Intent(getContext(), MerchantAfterSalesListActivity.class)));

        llProductManagement.setOnClickListener(v -> startActivity(new Intent(getContext(), ProductManagementActivity.class)));
    }

    private void updateUI() {
        if (currentMerchant == null) return;
        if (ivStoreAvatar == null || tvStoreName == null) return;

        try {
            if (currentMerchant.getAvatar() != null && !currentMerchant.getAvatar().isEmpty()) {
                ivStoreAvatar.setImageURI(Uri.parse(currentMerchant.getAvatar()));
            } else {
                ivStoreAvatar.setImageResource(R.drawable.merchant_picture);
            }
        } catch (Exception e) {
            ivStoreAvatar.setImageResource(R.drawable.merchant_picture);
        }

        tvStoreName.setText(currentMerchant.getMerchantName());

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
        updateUI();

        Executors.newSingleThreadExecutor().execute(() -> {
            if (getContext() != null) {
                AppDatabase.getInstance(getContext()).merchantDao().update(currentMerchant);
                if (getActivity() instanceof MerchantMainActivity) {
                    ((MerchantMainActivity) getActivity()).setCurrentMerchant(currentMerchant);
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        String statusMsg = newStatus ? "店铺已开启" : "店铺已关闭";
                        Toast.makeText(getContext(), statusMsg, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}
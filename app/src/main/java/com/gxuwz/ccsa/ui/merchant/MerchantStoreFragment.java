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
import com.gxuwz.ccsa.db.OrderDao;
import com.gxuwz.ccsa.db.ProductDao;
import com.gxuwz.ccsa.model.Merchant;
import com.gxuwz.ccsa.model.Order;

import java.util.List;
import java.util.concurrent.Executors;

public class MerchantStoreFragment extends Fragment {

    private ImageView ivStoreAvatar;
    private TextView tvStoreName;
    private ImageView ivStoreStatus;

    // 统计数据显示控件
    private TextView tvPendingCount;
    private TextView tvProcessingCount;
    private TextView tvAfterSalesCount;
    private TextView tvProductCount;

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
        // 初始加载统计数据
        refreshDashboardCounts();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        syncDataFromActivity();
        // 每次界面恢复显示时（例如从子页面返回），刷新统计数据
        refreshDashboardCounts();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            syncDataFromActivity();
            refreshDashboardCounts();
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

        // 初始化统计数字的TextView
        tvPendingCount = view.findViewById(R.id.tv_pending_count);
        tvProcessingCount = view.findViewById(R.id.tv_processing_count);
        tvAfterSalesCount = view.findViewById(R.id.tv_after_sales_count);
        tvProductCount = view.findViewById(R.id.tv_product_count);

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

    /**
     * 异步刷新仪表盘上的计数
     */
    private void refreshDashboardCounts() {
        if (currentMerchant == null || getContext() == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getContext());
                OrderDao orderDao = db.orderDao();
                ProductDao productDao = db.productDao();
                String merchantIdStr = String.valueOf(currentMerchant.getId());

                // 1. 获取商品数量
                int productCount = productDao.getProductsByMerchantId(currentMerchant.getId()).size();

                // 2. 获取待接单数量
                int pendingOrderCount = orderDao.getPendingOrdersByMerchant(merchantIdStr).size();

                // 3. 获取接单中数量
                int processingOrderCount = orderDao.getOrdersByMerchantAndStatus(merchantIdStr, "接单中").size();

                // 4. 获取待处理的售后订单数量
                List<Order> afterSalesOrders = orderDao.getMerchantAfterSalesOrders(merchantIdStr);
                int pendingAfterSalesCount = 0;
                for (Order order : afterSalesOrders) {
                    // 【修复点】直接访问 public 字段 afterSalesStatus，而不是调用 getAfterSalesStatus()
                    if (order.afterSalesStatus == 1) {
                        pendingAfterSalesCount++;
                    }
                }

                // 切换到主线程更新UI
                int finalProductCount = productCount;
                int finalPendingOrderCount = pendingOrderCount;
                int finalProcessingOrderCount = processingOrderCount;
                int finalPendingAfterSalesCount = pendingAfterSalesCount;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (tvProductCount != null) tvProductCount.setText(String.valueOf(finalProductCount));
                        if (tvPendingCount != null) tvPendingCount.setText(String.valueOf(finalPendingOrderCount));
                        if (tvProcessingCount != null) tvProcessingCount.setText(String.valueOf(finalProcessingOrderCount));
                        if (tvAfterSalesCount != null) tvAfterSalesCount.setText(String.valueOf(finalPendingAfterSalesCount));
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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
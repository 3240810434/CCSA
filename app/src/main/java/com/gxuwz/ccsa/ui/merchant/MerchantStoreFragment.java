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
import androidx.core.content.ContextCompat;
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

    // 状态相关控件
    private ImageView ivStoreStatus;
    private TextView tvStatusText;
    private LinearLayout llStatusContainer;

    // 统计数据显示控件
    private TextView tvPendingCount;
    private TextView tvProcessingCount; // 关键：接单中数量控件
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
        refreshDashboardCounts(); // 初始化时刷新数据

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        syncDataFromActivity();
        refreshDashboardCounts(); // 页面可见时刷新数据
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

        // 状态相关
        ivStoreStatus = view.findViewById(R.id.iv_store_status);
        tvStatusText = view.findViewById(R.id.tv_status_text);
        llStatusContainer = view.findViewById(R.id.ll_status_container);

        // 统计数据控件绑定
        tvPendingCount = view.findViewById(R.id.tv_pending_count);
        tvProcessingCount = view.findViewById(R.id.tv_processing_count); // 绑定到 XML 中的 tv_processing_count
        tvAfterSalesCount = view.findViewById(R.id.tv_after_sales_count);
        tvProductCount = view.findViewById(R.id.tv_product_count);

        // 功能入口
        llPendingOrders = view.findViewById(R.id.ll_pending_orders);
        llProcessingOrders = view.findViewById(R.id.ll_processing_orders);
        llCompletedOrders = view.findViewById(R.id.ll_completed_orders);
        llAfterSales = view.findViewById(R.id.ll_after_sales);
        llProductManagement = view.findViewById(R.id.ll_product_management);
    }

    private boolean checkQualification() {
        if (currentMerchant == null) return false;
        if (currentMerchant.getQualificationStatus() != 2) {
            Toast.makeText(getContext(), "您尚未通过商家资质认证，暂时无法使用此功能。请前往【我的-商家资质】进行认证。", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void setupListeners() {
        if (llStatusContainer != null) {
            llStatusContainer.setOnClickListener(v -> {
                if (checkQualification()) {
                    showToggleStatusDialog();
                }
            });
        }
        if (ivStoreStatus != null) {
            ivStoreStatus.setOnClickListener(v -> {
                if (checkQualification()) {
                    showToggleStatusDialog();
                }
            });
        }

        llPendingOrders.setOnClickListener(v -> {
            if (checkQualification()) {
                startActivity(new Intent(getContext(), PendingOrdersActivity.class));
            }
        });

        llProcessingOrders.setOnClickListener(v -> {
            if (checkQualification()) {
                startActivity(new Intent(getContext(), ProcessingOrdersActivity.class));
            }
        });

        llCompletedOrders.setOnClickListener(v -> {
            if (checkQualification()) {
                startActivity(new Intent(getContext(), CompletedOrdersActivity.class));
            }
        });

        llAfterSales.setOnClickListener(v -> {
            if (checkQualification()) {
                startActivity(new Intent(getContext(), MerchantAfterSalesListActivity.class));
            }
        });

        llProductManagement.setOnClickListener(v -> {
            if (checkQualification()) {
                startActivity(new Intent(getContext(), ProductManagementActivity.class));
            }
        });
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

        if (getContext() != null) {
            if (currentMerchant.isOpen()) {
                ivStoreStatus.setImageResource(R.drawable.open);
                tvStatusText.setText("营业中");
                tvStatusText.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_dark));
            } else {
                ivStoreStatus.setImageResource(R.drawable.close);
                tvStatusText.setText("休息中");
                tvStatusText.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
            }
        }
    }

    // 关键方法：查询数据库并更新UI上的数字
    private void refreshDashboardCounts() {
        if (currentMerchant == null || getContext() == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getContext());
                OrderDao orderDao = db.orderDao();
                ProductDao productDao = db.productDao();
                String merchantIdStr = String.valueOf(currentMerchant.getId());

                // 1. 商品数量
                int productCount = productDao.getProductsByMerchantId(currentMerchant.getId()).size();

                // 2. 待接单数量
                int pendingOrderCount = orderDao.getPendingOrdersByMerchant(merchantIdStr).size();

                // 3. 修复点：统计“接单中”状态的订单数量
                // 确保这里的状态字符串 "接单中" 与您数据库中 Order 表的 status 字段值完全一致
                int processingOrderCount = orderDao.getOrdersByMerchantAndStatus(merchantIdStr, "接单中").size();

                // 4. 售后数量
                List<Order> afterSalesOrders = orderDao.getMerchantAfterSalesOrders(merchantIdStr);
                int pendingAfterSalesCount = 0;
                for (Order order : afterSalesOrders) {
                    if (order.afterSalesStatus == 1) { // 1 代表申请中
                        pendingAfterSalesCount++;
                    }
                }

                int finalProductCount = productCount;
                int finalPendingOrderCount = pendingOrderCount;
                int finalProcessingOrderCount = processingOrderCount;
                int finalPendingAfterSalesCount = pendingAfterSalesCount;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (tvProductCount != null) tvProductCount.setText(String.valueOf(finalProductCount));
                        if (tvPendingCount != null) tvPendingCount.setText(String.valueOf(finalPendingOrderCount));
                        // 更新 UI 显示
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
        String title = isOpen ? "暂停营业" : "开始营业";
        String message = isOpen ?
                "确定要关闭店铺吗？\n关闭后，居民将无法看到您的商品，但您仍可处理进行中的订单。" :
                "确定要开启店铺吗？\n开启后，您的商品将重新上架，居民可以进行购买。";

        new AlertDialog.Builder(getContext())
                .setTitle(title)
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
                        String statusMsg = newStatus ? "店铺已开启，祝您生意兴隆！" : "店铺已关闭，注意休息！";
                        Toast.makeText(getContext(), statusMsg, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}
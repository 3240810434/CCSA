package com.gxuwz.ccsa.ui.merchant;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Executors;

public class MerchantRevenueActivity extends AppCompatActivity {

    private TextView tvTotalRevenue;
    private TextView tvOrderCount;
    private long merchantId;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_revenue);

        initViews();

        // 保持与 MerchantProfileFragment 一致的 SharedPreferences 读取方式
        merchantId = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getLong("merchant_id", -1);

        if (merchantId == -1) {
            Toast.makeText(this, "用户状态异常，请重新登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = AppDatabase.getInstance(this);
        calculateRevenue();
    }

    private void initViews() {
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        tvOrderCount = findViewById(R.id.tv_order_count);

        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void calculateRevenue() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // 1. 查询所有已完成的订单
            // OrderDao 中的 getOrdersByMerchantAndStatus 需要 String 类型的 merchantId
            List<Order> completedOrders = db.orderDao().getOrdersByMerchantAndStatus(
                    String.valueOf(merchantId),
                    "已完成"
            );

            // 2. 累加金额
            BigDecimal totalAmount = new BigDecimal("0.00");
            int count = 0;

            if (completedOrders != null) {
                count = completedOrders.size();
                for (Order order : completedOrders) {
                    if (order.payAmount != null && !order.payAmount.isEmpty()) {
                        try {
                            // 移除可能存在的 "¥" 符号、中文“元”或空格，确保能转为数字
                            String cleanAmount = order.payAmount
                                    .replace("¥", "")
                                    .replace("元", "")
                                    .trim();

                            if (!cleanAmount.isEmpty()) {
                                BigDecimal amount = new BigDecimal(cleanAmount);
                                totalAmount = totalAmount.add(amount);
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            // 如果金额格式错误，跳过该订单
                        }
                    }
                }
            }

            // 3. 更新 UI
            BigDecimal finalTotalAmount = totalAmount;
            int finalCount = count;
            runOnUiThread(() -> {
                // 保留两位小数
                tvTotalRevenue.setText(String.format("%.2f", finalTotalAmount));
                tvOrderCount.setText("累计完成订单：" + finalCount + " 单");
            });
        });
    }
}
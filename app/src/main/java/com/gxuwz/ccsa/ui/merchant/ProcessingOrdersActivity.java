package com.gxuwz.ccsa.ui.merchant;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.MerchantProcessingOrderAdapter; // 引入上面的适配器
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Order;
import java.util.List;

public class ProcessingOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppDatabase db;
    private long merchantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing_orders); // 确保你有这个xml布局，内容包含一个RecyclerView

        merchantId = getSharedPreferences("merchant_prefs", MODE_PRIVATE).getLong("merchant_id", -1);
        if (merchantId == -1) {
            Toast.makeText(this, "登录状态失效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = AppDatabase.getInstance(this);
        recyclerView = findViewById(R.id.recycler_view); // 确保布局里 ID 是 recycler_view
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 如果有返回按钮
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 放在 onResume 确保每次回到页面都刷新
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            // 查询状态为 "配送中" 的订单
            List<Order> orders = db.orderDao().getOrdersByMerchantAndStatus(String.valueOf(merchantId), "配送中");

            runOnUiThread(() -> {
                MerchantProcessingOrderAdapter adapter = new MerchantProcessingOrderAdapter(this, orders);

                // 设置“标记完成”的监听回调
                adapter.setOnOrderActionListener(order -> {
                    finishOrder(order);
                });

                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    private void finishOrder(Order order) {
        new Thread(() -> {
            // 1. 修改状态为 "已完成"
            order.status = "已完成";
            db.orderDao().update(order);

            // 2. 刷新界面
            runOnUiThread(() -> {
                Toast.makeText(ProcessingOrdersActivity.this, "订单已完成", Toast.LENGTH_SHORT).show();
                loadData(); // 重新加载，该订单会从列表中消失
            });
        }).start();
    }
}
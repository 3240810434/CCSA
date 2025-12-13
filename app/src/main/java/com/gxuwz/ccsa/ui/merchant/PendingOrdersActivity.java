package com.gxuwz.ccsa.ui.merchant;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.MerchantPendingOrderAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Order;

import java.util.List;

public class PendingOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppDatabase db;
    // 【修改点1】改为 int 类型
    private int merchantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_orders);

        // 【修改点2】使用 getInt 获取 merchant_id
        merchantId = getSharedPreferences("merchant_prefs", MODE_PRIVATE).getInt("merchant_id", -1);

        // 校验登录状态
        if (merchantId == -1) {
            Toast.makeText(this, "登录状态异常，请重新登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = AppDatabase.getInstance(this);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            // 根据 merchantId 查询状态为“待接单”的订单
            List<Order> orders = db.orderDao().getPendingOrdersByMerchant(String.valueOf(merchantId));

            runOnUiThread(() -> {
                // 如果需要空数据提示，可以在这里处理
                if (orders == null || orders.isEmpty()) {
                    // Toast.makeText(this, "暂无待接单订单", Toast.LENGTH_SHORT).show();
                }

                MerchantPendingOrderAdapter adapter = new MerchantPendingOrderAdapter(orders);

                adapter.setOnOrderActionListener(order -> {
                    new Thread(() -> {
                        order.status = "配送中";
                        db.orderDao().update(order);

                        runOnUiThread(() -> {
                            Toast.makeText(PendingOrdersActivity.this, "已接单，开始配送", Toast.LENGTH_SHORT).show();
                            loadData(); // 重新加载
                        });
                    }).start();
                });

                recyclerView.setAdapter(adapter);
            });
        }).start();
    }
}
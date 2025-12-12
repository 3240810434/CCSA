package com.gxuwz.ccsa.ui.merchant;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.MerchantPendingOrderAdapter; // 需新建
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Order;
import java.util.List;

public class PendingOrdersActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AppDatabase db;
    private long merchantId; // 从登录session获取

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_orders);

        // 假设 merchantId 从 SharedPrefs 获取
        merchantId = getSharedPreferences("merchant_prefs", MODE_PRIVATE).getLong("merchant_id", -1);
        db = AppDatabase.getInstance(this);

        recyclerView = findViewById(R.id.recycler_view); // 确保 layout 中有这个 ID
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 头部返回按钮逻辑
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            // 查询 status = '待接单'
            List<Order> orders = db.orderDao().getPendingOrdersByMerchant(String.valueOf(merchantId));
            runOnUiThread(() -> {
                MerchantPendingOrderAdapter adapter = new MerchantPendingOrderAdapter(orders);
                // 设置接单回调
                adapter.setOnOrderActionListener(order -> {
                    new Thread(() -> {
                        order.status = "配送中"; // 接单后变更为配送中
                        db.orderDao().update(order);
                        loadData(); // 刷新列表
                    }).start();
                });
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }
}
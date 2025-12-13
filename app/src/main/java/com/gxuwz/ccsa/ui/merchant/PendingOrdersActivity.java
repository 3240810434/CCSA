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
    private long merchantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_orders);

        // 1. 从 SharedPreferences 获取商家ID
        merchantId = getSharedPreferences("merchant_prefs", MODE_PRIVATE).getLong("merchant_id", -1);

        // 2. [新增] 校验登录状态
        // 如果获取不到ID（即为默认值-1），提示错误并关闭页面
        if (merchantId == -1) {
            Toast.makeText(this, "登录状态异常，请重新登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化数据库实例
        db = AppDatabase.getInstance(this);

        // 初始化 RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 返回按钮逻辑
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 加载数据
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            // 根据 merchantId 查询状态为“待接单”的订单
            List<Order> orders = db.orderDao().getPendingOrdersByMerchant(String.valueOf(merchantId));

            runOnUiThread(() -> {
                // 3. [可选] 空数据处理逻辑（此处仅做代码结构保留，可视需求开启Toast）
                if (orders == null || orders.isEmpty()) {
                    // Toast.makeText(this, "暂无待接单订单", Toast.LENGTH_SHORT).show();
                }

                MerchantPendingOrderAdapter adapter = new MerchantPendingOrderAdapter(orders);

                // 设置接单按钮点击事件
                adapter.setOnOrderActionListener(order -> {
                    new Thread(() -> {
                        // 更新订单状态
                        order.status = "配送中";
                        db.orderDao().update(order);

                        // 4. [新增] 回到主线程刷新UI并提示用户
                        runOnUiThread(() -> {
                            Toast.makeText(PendingOrdersActivity.this, "已接单，开始配送", Toast.LENGTH_SHORT).show();
                            loadData(); // 重新加载列表以移除已接单的条目
                        });
                    }).start();
                });

                recyclerView.setAdapter(adapter);
            });
        }).start();
    }
}
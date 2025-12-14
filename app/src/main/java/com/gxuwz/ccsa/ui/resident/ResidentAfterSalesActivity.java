package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.ResidentOrderAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Order;
import com.gxuwz.ccsa.util.SharedPreferencesUtil;
import java.util.ArrayList;
import java.util.List;

public class ResidentAfterSalesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ResidentOrderAdapter adapter;
    private AppDatabase db;
    private long userId;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_after_sales);

        initView();

        // 获取当前登录用户ID
        // 假设你用 SharedPreferencesUtil 或者直接 getSharedPreferences
        // 这里沿用你 ResidentCompletedOrdersActivity 的获取方式
        userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getLong("user_id", -1);

        db = AppDatabase.getInstance(this);

        initRecyclerView();
    }

    private void initView() {
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("售后记录");

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        tvEmpty = findViewById(R.id.tv_empty); // 确保布局里有这个ID，没有也没关系，下面有判空
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResidentOrderAdapter(null);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            // 1. 获取该居民的所有订单
            List<Order> allOrders = db.orderDao().getOrdersByResident(String.valueOf(userId));
            List<Order> afterSalesOrders = new ArrayList<>();

            // 2. 筛选逻辑：只保留有售后状态的订单 (afterSalesStatus > 0)
            if (allOrders != null) {
                for (Order order : allOrders) {
                    if (order.afterSalesStatus > 0) {
                        afterSalesOrders.add(order);
                    }
                }
            }

            runOnUiThread(() -> {
                if (!afterSalesOrders.isEmpty()) {
                    adapter.updateList(afterSalesOrders);
                    recyclerView.setVisibility(View.VISIBLE);
                    if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    if (tvEmpty != null) {
                        tvEmpty.setText("暂无售后记录");
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                }
            });
        }).start();
    }
}
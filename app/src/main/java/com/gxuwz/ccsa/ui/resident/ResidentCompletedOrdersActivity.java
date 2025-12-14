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
import java.util.ArrayList;
import java.util.List;

public class ResidentCompletedOrdersActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ResidentOrderAdapter adapter;
    private AppDatabase db;
    private long userId;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_completed_orders);

        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("已完成订单");

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getLong("user_id", -1);
        db = AppDatabase.getInstance(this);

        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);

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
            List<Order> allOrders = db.orderDao().getOrdersByResident(String.valueOf(userId));
            List<Order> completedOrders = new ArrayList<>();

            // 过滤逻辑：只显示 已完成
            if (allOrders != null) {
                for (Order order : allOrders) {
                    if ("已完成".equals(order.status)) {
                        completedOrders.add(order);
                    }
                }
            }

            runOnUiThread(() -> {
                if (!completedOrders.isEmpty()) {
                    adapter.updateList(completedOrders);
                    recyclerView.setVisibility(View.VISIBLE);
                    if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    if (tvEmpty != null) {
                        tvEmpty.setText("暂无已完成的订单");
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                }
            });
        }).start();
    }
}
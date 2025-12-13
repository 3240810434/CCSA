package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.ResidentOrderAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Order;
import java.util.List;

public class ResidentOrdersActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ResidentOrderAdapter adapter;
    private AppDatabase db;
    private long userId;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_orders);

        // 设置标题 (假设布局中有标题栏，如果没有可忽略)
        try {
            TextView title = findViewById(R.id.tv_title); // 假设你的通用布局里有这个ID
            if (title != null) title.setText("我的订单");
            findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        } catch (Exception e) {}

        userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getLong("user_id", -1);
        db = AppDatabase.getInstance(this);

        recyclerView = findViewById(R.id.recycler_view);
        // 如果布局里没有 tv_empty，可以在 xml 中添加一个 TextView 默认隐藏
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
            // 获取该居民的所有订单，按ID倒序
            List<Order> orders = db.orderDao().getOrdersByResident(String.valueOf(userId));
            runOnUiThread(() -> {
                if (orders != null && !orders.isEmpty()) {
                    adapter.updateList(orders);
                    recyclerView.setVisibility(View.VISIBLE);
                    if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }
}
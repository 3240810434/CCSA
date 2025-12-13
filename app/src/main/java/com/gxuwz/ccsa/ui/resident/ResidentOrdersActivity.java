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

        // 设置简单的标题栏
        TextView tvTitle = findViewById(R.id.tv_title);
        if(tvTitle != null) tvTitle.setText("我的订单");

        ImageView btnBack = findViewById(R.id.btn_back);
        if(btnBack != null) btnBack.setOnClickListener(v -> finish());

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
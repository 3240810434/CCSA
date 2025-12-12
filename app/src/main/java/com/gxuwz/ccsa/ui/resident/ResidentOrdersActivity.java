package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.ResidentOrderAdapter; // 需新建
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Order;
import java.util.List;

public class ResidentOrdersActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AppDatabase db;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_orders); // 简单布局，含一个RecyclerView

        userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getLong("user_id", -1);
        db = AppDatabase.getInstance(this);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            List<Order> orders = db.orderDao().getOrdersByResident(String.valueOf(userId));
            runOnUiThread(() -> {
                recyclerView.setAdapter(new ResidentOrderAdapter(orders));
            });
        }).start();
    }
}
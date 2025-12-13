package com.gxuwz.ccsa.ui.merchant;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.MerchantCompletedOrderAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Order;
import java.util.List;

public class CompletedOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppDatabase db;
    // 【修改点1】改为 int 类型
    private int merchantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_orders);

        // 【修改点2】使用 getInt 获取 merchant_id
        merchantId = getSharedPreferences("merchant_prefs", MODE_PRIVATE).getInt("merchant_id", -1);

        if (merchantId == -1) {
            Toast.makeText(this, "登录状态失效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = AppDatabase.getInstance(this);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            List<Order> orders = db.orderDao().getOrdersByMerchantAndStatus(String.valueOf(merchantId), "已完成");

            runOnUiThread(() -> {
                MerchantCompletedOrderAdapter adapter = new MerchantCompletedOrderAdapter(this, orders);
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }
}
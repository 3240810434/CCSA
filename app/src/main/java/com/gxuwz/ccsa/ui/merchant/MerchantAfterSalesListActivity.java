package com.gxuwz.ccsa.ui.merchant;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.MerchantAfterSalesAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Order;
import java.util.List;

public class MerchantAfterSalesListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AppDatabase db;
    private int merchantId; // 改为int匹配你其他页面的逻辑

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 确保你的layout文件名是 activity_merchant_after_sales_list
        setContentView(R.layout.activity_merchant_after_sales_list);

        db = AppDatabase.getInstance(this);

        // 【核心修正】使用和你 CompletedOrdersActivity 一致的方式获取ID
        merchantId = getSharedPreferences("merchant_prefs", MODE_PRIVATE).getInt("merchant_id", -1);

        if (merchantId == -1) {
            Toast.makeText(this, "登录状态失效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            List<Order> list = db.orderDao().getMerchantAfterSalesOrders(String.valueOf(merchantId));

            runOnUiThread(() -> {
                if (list == null || list.isEmpty()) {
                    Toast.makeText(this, "暂无售后订单", Toast.LENGTH_SHORT).show();
                }
                MerchantAfterSalesAdapter adapter = new MerchantAfterSalesAdapter(list);
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }
}
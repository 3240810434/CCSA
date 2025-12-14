package com.gxuwz.ccsa.ui.merchant;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.MerchantAfterSalesAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Order;
import com.gxuwz.ccsa.util.SharedPreferencesUtil;
import java.util.List;

public class MerchantAfterSalesListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AppDatabase db;
    private String merchantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 确保你已经创建了对应的XML文件，文件名必须完全一致
        setContentView(R.layout.activity_merchant_after_sales_list);

        db = AppDatabase.getInstance(this);

        // 使用新创建的工具类获取ID
        merchantId = SharedPreferencesUtil.getInstance(this).getMerchantId();

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
            // 查询所有 afterSalesStatus > 0 的订单
            List<Order> list = db.orderDao().getMerchantAfterSalesOrders(merchantId);

            runOnUiThread(() -> {
                MerchantAfterSalesAdapter adapter = new MerchantAfterSalesAdapter(list);
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }
}
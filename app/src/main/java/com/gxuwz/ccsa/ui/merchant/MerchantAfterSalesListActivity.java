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

// 假设你有工具类保存登录信息，如果没有请自行替换获取ID的逻辑
public class MerchantAfterSalesListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AppDatabase db;
    private String merchantId; // 假设ID是String

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_after_sales_list);

        // 初始化DB
        db = AppDatabase.getInstance(this);

        // 模拟获取当前登录商家ID，实际开发请用 SharedPref
        // merchantId = SharedPreferencesUtil.getInstance(this).getMerchantId();
        merchantId = "1"; // 测试用写死

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        // 查询所有 afterSalesStatus > 0 的订单
        List<Order> list = db.orderDao().getMerchantAfterSalesOrders(merchantId);
        MerchantAfterSalesAdapter adapter = new MerchantAfterSalesAdapter(list);
        recyclerView.setAdapter(adapter);
    }
}
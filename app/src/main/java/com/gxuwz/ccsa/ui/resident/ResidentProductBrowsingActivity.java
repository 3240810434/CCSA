package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.ProductAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ResidentProductBrowsingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_product_browsing);

        // 初始化视图
        initViews();

        // 设置 RecyclerView
        // 使用 GridLayoutManager 实现每行两个卡片 (context, spanCount)
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // 关键点：这里调用我们在 Adapter 中定义的构造函数
        adapter = new ProductAdapter(this, productList);
        recyclerView.setAdapter(adapter);

        // 加载数据
        loadData();
    }

    private void initViews() {
        // 返回按钮逻辑
        View backBtn = findViewById(R.id.iv_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }

        tvEmpty = findViewById(R.id.tv_empty);
        recyclerView = findViewById(R.id.recycler_view);
    }

    private void loadData() {
        new Thread(() -> {
            // 数据库操作
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            // 确保 ProductDao 中有 getAllProducts() 方法
            List<Product> products = db.productDao().getAllProducts();

            runOnUiThread(() -> {
                productList.clear();
                if (products != null) {
                    productList.addAll(products);
                }

                // 刷新数据，此时 adapter 已经是 RecyclerView.Adapter 的子类，包含此方法
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                // 处理空状态显示
                if (productList.isEmpty()) {
                    if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
                    if (recyclerView != null) recyclerView.setVisibility(View.GONE);
                } else {
                    if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
                    if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }
}
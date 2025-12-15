package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.ProductAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Product;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.util.SharedPreferencesUtil;

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

        // 初始化适配器
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
            // 获取数据库实例
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());

            // 1. 获取当前登录用户信息
            User currentUser = SharedPreferencesUtil.getUser(getApplicationContext());

            List<Product> products;

            // 2. 根据用户小区筛选商品
            if (currentUser != null && !TextUtils.isEmpty(currentUser.getCommunity())) {
                // 如果用户已登录且有小区信息，调用筛选方法
                products = db.productDao().getProductsByCommunity(currentUser.getCommunity());
            } else {
                // 如果未登录或无小区信息，默认显示所有商品（或根据需求显示空）
                products = db.productDao().getAllProducts();
            }

            // 3. 更新 UI
            runOnUiThread(() -> {
                productList.clear();
                if (products != null) {
                    productList.addAll(products);
                }

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
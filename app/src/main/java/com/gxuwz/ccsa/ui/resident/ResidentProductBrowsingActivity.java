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

        initViews();

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(this, productList);
        recyclerView.setAdapter(adapter);

        loadData();
    }

    private void initViews() {
        View backBtn = findViewById(R.id.iv_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }
        tvEmpty = findViewById(R.id.tv_empty);
        recyclerView = findViewById(R.id.recycler_view);
    }

    private void loadData() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            User currentUser = SharedPreferencesUtil.getUser(getApplicationContext());

            List<Product> products;

            // --- 核心筛选逻辑 ---
            if (currentUser != null && !TextUtils.isEmpty(currentUser.getCommunity())) {
                // 如果用户已登录且有小区信息，只显示该小区商家的商品
                products = db.productDao().getProductsByCommunity(currentUser.getCommunity());
            } else {
                // 未登录默认显示所有
                products = db.productDao().getAllProducts();
            }

            runOnUiThread(() -> {
                productList.clear();
                if (products != null) {
                    productList.addAll(products);
                }
                adapter.notifyDataSetChanged();

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
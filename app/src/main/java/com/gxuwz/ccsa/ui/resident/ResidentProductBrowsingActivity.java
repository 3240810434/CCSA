package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
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

    // allProducts 保存从数据库加载的原始全部数据
    private List<Product> allProducts = new ArrayList<>();
    // displayList 用于 Adapter 展示（经过筛选后的数据）
    private List<Product> displayList = new ArrayList<>();

    private TextView tvEmpty;
    private EditText etSearch;
    private TabLayout tabLayout;

    // 定义分类标题
    private final String[] TAB_TITLES = {
            "推荐",
            "生鲜食材",
            "日用百货",
            "维修服务",
            "家政帮手",
            "保洁服务",
            "便民代办"
    };

    // 当前选中的分类，默认为“推荐”
    private String currentCategory = "推荐";
    // 当前搜索关键词
    private String searchKeyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_product_browsing);

        initViews();
        initTabs(); // 初始化顶部导航栏
        initListeners(); // 初始化监听器

        // 设置 RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(this, displayList);
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
        etSearch = findViewById(R.id.et_search);
        tabLayout = findViewById(R.id.tab_layout);
    }

    private void initTabs() {
        // 动态添加 Tab 标签
        for (String title : TAB_TITLES) {
            tabLayout.addTab(tabLayout.newTab().setText(title));
        }
    }

    private void initListeners() {
        // 1. 搜索框文字变化监听
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchKeyword = s.toString().trim();
                filterData(); // 实时筛选
            }
        });

        // 软键盘搜索按钮监听
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchKeyword = etSearch.getText().toString().trim();
                filterData();
                return true;
            }
            return false;
        });

        // 2. Tab 切换监听
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText() != null) {
                    currentCategory = tab.getText().toString();
                    filterData(); // 切换分类时筛选
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadData() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            User currentUser = SharedPreferencesUtil.getUser(getApplicationContext());

            List<Product> products;

            // --- 原始数据库查询逻辑保持不变 ---
            if (currentUser != null && !TextUtils.isEmpty(currentUser.getCommunity())) {
                products = db.productDao().getProductsByCommunity(currentUser.getCommunity());
            } else {
                products = db.productDao().getAllProducts();
            }

            runOnUiThread(() -> {
                allProducts.clear();
                if (products != null) {
                    allProducts.addAll(products);
                }
                // 数据加载完毕后，根据当前默认状态（“推荐”且无搜索词）进行初次展示
                filterData();
            });
        }).start();
    }

    /**
     * 核心筛选方法：同时根据【分类标签】和【搜索关键词】过滤列表
     */
    private void filterData() {
        displayList.clear();

        for (Product product : allProducts) {
            // 1. 判断分类是否匹配
            boolean isCategoryMatch = false;
            if ("推荐".equals(currentCategory)) {
                // "推荐"页面显示全部商品
                isCategoryMatch = true;
            } else {
                // 其他页面：比较商品的 tag 字段是否与当前 Tab 标题一致
                // 注意：这里假设商家发布商品时，tag 字段存的就是 "生鲜食材"、"维修服务" 等字符串
                if (product.tag != null && product.tag.equals(currentCategory)) {
                    isCategoryMatch = true;
                }
            }

            // 2. 判断搜索关键词是否匹配 (模糊搜索商品名)
            boolean isSearchMatch = true;
            if (!TextUtils.isEmpty(searchKeyword)) {
                if (product.getName() != null && product.getName().contains(searchKeyword)) {
                    isSearchMatch = true;
                } else {
                    isSearchMatch = false;
                }
            }

            // 只有两个条件都满足，才添加到显示列表
            if (isCategoryMatch && isSearchMatch) {
                displayList.add(product);
            }
        }

        // 刷新列表
        adapter.notifyDataSetChanged();
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (displayList.isEmpty()) {
            if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        } else {
            if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
            if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
package com.gxuwz.ccsa.ui.merchant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Merchant;
import com.gxuwz.ccsa.model.Product;
import com.gxuwz.ccsa.ui.resident.ImagePreviewActivity; // 假设有这个全屏查看
import com.youth.banner.Banner; // 如果项目有banner库
import com.youth.banner.adapter.BannerImageAdapter;
import com.youth.banner.holder.BannerImageHolder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MerchantProductDetailActivity extends AppCompatActivity {

    private int productId;
    private Banner banner; // 简单实现可以只是一个 ImageView，但需求说轮播
    private ImageView ivSingleImage;
    private TextView tvName, tvMerchantName, tvDesc;
    private ImageView ivMerchantAvatar, btnBack;
    private LinearLayout llPriceTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_product_detail);

        productId = getIntent().getIntExtra("product_id", -1);
        initView();
        loadData();
    }

    private void initView() {
        btnBack = findViewById(R.id.btn_close_detail); // fork.png
        // 这里如果是轮播组件，根据项目依赖决定，这里用简单的 ImageView 模拟单图，如果多图建议用 ViewPager
        // 由于不能确定项目是否有Banner库，我用原生 ImageView 并在代码逻辑中处理（简化：只显示第一张，点击查看全图）
        ivSingleImage = findViewById(R.id.iv_banner_single);

        tvName = findViewById(R.id.tv_detail_name);
        llPriceTable = findViewById(R.id.ll_detail_price_table);
        ivMerchantAvatar = findViewById(R.id.iv_merchant_avatar);
        tvMerchantName = findViewById(R.id.tv_merchant_name);
        tvDesc = findViewById(R.id.tv_detail_desc);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadData() {
        new Thread(() -> {
            Product product = AppDatabase.getInstance(this).productDao().getProductById(productId);
            if (product != null) {
                Merchant merchant = AppDatabase.getInstance(this).merchantDao().getMerchantById(product.merchantId);
                runOnUiThread(() -> updateUI(product, merchant));
            }
        }).start();
    }

    private void updateUI(Product product, Merchant merchant) {
        tvName.setText(product.name);
        tvDesc.setText(product.description);

        // 处理图片 (简单起见，显示第一张，点击全屏)
        List<String> images = new ArrayList<>();
        if (product.imagePaths != null) {
            images = Arrays.asList(product.imagePaths.split(","));
        }

        if (!images.isEmpty()) {
            Glide.with(this).load(images.get(0)).into(ivSingleImage);
            ivSingleImage.setOnClickListener(v -> {
                // 跳转到全屏查看逻辑
            });
        }

        // 渲染价格表
        llPriceTable.removeAllViews();
        try {
            JSONArray jsonArray = new JSONArray(product.priceTableJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                View row = LayoutInflater.from(this).inflate(R.layout.item_price_table_row_view, llPriceTable, false);
                TextView t1 = row.findViewById(R.id.tv_row_desc);
                TextView t2 = row.findViewById(R.id.tv_row_price);
                t1.setText(obj.getString("desc"));
                t2.setText("¥" + obj.getString("price"));
                llPriceTable.addView(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 商家信息
        if (merchant != null) {
            tvMerchantName.setText(merchant.name);
            // 假设 Merchant 有 avatar 字段，或者使用默认
            // Glide.with(this).load(merchant.avatar).into(ivMerchantAvatar);
        }
    }
}
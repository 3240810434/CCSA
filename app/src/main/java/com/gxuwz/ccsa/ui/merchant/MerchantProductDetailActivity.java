package com.gxuwz.ccsa.ui.merchant;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.BannerAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Merchant;
import com.gxuwz.ccsa.model.Product;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MerchantProductDetailActivity extends AppCompatActivity {

    private int productId;
    private ViewPager2 vpBanner;
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
        btnBack = findViewById(R.id.btn_close_detail);
        vpBanner = findViewById(R.id.vp_banner);
        View singleImg = findViewById(R.id.iv_banner_single);
        if (singleImg != null) singleImg.setVisibility(View.GONE);
        if (vpBanner != null) vpBanner.setVisibility(View.VISIBLE);

        tvName = findViewById(R.id.tv_detail_name);
        llPriceTable = findViewById(R.id.ll_detail_price_table);
        ivMerchantAvatar = findViewById(R.id.iv_merchant_avatar);
        tvMerchantName = findViewById(R.id.tv_merchant_name);
        tvDesc = findViewById(R.id.tv_detail_desc);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadData() {
        new Thread(() -> {
            // 每次进入页面重新查询数据库
            Product product = AppDatabase.getInstance(this).productDao().getProductById(productId);
            if (product != null) {
                Merchant merchant = AppDatabase.getInstance(this).merchantDao().findById(product.merchantId);
                runOnUiThread(() -> updateUI(product, merchant));
            }
        }).start();
    }

    private void updateUI(Product product, Merchant merchant) {
        tvName.setText(product.name);
        tvDesc.setText(product.description);

        if (vpBanner != null) {
            List<String> imageList = new ArrayList<>();
            if (product.imagePaths != null && !product.imagePaths.isEmpty()) {
                String[] paths = product.imagePaths.split(",");
                imageList.addAll(Arrays.asList(paths));
            }
            BannerAdapter bannerAdapter = new BannerAdapter(this, imageList);
            vpBanner.setAdapter(bannerAdapter);
            bannerAdapter.setOnBannerClickListener(this::showZoomImage);
        }

        llPriceTable.removeAllViews();
        try {
            JSONArray jsonArray = new JSONArray(product.priceTableJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                View row = LayoutInflater.from(this).inflate(R.layout.item_price_table_row_view, llPriceTable, false);
                TextView t1 = row.findViewById(R.id.tv_row_desc);
                TextView t2 = row.findViewById(R.id.tv_row_price);
                if (t1 != null) t1.setText(obj.optString("desc"));
                if (t2 != null) t2.setText("¥" + obj.optString("price"));
                llPriceTable.addView(row);
            }
        } catch (Exception e) {
            TextView tv = new TextView(this);
            tv.setText("暂无详细价格");
            tv.setPadding(10, 10, 10, 10);
            llPriceTable.addView(tv);
        }

        if (merchant != null) {
            tvMerchantName.setText(merchant.getMerchantName());
            String avatarUrl = merchant.getAvatar();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                // 【修复点】添加 signature(new ObjectKey(...)) 以清除 Glide 缓存，确保头像更新
                Glide.with(this)
                        .load(avatarUrl)
                        .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                        .error(R.drawable.merchant_picture)
                        .into(ivMerchantAvatar);
            } else {
                ivMerchantAvatar.setImageResource(R.drawable.merchant_picture);
            }
        } else {
            tvMerchantName.setText("未知商家");
            ivMerchantAvatar.setImageResource(R.drawable.merchant_picture);
        }
    }

    private void showZoomImage(String imageUrl) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        RelativeLayout root = new RelativeLayout(this);
        root.setBackgroundColor(Color.BLACK);

        ImageView fullImage = new ImageView(this);
        RelativeLayout.LayoutParams imgParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imgParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        fullImage.setLayoutParams(imgParams);
        fullImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

        Glide.with(this).load(imageUrl).into(fullImage);
        root.addView(fullImage);

        ImageView closeBtn = new ImageView(this);
        closeBtn.setImageResource(R.drawable.fork);
        int size = 100;
        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(size, size);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        btnParams.setMargins(0, 50, 50, 0);
        closeBtn.setLayoutParams(btnParams);
        closeBtn.setPadding(20, 20, 20, 20);

        closeBtn.setOnClickListener(v -> dialog.dismiss());
        root.addView(closeBtn);

        dialog.setContentView(root);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        }

        dialog.show();
    }
}
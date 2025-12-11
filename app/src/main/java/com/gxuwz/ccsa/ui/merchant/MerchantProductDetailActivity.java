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
    private ViewPager2 vpBanner; // 轮播图控件
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

        // 尝试获取 ViewPager2，如果布局中没有，请将 XML 中的 ImageView 替换为 <androidx.viewpager2.widget.ViewPager2 android:id="@+id/vp_banner" ... />
        vpBanner = findViewById(R.id.vp_banner);
        // 如果找不到 vp_banner，可能需要您去 XML 加上，或者这里为了兼容旧 XML 查找 iv_banner_single 并隐藏
        View singleImg = findViewById(R.id.iv_banner_single);
        if (singleImg != null) singleImg.setVisibility(View.GONE); // 隐藏旧的单图控件，使用轮播图
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

        // --- 修复点 1：轮播图逻辑 ---
        if (vpBanner != null) {
            List<String> imageList = new ArrayList<>();
            if (product.imagePaths != null && !product.imagePaths.isEmpty()) {
                // 以逗号分隔图片路径
                String[] paths = product.imagePaths.split(",");
                imageList.addAll(Arrays.asList(paths));
            }

            // 如果没有图片，放一张默认图路径（或者资源ID转String处理，这里简单处理为空列表则不显示或显示默认占位）
            // BannerAdapter 已经处理了加载占位图

            BannerAdapter bannerAdapter = new BannerAdapter(this, imageList);
            vpBanner.setAdapter(bannerAdapter);

            // 点击查看大图
            bannerAdapter.setOnBannerClickListener(this::showZoomImage);
        }

        // --- 价格表显示 (保持原样) ---
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

        // --- 修复点 2：商家头像与默认头像 ---
        if (merchant != null) {
            tvMerchantName.setText(merchant.getMerchantName());
            String avatarUrl = merchant.getAvatar();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(this)
                        .load(avatarUrl)
                        .error(R.drawable.merchant_picture) // 加载失败显示默认 merchant_picture
                        .into(ivMerchantAvatar);
            } else {
                // 默认显示 merchant_picture.jpg
                ivMerchantAvatar.setImageResource(R.drawable.merchant_picture);
            }
        } else {
            tvMerchantName.setText("未知商家");
            ivMerchantAvatar.setImageResource(R.drawable.merchant_picture);
        }
    }

    // --- 修复点 3：点击放大查看，带 fork.png 关闭按钮 ---
    private void showZoomImage(String imageUrl) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 动态创建一个全屏布局，包含大图和关闭按钮
        RelativeLayout root = new RelativeLayout(this);
        root.setBackgroundColor(Color.BLACK);

        // 大图
        ImageView fullImage = new ImageView(this);
        RelativeLayout.LayoutParams imgParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imgParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        fullImage.setLayoutParams(imgParams);
        fullImage.setScaleType(ImageView.ScaleType.FIT_CENTER); // 保持比例居中

        Glide.with(this).load(imageUrl).into(fullImage);
        root.addView(fullImage);

        // 关闭按钮 (fork.png)
        ImageView closeBtn = new ImageView(this);
        closeBtn.setImageResource(R.drawable.fork); // 使用 fork.png
        int size = 100; // 按钮大小，根据需要调整
        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(size, size);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        btnParams.setMargins(0, 50, 50, 0); // 设置边距
        closeBtn.setLayoutParams(btnParams);
        closeBtn.setPadding(20, 20, 20, 20); // 增加点击区域和内边距

        closeBtn.setOnClickListener(v -> dialog.dismiss());
        root.addView(closeBtn);

        dialog.setContentView(root);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        }

        dialog.show();
    }
}
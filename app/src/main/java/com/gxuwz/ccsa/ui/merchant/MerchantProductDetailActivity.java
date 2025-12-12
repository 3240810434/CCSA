package com.gxuwz.ccsa.ui.merchant;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
import java.util.concurrent.Executors;

public class MerchantProductDetailActivity extends AppCompatActivity {

    private int productId;
    private Product currentProduct;
    private ViewPager2 vpBanner;
    // 更新了控件变量，去掉了 tvType, 增加了 tvDelivery
    private TextView tvName, tvMerchantName, tvDesc, tvPrice, tvDelivery, tvTag;
    private ImageView ivMerchantAvatar, btnBack, btnEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_product_detail);

        productId = getIntent().getIntExtra("product_id", -1);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void initView() {
        btnBack = findViewById(R.id.btn_close_detail);
        btnEdit = findViewById(R.id.btn_edit);

        vpBanner = findViewById(R.id.vp_banner);
        if (vpBanner != null) vpBanner.setVisibility(View.VISIBLE);

        tvName = findViewById(R.id.tv_detail_name);
        tvDesc = findViewById(R.id.tv_detail_desc);
        tvPrice = findViewById(R.id.tv_detail_price);
        // 初始化新控件
        tvDelivery = findViewById(R.id.tv_detail_delivery);
        tvTag = findViewById(R.id.tv_detail_tag);

        ivMerchantAvatar = findViewById(R.id.iv_merchant_avatar);
        tvMerchantName = findViewById(R.id.tv_merchant_name);

        btnBack.setOnClickListener(v -> finish());
        btnEdit.setOnClickListener(v -> showEditDialog());
    }

    private void loadData() {
        new Thread(() -> {
            Product product = AppDatabase.getInstance(this).productDao().getProductById(productId);
            if (product != null) {
                currentProduct = product;
                Merchant merchant = AppDatabase.getInstance(this).merchantDao().findById(product.merchantId);
                runOnUiThread(() -> updateUI(product, merchant));
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "商品不存在", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }

    private void updateUI(Product product, Merchant merchant) {
        // 1. 设置名称
        tvName.setText(product.name);

        // 2. 设置描述
        tvDesc.setText(product.description);

        // 3. 设置轮播图
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

        // 4. 设置价格 (处理JSON以获取显示价格)
        try {
            if (product.priceTableJson != null) {
                JSONArray jsonArray = new JSONArray(product.priceTableJson);
                if (jsonArray.length() > 0) {
                    JSONObject obj = jsonArray.getJSONObject(0);
                    // 仅显示价格数字，不带 /unit
                    String price = obj.optString("price");
                    tvPrice.setText("¥ " + price);
                }
            } else {
                tvPrice.setText("¥ " + product.price);
            }
        } catch (Exception e) {
            e.printStackTrace();
            tvPrice.setText("¥ " + product.price);
        }

        // 5. 设置配送方式
        if (tvDelivery != null) {
            tvDelivery.setText(product.deliveryMethod == 0 ? "商家配送" : "自提");
        }

        // 6. 设置商品标签
        if (tvTag != null) {
            tvTag.setText((product.tag != null && !product.tag.isEmpty()) ? product.tag : "暂无标签");
        }

        // 商家信息
        if (merchant != null) {
            tvMerchantName.setText(merchant.getMerchantName());
            String avatarUrl = merchant.getAvatar();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
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

    private void showEditDialog() {
        if (currentProduct == null) return;

        Dialog dialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_merchant_product_edit, null);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setWindowAnimations(R.style.DialogAnimation);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            params.height = (int) (displayMetrics.heightPixels * 0.4);
            params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            params.dimAmount = 0.5f;
            window.setAttributes(params);
        }

        Button btnReEdit = view.findViewById(R.id.btn_re_edit);
        Button btnDelete = view.findViewById(R.id.btn_delete_product);

        btnReEdit.setOnClickListener(v -> {
            dialog.dismiss();
            goToEditPage();
        });

        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteConfirmDialog();
        });

        dialog.show();
    }

    private void goToEditPage() {
        Intent intent;
        // 根据类型跳转不同的编辑页面
        if ("GOODS".equals(currentProduct.type)) {
            intent = new Intent(this, PhysicalProductEditActivity.class);
        } else {
            intent = new Intent(this, ServiceEditActivity.class);
        }
        intent.putExtra("product", currentProduct);
        startActivity(intent);
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除该商品吗？此操作不可撤销。")
                .setPositiveButton("删除", (dialog, which) -> deleteProduct())
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteProduct() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).productDao().delete(currentProduct);
            runOnUiThread(() -> {
                Toast.makeText(this, "商品已删除", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
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
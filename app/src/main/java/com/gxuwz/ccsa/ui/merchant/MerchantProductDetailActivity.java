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
import android.widget.LinearLayout;
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
    private Product currentProduct; // 保存当前商品对象供编辑/删除使用
    private ViewPager2 vpBanner;
    private TextView tvName, tvMerchantName, tvDesc;
    private ImageView ivMerchantAvatar, btnBack, btnEdit; // 新增 btnEdit
    private LinearLayout llPriceTable;

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
        // 每次页面可见时刷新数据（因为可能从编辑页面返回）
        loadData();
    }

    private void initView() {
        btnBack = findViewById(R.id.btn_close_detail);
        btnEdit = findViewById(R.id.btn_edit); // 绑定编辑按钮

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

        // --- 新增：编辑按钮点击事件 ---
        btnEdit.setOnClickListener(v -> showEditDialog());
    }

    private void loadData() {
        new Thread(() -> {
            Product product = AppDatabase.getInstance(this).productDao().getProductById(productId);
            if (product != null) {
                currentProduct = product; // 保存引用
                Merchant merchant = AppDatabase.getInstance(this).merchantDao().findById(product.merchantId);
                runOnUiThread(() -> updateUI(product, merchant));
            } else {
                // 如果商品已被删除（例如在其他地方），关闭页面
                runOnUiThread(() -> {
                    Toast.makeText(this, "商品不存在", Toast.LENGTH_SHORT).show();
                    finish();
                });
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

    // --- 核心功能：显示编辑/删除弹窗 ---
    private void showEditDialog() {
        if (currentProduct == null) return;

        // 1. 创建 Dialog
        Dialog dialog = new Dialog(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_merchant_product_edit, null);
        dialog.setContentView(view);

        // 2. 设置 Window 属性 (底部滑出，占据40%高度)
        Window window = dialog.getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setWindowAnimations(R.style.DialogAnimation); // 设置动画样式
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 背景透明以显示圆角(如果布局有圆角)

            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;

            // 计算屏幕高度的 40%
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            params.height = (int) (displayMetrics.heightPixels * 0.4);

            // 设置背景变暗 (默认 dimAmount 通常为 0.5/0.6，这里确保开启)
            params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            params.dimAmount = 0.5f;

            window.setAttributes(params);
        }

        // 3. 绑定按钮事件
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
        // 根据商品类型跳转到对应的编辑页面
        // 注意：这里假设 Product 类中有 type 字段 (0:实体商品, 1:服务)
        // 如果没有 type 字段，请根据实际业务逻辑判断，例如 catch 异常或默认跳转
        Intent intent;
        if (currentProduct.getType() == 0) { // 实体商品
            intent = new Intent(this, PhysicalProductEditActivity.class);
        } else { // 服务类商品
            intent = new Intent(this, ServiceEditActivity.class);
        }
        // 传递当前商品对象，以便编辑页面填充数据
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
                finish(); // 删除后关闭详情页
            });
        });
    }

    // --- 图片放大查看功能 ---
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
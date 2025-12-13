package com.gxuwz.ccsa.ui.resident;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.BannerAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Merchant;
import com.gxuwz.ccsa.model.Order;
import com.gxuwz.ccsa.model.Product;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.util.DateUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResidentProductDetailActivity extends AppCompatActivity {

    private Product product;
    private AppDatabase db;
    private User currentUser;
    private Merchant productMerchant; // 增加商家对象
    private Dialog bottomSheetDialog;

    // UI 组件
    private ViewPager2 bannerViewPager;
    private TextView tvBannerIndicator;
    private TextView tvName, tvDesc, tvPrice, tvTypeInfo, tvTags;
    private ImageView ivMerchantAvatar;
    private TextView tvMerchantName;

    // 购买弹窗相关
    private Button btnPay;
    private LinearLayout containerSpecs;
    private LinearLayout containerService;
    private TextView tvServicePrice;
    private TextView tvServiceCount;
    private double currentPrice = 0.0;
    private int serviceQuantity = 1;
    private String selectedSpecStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_product_detail);

        db = AppDatabase.getInstance(this);
        product = (Product) getIntent().getSerializableExtra("product");

        if (product == null) {
            Toast.makeText(this, "商品数据错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initView();
        loadData();
    }

    private void initView() {
        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 绑定视图
        bannerViewPager = findViewById(R.id.banner_view_pager);
        tvBannerIndicator = findViewById(R.id.tv_banner_indicator);

        tvName = findViewById(R.id.tv_product_name);
        tvDesc = findViewById(R.id.tv_desc);
        tvPrice = findViewById(R.id.tv_price);
        tvTypeInfo = findViewById(R.id.tv_type_info);
        tvTags = findViewById(R.id.tv_tags);

        ivMerchantAvatar = findViewById(R.id.iv_merchant_avatar);
        tvMerchantName = findViewById(R.id.tv_merchant_name);

        // 购买按钮
        findViewById(R.id.btn_buy).setOnClickListener(v -> showPurchaseDialog());
    }

    private void loadData() {
        // 异步获取数据
        long userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getLong("user_id", -1);

        new Thread(() -> {
            // 1. 获取当前登录用户（用于下单）
            currentUser = db.userDao().findById(userId);
            // 2. 获取该商品的发布商家（用于显示）
            productMerchant = db.merchantDao().findById(product.getMerchantId());

            // 切回主线程更新UI
            runOnUiThread(this::setupUI);
        }).start();
    }

    private void setupUI() {
        if (isDestroyed()) return;

        // --- 1. 设置轮播图 ---
        List<String> imageUrls = new ArrayList<>();
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            String[] urls = product.getImageUrls().split(",");
            imageUrls.addAll(Arrays.asList(urls));
        } else {
            imageUrls.add(""); // 占位
        }

        BannerAdapter bannerAdapter = new BannerAdapter(this, imageUrls);
        bannerViewPager.setAdapter(bannerAdapter);

        // 查看大图逻辑
        bannerAdapter.setOnBannerClickListener(url -> {
            Intent intent = new Intent(this, ImagePreviewActivity.class);
            intent.putStringArrayListExtra("images", (ArrayList<String>) imageUrls);
            intent.putExtra("position", bannerViewPager.getCurrentItem());
            startActivity(intent);
        });

        // 指示器
        tvBannerIndicator.setText("1/" + imageUrls.size());
        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tvBannerIndicator.setText((position + 1) + "/" + imageUrls.size());
            }
        });

        // --- 2. 设置基本信息 ---
        tvName.setText(product.getName());
        tvDesc.setText(product.getDescription() != null ? product.getDescription() : "暂无描述");

        // --- 3. 根据类型设置差异化信息 (参考 MerchantProductDetailActivity 逻辑) ---
        if ("服务".equals(product.getType()) || "SERVICE".equals(product.getType())) {
            // === 服务商品 ===

            // 价格 + 单位
            String unit = (product.getUnit() != null && !product.getUnit().isEmpty()) ? product.getUnit() : "次";
            tvPrice.setText("¥ " + product.getPrice() + " / " + unit);
            tvPrice.setTextSize(18);

            // 服务类型解析
            String serviceMode = "上门服务"; // 默认
            try {
                if (product.priceTableJson != null) {
                    JSONArray ja = new JSONArray(product.priceTableJson);
                    if (ja.length() > 0) {
                        serviceMode = ja.getJSONObject(0).optString("mode", "上门服务");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            tvTypeInfo.setText("服务类型：" + serviceMode);
            tvTags.setText("服务标签：" + (product.tag != null ? product.tag : "暂无标签"));

        } else {
            // === 实物商品 ===

            // 价格表解析
            try {
                if (product.priceTableJson != null && !product.priceTableJson.isEmpty()) {
                    JSONArray jsonArray = new JSONArray(product.priceTableJson);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        sb.append(obj.optString("desc"))
                                .append(" : ¥ ")
                                .append(obj.optString("price"))
                                .append("\n");
                    }
                    tvPrice.setText(sb.toString().trim());
                    tvPrice.setTextSize(16); // 列表字稍小
                } else {
                    tvPrice.setText("¥ " + product.getPrice());
                    tvPrice.setTextSize(18);
                }
            } catch (Exception e) {
                tvPrice.setText("¥ " + product.getPrice());
            }

            // 配送方式
            String delivery = product.deliveryMethod == 0 ? "商家配送" : "到店自提";
            tvTypeInfo.setText("配送方式：" + delivery);
            tvTags.setText("商品标签：" + (product.tag != null ? product.tag : "暂无标签"));
        }

        // --- 4. 设置商家信息 ---
        if (productMerchant != null) {
            tvMerchantName.setText(productMerchant.getMerchantName());
            String avatarUrl = productMerchant.getAvatar();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(this)
                        .load(avatarUrl)
                        .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                        .placeholder(R.drawable.merchant_picture)
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

    // --- 购买弹窗逻辑 (与之前保持一致，为完整性保留) ---
    private void showPurchaseDialog() {
        bottomSheetDialog = new Dialog(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_purchase, null);

        Window window = bottomSheetDialog.getWindow();
        if (window != null) {
            window.setContentView(view);
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (getResources().getDisplayMetrics().heightPixels * 0.7));
            window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }

        TextView tvSheetName = view.findViewById(R.id.tv_sheet_name);
        ImageView ivThumb = view.findViewById(R.id.iv_thumb);
        containerSpecs = view.findViewById(R.id.ll_spec_container);
        containerService = view.findViewById(R.id.ll_service_container);
        tvServicePrice = view.findViewById(R.id.tv_service_base_price);
        tvServiceCount = view.findViewById(R.id.tv_service_count);
        ImageView btnServiceAdd = view.findViewById(R.id.btn_service_add);
        btnPay = view.findViewById(R.id.btn_pay_now);

        if (product != null) {
            tvSheetName.setText(product.getName());
            String imgUrl = product.getFirstImage();
            Glide.with(this).load(imgUrl).into(ivThumb);
        }

        if ("服务".equals(product.getType()) || "SERVICE".equals(product.getType())) {
            containerSpecs.setVisibility(View.GONE);
            containerService.setVisibility(View.VISIBLE);
            loadServiceLogic(tvServicePrice, tvServiceCount, btnServiceAdd);
        } else {
            containerService.setVisibility(View.GONE);
            containerSpecs.setVisibility(View.VISIBLE);
            loadPhysicalSpecs();
        }

        btnPay.setOnClickListener(v -> handlePayment());
        bottomSheetDialog.show();
    }

    private void loadPhysicalSpecs() {
        containerSpecs.removeAllViews();
        try {
            List<String[]> specs = new ArrayList<>();
            // 解析 JSON 规格表或使用单一价格
            if (product.priceTableJson != null && !product.priceTableJson.isEmpty()) {
                JSONArray jsonArray = new JSONArray(product.priceTableJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    specs.add(new String[]{obj.optString("desc"), obj.optString("price")});
                }
            } else {
                specs.add(new String[]{"标准规格", product.getPrice() != null ? product.getPrice() : "0"});
            }

            for (String[] spec : specs) {
                View itemView = LayoutInflater.from(this).inflate(R.layout.item_spec_row, containerSpecs, false);
                TextView tvSpec = itemView.findViewById(R.id.tv_spec_name);
                TextView tvP = itemView.findViewById(R.id.tv_spec_price);
                LinearLayout llRow = itemView.findViewById(R.id.ll_spec_row);

                tvSpec.setText(spec[0]);
                tvP.setText("¥" + spec[1]);

                llRow.setOnClickListener(v -> {
                    for (int i = 0; i < containerSpecs.getChildCount(); i++) {
                        View child = containerSpecs.getChildAt(i);
                        child.findViewById(R.id.ll_spec_row).setBackgroundResource(R.drawable.box_bg);
                        ((TextView) child.findViewById(R.id.tv_spec_name)).setTextColor(Color.BLACK);
                        ((TextView) child.findViewById(R.id.tv_spec_price)).setTextColor(Color.BLACK);
                    }
                    llRow.setBackgroundResource(R.drawable.border_red);
                    tvSpec.setTextColor(Color.RED);
                    tvP.setTextColor(Color.RED);

                    try {
                        currentPrice = Double.parseDouble(spec[1]);
                    } catch (NumberFormatException e) { currentPrice = 0; }

                    selectedSpecStr = spec[0];
                    updatePayButton();
                });
                containerSpecs.addView(itemView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadServiceLogic(TextView tvPrice, TextView tvCount, ImageView btnAdd) {
        try {
            double basePrice = 0;
            try { basePrice = Double.parseDouble(product.getPrice()); } catch (Exception e) { basePrice = 50.0; }

            final double finalBasePrice = basePrice;
            String unit = product.getUnit() != null ? product.getUnit() : "次";
            tvPrice.setText("基础价格: ¥" + finalBasePrice + "/" + unit);

            serviceQuantity = 1;
            currentPrice = finalBasePrice;
            tvCount.setText("x" + serviceQuantity);
            updatePayButton();

            btnAdd.setOnClickListener(v -> {
                serviceQuantity++;
                currentPrice += finalBasePrice;
                tvCount.setText("x" + serviceQuantity);
                updatePayButton();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePayButton() {
        if (btnPay != null) {
            btnPay.setText("立即支付 ¥" + String.format("%.2f", currentPrice));
        }
    }

    private void handlePayment() {
        if (currentPrice <= 0) {
            Toast.makeText(this, "请选择商品/服务规格", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                Order order = new Order();
                order.orderNo = "ORD" + System.currentTimeMillis();
                if (currentUser != null) {
                    order.residentId = String.valueOf(currentUser.getId());
                    order.residentName = currentUser.getName();
                    order.residentPhone = currentUser.getPhone();
                    order.address = currentUser.getCommunityName() + currentUser.getBuilding() + currentUser.getRoomNumber();
                } else {
                    order.residentId = "0";
                    order.address = "未登录";
                }

                order.merchantId = String.valueOf(product.getMerchantId());
                order.productId = String.valueOf(product.getId());
                order.productName = product.getName();
                order.productType = product.getType();
                order.productImageUrl = product.getImageUrls();

                if ("服务".equals(product.getType()) || "SERVICE".equals(product.getType())) {
                    order.selectedSpec = "服务";
                    order.serviceCount = serviceQuantity;
                } else {
                    order.selectedSpec = selectedSpecStr;
                    order.serviceCount = 1;
                }

                order.payAmount = String.format("%.2f", currentPrice);
                order.status = "待接单";
                order.createTime = DateUtils.getCurrentDateTime();

                db.orderDao().insert(order);

                runOnUiThread(() -> {
                    Toast.makeText(this, "支付成功！", Toast.LENGTH_SHORT).show();
                    if (bottomSheetDialog != null) bottomSheetDialog.dismiss();
                    finish();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "支付失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
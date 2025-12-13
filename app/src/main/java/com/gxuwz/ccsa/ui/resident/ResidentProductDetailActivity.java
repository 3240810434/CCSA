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

import androidx.appcompat.app.AlertDialog;
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
    private Merchant productMerchant;
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
    private TextView tvServiceCountDisplay;
    private double currentPrice = 0.0;
    private int serviceQuantity = 1; // 服务数量
    private String selectedSpecStr = ""; // 实物选中的规格描述
    private String productUnit = "";

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
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        bannerViewPager = findViewById(R.id.banner_view_pager);
        tvBannerIndicator = findViewById(R.id.tv_banner_indicator);
        tvName = findViewById(R.id.tv_product_name);
        tvDesc = findViewById(R.id.tv_desc);
        tvPrice = findViewById(R.id.tv_price);
        tvTypeInfo = findViewById(R.id.tv_type_info);
        tvTags = findViewById(R.id.tv_tags);
        ivMerchantAvatar = findViewById(R.id.iv_merchant_avatar);
        tvMerchantName = findViewById(R.id.tv_merchant_name);
        findViewById(R.id.btn_buy).setOnClickListener(v -> showPurchaseDialog());
    }

    private void loadData() {
        long userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getLong("user_id", -1);
        new Thread(() -> {
            currentUser = db.userDao().findById(userId);
            productMerchant = db.merchantDao().findById(product.getMerchantId());
            runOnUiThread(this::setupUI);
        }).start();
    }

    private void setupUI() {
        if (isDestroyed()) return;

        // 1. 轮播图
        List<String> imageUrls = new ArrayList<>();
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            imageUrls.addAll(Arrays.asList(product.getImageUrls().split(",")));
        } else {
            imageUrls.add("");
        }
        BannerAdapter bannerAdapter = new BannerAdapter(this, imageUrls);
        bannerViewPager.setAdapter(bannerAdapter);
        tvBannerIndicator.setText("1/" + imageUrls.size());
        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tvBannerIndicator.setText((position + 1) + "/" + imageUrls.size());
            }
        });

        // 2. 基本信息
        tvName.setText(product.getName());
        tvDesc.setText(product.getDescription() != null ? product.getDescription() : "暂无描述");
        productUnit = (product.getUnit() != null && !product.getUnit().isEmpty()) ? product.getUnit() : "份";

        // 3. 价格与类型显示
        if ("服务".equals(product.getType()) || "SERVICE".equals(product.getType())) {
            tvPrice.setText("¥ " + product.getPrice() + " / " + productUnit);
            tvPrice.setTextSize(18);
            tvTypeInfo.setText("类型：上门服务");
        } else {
            // 实物
            tvPrice.setText("¥ " + product.getPrice());
            tvPrice.setTextSize(18);
            String delivery = product.deliveryMethod == 0 ? "商家配送" : "到店自提";
            tvTypeInfo.setText("配送方式：" + delivery);
        }
        tvTags.setText("标签：" + (product.tag != null ? product.tag : "暂无"));

        // 4. 商家信息
        if (productMerchant != null) {
            tvMerchantName.setText(productMerchant.getMerchantName());
            Glide.with(this).load(productMerchant.getAvatar())
                    .placeholder(R.drawable.merchant_picture)
                    .error(R.drawable.merchant_picture)
                    .into(ivMerchantAvatar);
        } else {
            tvMerchantName.setText("未知商家");
        }
    }

    // === 核心修改：购买弹窗 ===
    private void showPurchaseDialog() {
        bottomSheetDialog = new Dialog(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_resident_purchase, null);

        Window window = bottomSheetDialog.getWindow();
        if (window != null) {
            window.setContentView(view);
            window.setGravity(Gravity.BOTTOM);
            // 设置高度为屏幕的 70%
            int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.7);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height);
            window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }

        // 1. 顶部：居民收货地址信息
        TextView tvAddrName = view.findViewById(R.id.tv_addr_name);
        TextView tvAddrPhone = view.findViewById(R.id.tv_addr_phone);
        TextView tvAddrDetail = view.findViewById(R.id.tv_addr_detail);

        if (currentUser != null) {
            tvAddrName.setText("姓名：" + currentUser.getName());
            tvAddrPhone.setText("电话：" + currentUser.getPhone());
            String address = String.format("%s %s %s",
                    currentUser.getCommunityName() != null ? currentUser.getCommunityName() : "未知小区",
                    currentUser.getBuilding() != null ? currentUser.getBuilding() : "",
                    currentUser.getRoomNumber() != null ? currentUser.getRoomNumber() : "");
            tvAddrDetail.setText("地址：" + address);
        } else {
            tvAddrDetail.setText("请先登录以获取收货地址");
        }

        // 2. 中间：商品图和名称
        TextView tvSheetName = view.findViewById(R.id.tv_sheet_name);
        ImageView ivThumb = view.findViewById(R.id.iv_thumb);
        if (product != null) {
            tvSheetName.setText(product.getName());
            Glide.with(this).load(product.getFirstImage()).into(ivThumb);
        }

        // 3. 选择区域
        containerSpecs = view.findViewById(R.id.ll_spec_container);
        containerService = view.findViewById(R.id.ll_service_container);
        tvServicePrice = view.findViewById(R.id.tv_service_base_price);
        tvServiceCountDisplay = view.findViewById(R.id.tv_service_count_display);
        ImageView btnServiceAdd = view.findViewById(R.id.btn_service_add);
        btnPay = view.findViewById(R.id.btn_pay_now);

        // 重置状态
        serviceQuantity = 1;
        currentPrice = 0.0;
        selectedSpecStr = "";

        // 根据类型加载不同逻辑
        if ("服务".equals(product.getType()) || "SERVICE".equals(product.getType())) {
            // === 服务逻辑 ===
            containerSpecs.setVisibility(View.GONE);
            containerService.setVisibility(View.VISIBLE);

            double basePrice = 0;
            try { basePrice = Double.parseDouble(product.getPrice()); } catch (Exception e) { basePrice = 0; }

            final double pricePerUnit = basePrice;
            currentPrice = pricePerUnit * serviceQuantity; // 初始价格

            tvServicePrice.setText("单价: ¥" + pricePerUnit + "/" + productUnit);
            tvServiceCountDisplay.setText(String.valueOf(serviceQuantity));
            updatePayButton();

            // 价格累加逻辑
            btnServiceAdd.setOnClickListener(v -> {
                serviceQuantity++;
                currentPrice = pricePerUnit * serviceQuantity;
                tvServiceCountDisplay.setText(String.valueOf(serviceQuantity));
                updatePayButton();
            });

        } else {
            // === 实物逻辑 ===
            containerService.setVisibility(View.GONE);
            containerSpecs.setVisibility(View.VISIBLE);
            loadPhysicalSpecs();
        }

        // 4. 点击支付按钮，弹出支付方式选择
        btnPay.setOnClickListener(v -> {
            if (currentPrice <= 0) {
                Toast.makeText(this, "请选择有效的规格", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentUser == null) {
                Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
                return;
            }
            showPaymentMethodDialog();
        });

        bottomSheetDialog.show();
    }

    // 加载实物规格表
    private void loadPhysicalSpecs() {
        containerSpecs.removeAllViews();
        try {
            JSONArray jsonArray;
            if (product.priceTableJson != null && !product.priceTableJson.isEmpty()) {
                jsonArray = new JSONArray(product.priceTableJson);
            } else {
                // 如果没有json表，构造一个默认的单行
                jsonArray = new JSONArray();
                JSONObject defaultObj = new JSONObject();
                defaultObj.put("desc", "默认规格");
                defaultObj.put("price", product.getPrice());
                jsonArray.put(defaultObj);
            }

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String desc = obj.optString("desc");
                String priceStr = obj.optString("price");

                // 动态创建每一行：复用 spec_item 布局 或 动态生成 View
                // 这里使用动态生成简单的布局
                View rowView = LayoutInflater.from(this).inflate(R.layout.item_spec_row, containerSpecs, false);
                TextView tvName = rowView.findViewById(R.id.tv_spec_name);
                TextView tvP = rowView.findViewById(R.id.tv_spec_price);
                LinearLayout llRoot = rowView.findViewById(R.id.ll_spec_row);

                tvName.setText(desc);
                tvP.setText("¥" + priceStr);

                // 点击事件：单选变红
                llRoot.setOnClickListener(v -> {
                    // 清除其他选中状态
                    for (int j = 0; j < containerSpecs.getChildCount(); j++) {
                        View child = containerSpecs.getChildAt(j);
                        child.findViewById(R.id.ll_spec_row).setBackgroundResource(R.drawable.box_bg); // 默认背景
                        ((TextView) child.findViewById(R.id.tv_spec_name)).setTextColor(Color.BLACK);
                        ((TextView) child.findViewById(R.id.tv_spec_price)).setTextColor(Color.BLACK);
                    }
                    // 设置当前选中状态
                    llRoot.setBackgroundResource(R.drawable.border_red); // 红色边框背景
                    tvName.setTextColor(Color.RED);
                    tvP.setTextColor(Color.RED);

                    // 更新价格
                    try {
                        currentPrice = Double.parseDouble(priceStr);
                    } catch (NumberFormatException e) { currentPrice = 0; }
                    selectedSpecStr = desc;
                    updatePayButton();
                });

                containerSpecs.addView(rowView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePayButton() {
        if (btnPay != null) {
            btnPay.setText("立即支付 ¥" + String.format("%.2f", currentPrice));
        }
    }

    // 弹出支付方式选择
    private void showPaymentMethodDialog() {
        String[] methods = {"微信支付", "支付宝"};
        new AlertDialog.Builder(this)
                .setTitle("选择支付方式")
                .setItems(methods, (dialog, which) -> {
                    String method = methods[which];
                    createOrder(method);
                })
                .show();
    }

    // 创建订单并写入数据库
    private void createOrder(String payMethod) {
        new Thread(() -> {
            try {
                Order order = new Order();
                order.orderNo = "ORD" + System.currentTimeMillis() + (int)(Math.random()*100);
                order.residentId = String.valueOf(currentUser.getId());
                order.residentName = currentUser.getName();
                order.residentPhone = currentUser.getPhone();
                order.address = currentUser.getCommunityName() + " " + currentUser.getBuilding() + " " + currentUser.getRoomNumber();

                order.merchantId = String.valueOf(product.getMerchantId());
                order.merchantName = productMerchant != null ? productMerchant.getMerchantName() : "未知商家";

                order.productId = String.valueOf(product.getId());
                order.productName = product.getName();
                order.productType = product.getType();
                order.productImageUrl = product.getFirstImage();

                if ("服务".equals(product.getType()) || "SERVICE".equals(product.getType())) {
                    order.selectedSpec = "标准服务";
                    order.serviceCount = serviceQuantity;
                } else {
                    order.selectedSpec = selectedSpecStr;
                    order.serviceCount = 1; // 实物默认数量1
                }

                order.payAmount = String.format("%.2f", currentPrice);
                order.paymentMethod = payMethod; // 记录支付方式
                order.status = "待接单";
                order.createTime = DateUtils.getCurrentDateTime();

                db.orderDao().insert(order);

                runOnUiThread(() -> {
                    Toast.makeText(this, "支付成功！订单已生成", Toast.LENGTH_SHORT).show();
                    if (bottomSheetDialog != null) bottomSheetDialog.dismiss();
                    // 跳转或关闭
                    finish();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "订单创建失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
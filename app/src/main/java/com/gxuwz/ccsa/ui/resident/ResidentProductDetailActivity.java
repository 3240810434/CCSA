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

    // 购买弹窗相关变量
    private Button btnPay;
    private LinearLayout containerSpecs;
    private LinearLayout containerService;
    private TextView tvServicePrice;
    private TextView tvServiceCountDisplay;
    private double currentPrice = 0.0;
    private int serviceQuantity = 1;
    private String selectedSpecStr = "";
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

        // 点击底部“购买”按钮
        findViewById(R.id.btn_buy).setOnClickListener(v -> showPurchaseDialog());
    }

    private void loadData() {
        // 获取当前用户ID
        long userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getLong("user_id", -1);
        new Thread(() -> {
            // 1. 查询当前用户信息（如果未登录，userId为-1，查出来是null）
            currentUser = db.userDao().findById(userId);
            // 2. 查询商家信息
            productMerchant = db.merchantDao().findById(product.getMerchantId());

            runOnUiThread(this::setupUI);
        }).start();
    }

    private void setupUI() {
        if (isDestroyed()) return;

        // 设置轮播图
        List<String> imageUrls = new ArrayList<>();
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            imageUrls.addAll(Arrays.asList(product.getImageUrls().split(",")));
        } else {
            imageUrls.add("");
        }
        BannerAdapter bannerAdapter = new BannerAdapter(this, imageUrls);
        bannerViewPager.setAdapter(bannerAdapter);

        // 设置基本信息
        tvName.setText(product.getName());
        tvDesc.setText(product.getDescription());
        productUnit = (product.getUnit() != null && !product.getUnit().isEmpty()) ? product.getUnit() : "份";

        // 根据类型显示价格
        if ("服务".equals(product.getType()) || "SERVICE".equals(product.getType())) {
            tvPrice.setText("¥ " + product.getPrice() + " / " + productUnit);
            tvTypeInfo.setText("类型：上门服务");
        } else {
            tvPrice.setText("¥ " + product.getPrice());
            String delivery = product.deliveryMethod == 0 ? "商家配送" : "到店自提";
            tvTypeInfo.setText("配送方式：" + delivery);
        }

        // 商家信息
        if (productMerchant != null) {
            tvMerchantName.setText(productMerchant.getMerchantName());
            Glide.with(this).load(productMerchant.getAvatar()).placeholder(R.drawable.merchant_picture).into(ivMerchantAvatar);
        }
    }

    // =================================================================
    // 核心代码：显示购买弹窗
    // =================================================================
    private void showPurchaseDialog() {
        bottomSheetDialog = new Dialog(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert);
        // 使用新创建的布局 dialog_resident_purchase
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

        // --- 1. 绑定顶部居民信息 (解决不显示的问题) ---
        TextView tvAddrName = view.findViewById(R.id.tv_addr_name);
        TextView tvAddrPhone = view.findViewById(R.id.tv_addr_phone);
        TextView tvAddrDetail = view.findViewById(R.id.tv_addr_detail);

        if (currentUser != null) {
            // 已登录：显示真实信息
            tvAddrName.setText("姓名：" + currentUser.getName());
            tvAddrPhone.setText("电话：" + currentUser.getPhone());

            String community = currentUser.getCommunityName() != null ? currentUser.getCommunityName() : "";
            String building = currentUser.getBuilding() != null ? currentUser.getBuilding() : "";
            String room = currentUser.getRoomNumber() != null ? currentUser.getRoomNumber() : "";

            // 明文显示地址
            tvAddrDetail.setText("地址：" + community + " " + building + " " + room);
        } else {
            // 未登录：提示登录
            tvAddrName.setText("未登录");
            tvAddrPhone.setText("");
            tvAddrDetail.setText("请先登录以获取收货地址");
            tvAddrDetail.setTextColor(Color.RED);
        }

        // --- 2. 绑定商品基本信息 ---
        TextView tvSheetName = view.findViewById(R.id.tv_sheet_name);
        ImageView ivThumb = view.findViewById(R.id.iv_thumb);

        tvSheetName.setText(product.getName());
        // 加载第一张图
        Glide.with(this).load(product.getFirstImage()).into(ivThumb);

        // --- 3. 初始化选择区域控件 ---
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

        // --- 4. 区分实物与服务逻辑 ---
        if ("服务".equals(product.getType()) || "SERVICE".equals(product.getType())) {
            // ============ 服务商品逻辑 ============
            containerSpecs.setVisibility(View.GONE);
            containerService.setVisibility(View.VISIBLE);

            double basePrice = 0;
            try { basePrice = Double.parseDouble(product.getPrice()); } catch (Exception e) { basePrice = 0; }

            final double pricePerUnit = basePrice;
            currentPrice = pricePerUnit * serviceQuantity; // 初始总价

            tvServicePrice.setText("单价: ¥" + pricePerUnit + "/" + productUnit);
            tvServiceCountDisplay.setText(String.valueOf(serviceQuantity));
            updatePayButton();

            // 点击加号，数量增加，价格累加
            btnServiceAdd.setOnClickListener(v -> {
                serviceQuantity++;
                currentPrice = pricePerUnit * serviceQuantity;
                tvServiceCountDisplay.setText(String.valueOf(serviceQuantity));
                updatePayButton();
            });

        } else {
            // ============ 实物商品逻辑 ============
            containerService.setVisibility(View.GONE);
            containerSpecs.setVisibility(View.VISIBLE);
            // 加载规格表
            loadPhysicalSpecs();
        }

        // --- 5. 支付按钮点击事件 ---
        btnPay.setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
                // 可以跳转去登录页
                // startActivity(new Intent(this, ResidentLoginActivity.class));
                return;
            }
            if (currentPrice <= 0) {
                Toast.makeText(this, "请选择商品规格", Toast.LENGTH_SHORT).show();
                return;
            }
            // 弹出支付方式选择
            showPaymentMethodDialog();
        });

        bottomSheetDialog.show();
    }

    // 辅助方法：加载实物规格表
    private void loadPhysicalSpecs() {
        containerSpecs.removeAllViews();
        try {
            JSONArray jsonArray;
            if (product.priceTableJson != null && !product.priceTableJson.isEmpty()) {
                jsonArray = new JSONArray(product.priceTableJson);
            } else {
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

                // 动态生成一行规格 View
                View rowView = LayoutInflater.from(this).inflate(R.layout.item_spec_row, containerSpecs, false);
                TextView tvName = rowView.findViewById(R.id.tv_spec_name);
                TextView tvP = rowView.findViewById(R.id.tv_spec_price);
                LinearLayout llRoot = rowView.findViewById(R.id.ll_spec_row);

                tvName.setText(desc);
                tvP.setText("¥" + priceStr);

                // 单选逻辑
                llRoot.setOnClickListener(v -> {
                    // 1. 重置所有行颜色
                    for (int j = 0; j < containerSpecs.getChildCount(); j++) {
                        View child = containerSpecs.getChildAt(j);
                        child.findViewById(R.id.ll_spec_row).setBackgroundResource(R.drawable.box_bg);
                        ((TextView) child.findViewById(R.id.tv_spec_name)).setTextColor(Color.BLACK);
                        ((TextView) child.findViewById(R.id.tv_spec_price)).setTextColor(Color.BLACK);
                    }
                    // 2. 高亮当前行（红框红字）
                    llRoot.setBackgroundResource(R.drawable.border_red);
                    tvName.setTextColor(Color.RED);
                    tvP.setTextColor(Color.RED);

                    // 3. 更新价格和选中项
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

    // 更新底部支付按钮文字
    private void updatePayButton() {
        if (btnPay != null) {
            btnPay.setText("立即支付 ¥" + String.format("%.2f", currentPrice));
        }
    }

    // 弹出支付方式选择 (模拟支付系统)
    private void showPaymentMethodDialog() {
        String[] methods = {"微信支付", "支付宝"};
        new AlertDialog.Builder(this)
                .setTitle("选择支付方式")
                .setItems(methods, (dialog, which) -> {
                    String method = methods[which];
                    // 处理下单逻辑
                    createOrder(method);
                })
                .show();
    }

    // 创建订单写入数据库
    private void createOrder(String payMethod) {
        new Thread(() -> {
            try {
                Order order = new Order();
                order.orderNo = "ORD" + System.currentTimeMillis() + (int)(Math.random()*1000);

                // 居民信息
                order.residentId = String.valueOf(currentUser.getId());
                order.residentName = currentUser.getName();
                order.residentPhone = currentUser.getPhone();
                order.address = currentUser.getCommunityName() + " " + currentUser.getBuilding() + " " + currentUser.getRoomNumber();

                // 商家与商品信息
                order.merchantId = String.valueOf(product.getMerchantId());
                order.merchantName = productMerchant != null ? productMerchant.getMerchantName() : "未知商家";
                order.productId = String.valueOf(product.getId());
                order.productName = product.getName();
                order.productType = product.getType();
                order.productImageUrl = product.getFirstImage();

                // 区分规格/数量
                if ("服务".equals(product.getType()) || "SERVICE".equals(product.getType())) {
                    order.selectedSpec = "上门服务";
                    order.serviceCount = serviceQuantity;
                } else {
                    order.selectedSpec = selectedSpecStr;
                    order.serviceCount = 1;
                }

                order.payAmount = String.format("%.2f", currentPrice);
                order.paymentMethod = payMethod; // 记录支付方式
                order.status = "待接单";
                order.createTime = DateUtils.getCurrentDateTime();

                db.orderDao().insert(order);

                runOnUiThread(() -> {
                    Toast.makeText(this, "支付成功！订单已生成", Toast.LENGTH_SHORT).show();
                    if (bottomSheetDialog != null) bottomSheetDialog.dismiss();
                    finish(); // 关闭页面
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "订单创建失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
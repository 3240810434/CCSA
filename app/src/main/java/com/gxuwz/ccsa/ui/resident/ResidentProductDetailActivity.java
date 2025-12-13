package com.gxuwz.ccsa.ui.resident;

import android.app.Dialog;
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
import android.content.Intent;
import android.content.SharedPreferences; // 导入 SharedPreferences
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
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

    private ViewPager2 bannerViewPager;
    private TextView tvBannerIndicator;
    private TextView tvName, tvDesc, tvPrice, tvTypeInfo, tvTags;
    private ImageView ivMerchantAvatar;
    private TextView tvMerchantName;

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
        findViewById(R.id.btn_buy).setOnClickListener(v -> showPurchaseDialog());
    }

    private void loadData() {
        // 从 SharedPreferences 获取 userId
        SharedPreferences sp = getSharedPreferences("user_prefs", MODE_PRIVATE);
        long userId = sp.getLong("user_id", -1);

        new Thread(() -> {
            if (userId != -1) {
                currentUser = db.userDao().findById(userId);
            }
            if (product != null) {
                productMerchant = db.merchantDao().findById(product.getMerchantId());
            }
            runOnUiThread(this::setupUI);
        }).start();
    }

    private void setupUI() {
        if (isDestroyed() || isFinishing()) return;

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

        tvName.setText(product.getName());
        tvDesc.setText(product.getDescription() != null ? product.getDescription() : "暂无描述");
        productUnit = (product.getUnit() != null && !product.getUnit().isEmpty()) ? product.getUnit() : "份";

        // 判断类型，兼容中英文
        boolean isService = "服务".equals(product.getType()) || "SERVICE".equalsIgnoreCase(product.getType());

        if (isService) {
            tvPrice.setText("¥ " + product.getPrice() + " / " + productUnit);
            tvPrice.setTextSize(18);
            tvTypeInfo.setText("类型：上门服务");
        } else {
            tvPrice.setText("¥ " + product.getPrice());
            tvPrice.setTextSize(18);
            String delivery = product.deliveryMethod == 0 ? "商家配送" : "到店自提";
            tvTypeInfo.setText("配送方式：" + delivery);
        }
        tvTags.setText("标签：" + (product.tag != null ? product.tag : "暂无"));

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

    private void showPurchaseDialog() {
        // 核心检查：如果当前没有加载到用户
        if (currentUser == null) {
            long userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getLong("user_id", -1);
            if (userId == -1) {
                Toast.makeText(this, "未检测到登录状态，请先登录", Toast.LENGTH_SHORT).show();
                // 可选：跳转回登录页
                // startActivity(new Intent(this, com.gxuwz.ccsa.login.ResidentLoginActivity.class));
            } else {
                Toast.makeText(this, "正在加载用户信息，请稍候...", Toast.LENGTH_SHORT).show();
                // 尝试重新加载
                loadData();
            }
            return;
        }

        bottomSheetDialog = new Dialog(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_resident_purchase, null);

        Window window = bottomSheetDialog.getWindow();
        if (window != null) {
            window.setContentView(view);
            window.setGravity(Gravity.BOTTOM);
            int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.7);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height);
            window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }

        TextView tvAddrName = view.findViewById(R.id.tv_addr_name);
        TextView tvAddrPhone = view.findViewById(R.id.tv_addr_phone);
        TextView tvAddrDetail = view.findViewById(R.id.tv_addr_detail);

        // 填充用户信息
        if (currentUser != null) {
            tvAddrName.setText("姓名：" + (currentUser.getName() == null ? "暂无" : currentUser.getName()));
            tvAddrPhone.setText("电话：" + (currentUser.getPhone() == null ? "暂无" : currentUser.getPhone()));
            String address = String.format("%s %s %s",
                    currentUser.getCommunityName() != null ? currentUser.getCommunityName() : "",
                    currentUser.getBuilding() != null ? currentUser.getBuilding() : "",
                    currentUser.getRoomNumber() != null ? currentUser.getRoomNumber() : "");
            tvAddrDetail.setText("地址：" + address);
        }

        TextView tvSheetName = view.findViewById(R.id.tv_sheet_name);
        ImageView ivThumb = view.findViewById(R.id.iv_thumb);
        if (product != null) {
            tvSheetName.setText(product.getName());
            Glide.with(this).load(product.getFirstImage()).into(ivThumb);
        }

        containerSpecs = view.findViewById(R.id.ll_spec_container);
        containerService = view.findViewById(R.id.ll_service_container);
        tvServicePrice = view.findViewById(R.id.tv_service_base_price);
        tvServiceCountDisplay = view.findViewById(R.id.tv_service_count_display);
        ImageView btnServiceAdd = view.findViewById(R.id.btn_service_add);
        btnPay = view.findViewById(R.id.btn_pay_now);

        serviceQuantity = 1;
        currentPrice = 0.0;
        selectedSpecStr = "";

        boolean isService = "服务".equals(product.getType()) || "SERVICE".equalsIgnoreCase(product.getType());

        if (isService) {
            containerSpecs.setVisibility(View.GONE);
            containerService.setVisibility(View.VISIBLE);

            double basePrice = 0;
            try { basePrice = Double.parseDouble(product.getPrice()); } catch (Exception e) { basePrice = 0; }

            final double pricePerUnit = basePrice;
            currentPrice = pricePerUnit * serviceQuantity;

            tvServicePrice.setText("单价: ¥" + pricePerUnit + "/" + productUnit);
            tvServiceCountDisplay.setText(String.valueOf(serviceQuantity));
            updatePayButton();

            btnServiceAdd.setOnClickListener(v -> {
                serviceQuantity++;
                currentPrice = pricePerUnit * serviceQuantity;
                tvServiceCountDisplay.setText(String.valueOf(serviceQuantity));
                updatePayButton();
            });

        } else {
            containerService.setVisibility(View.GONE);
            containerSpecs.setVisibility(View.VISIBLE);
            loadPhysicalSpecs();
        }

        btnPay.setOnClickListener(v -> {
            if (currentPrice <= 0) {
                Toast.makeText(this, "请选择有效的规格或数量", Toast.LENGTH_SHORT).show();
                return;
            }
            showPaymentMethodDialog();
        });

        bottomSheetDialog.show();
    }

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

                View rowView = LayoutInflater.from(this).inflate(R.layout.item_spec_row, containerSpecs, false);
                TextView tvName = rowView.findViewById(R.id.tv_spec_name);
                TextView tvP = rowView.findViewById(R.id.tv_spec_price);
                LinearLayout llRoot = rowView.findViewById(R.id.ll_spec_row);

                tvName.setText(desc);
                tvP.setText("¥" + priceStr);

                llRoot.setOnClickListener(v -> {
                    for (int j = 0; j < containerSpecs.getChildCount(); j++) {
                        View child = containerSpecs.getChildAt(j);
                        child.findViewById(R.id.ll_spec_row).setBackgroundResource(R.drawable.box_bg);
                        ((TextView) child.findViewById(R.id.tv_spec_name)).setTextColor(Color.BLACK);
                        ((TextView) child.findViewById(R.id.tv_spec_price)).setTextColor(Color.BLACK);
                    }
                    llRoot.setBackgroundResource(R.drawable.border_red);
                    tvName.setTextColor(Color.RED);
                    tvP.setTextColor(Color.RED);

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

    private void showPaymentMethodDialog() {
        final String[] items = {"微信支付", "支付宝"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择支付方式");
        builder.setItems(items, (dialog, which) -> {
            String paymentMethod = items[which];
            simulatePaymentProcess(paymentMethod);
        });
        builder.show();
    }

    private void simulatePaymentProcess(String paymentMethod) {
        Dialog loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        new android.os.Handler().postDelayed(() -> {
            loadingDialog.dismiss();
            createOrder(paymentMethod);
        }, 1500);
    }

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

                boolean isService = "服务".equals(product.getType()) || "SERVICE".equalsIgnoreCase(product.getType());
                if (isService) {
                    order.selectedSpec = "标准服务";
                    order.serviceCount = serviceQuantity;
                } else {
                    order.selectedSpec = selectedSpecStr;
                    order.serviceCount = 1;
                }

                order.payAmount = String.format("%.2f", currentPrice);
                order.paymentMethod = payMethod;
                order.status = "待接单"; // 确保状态符合 MerchantPendingOrderAdapter 的查询逻辑 (pending)
                order.createTime = DateUtils.getCurrentDateTime();

                db.orderDao().insert(order);

                runOnUiThread(() -> {
                    Toast.makeText(this, "支付成功！订单已生成", Toast.LENGTH_SHORT).show();
                    if (bottomSheetDialog != null) bottomSheetDialog.dismiss();
                    Intent intent = new Intent(this, ResidentOrdersActivity.class);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "订单创建失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Order;
import com.gxuwz.ccsa.model.Product;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.util.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ResidentProductDetailActivity extends AppCompatActivity {

    private Product product;
    private AppDatabase db;
    private User currentUser;
    private Dialog bottomSheetDialog;

    // UI Components for Dialog
    private TextView tvTotalPrice;
    private LinearLayout containerSpecs; // For Physical
    private LinearLayout containerService; // For Service
    private TextView tvServicePrice;
    private TextView tvServiceCount;
    private double currentPrice = 0.0;
    private int serviceQuantity = 1;
    private String selectedSpecStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_product_detail.xml);

        db = AppDatabase.getInstance(this);
        long userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getLong("user_id", -1);
        new Thread(() -> currentUser = db.userDao().findById(userId)).start();

        product = (Product) getIntent().getSerializableExtra("product");

        initView();
    }

    private void initView() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        ImageView ivImage = findViewById(R.id.iv_product_image);
        TextView tvName = findViewById(R.id.tv_product_name);
        TextView tvDesc = findViewById(R.id.tv_desc);
        TextView tvPrice = findViewById(R.id.tv_price);

        if (product != null) {
            tvName.setText(product.getName());
            tvDesc.setText(product.getDescription());
            // 简单解析第一张图
            String imgUrl = "";
            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                imgUrl = product.getImageUrls().split(",")[0];
            }
            Glide.with(this).load(imgUrl).placeholder(R.drawable.ic_launcher_background).into(ivImage);

            // 价格显示逻辑根据类型略有不同，暂时显示“查看详情”
            tvPrice.setText("查看价格");
        }

        findViewById(R.id.btn_buy).setOnClickListener(v -> showPurchaseDialog());
    }

    private void showPurchaseDialog() {
        bottomSheetDialog = new Dialog(this, R.style.Theme_AppCompat_Light_Dialog_Alert); // 或自定义样式
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_purchase, null);

        // 设置Dialog参数：底部弹出，宽全屏，高70%
        Window window = bottomSheetDialog.getWindow();
        if (window != null) {
            window.setContentView(view);
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (getResources().getDisplayMetrics().heightPixels * 0.7));
            window.setBackgroundDrawable(new ColorDrawable(Color.WHITE)); // 白色背景
            window.setWindowAnimations(R.style.Animation_Design_BottomSheetDialog); // 默认动画
        }

        // --- Dialog UI 初始化 ---
        TextView tvAddressName = view.findViewById(R.id.tv_address_name);
        TextView tvAddressDetail = view.findViewById(R.id.tv_address_detail);
        ImageView ivThumb = view.findViewById(R.id.iv_thumb);
        TextView tvSheetName = view.findViewById(R.id.tv_sheet_name);
        containerSpecs = view.findViewById(R.id.ll_spec_container);
        containerService = view.findViewById(R.id.ll_service_container);
        tvServicePrice = view.findViewById(R.id.tv_service_base_price);
        tvServiceCount = view.findViewById(R.id.tv_service_count);
        ImageView btnServiceAdd = view.findViewById(R.id.btn_service_add);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);
        Button btnPay = view.findViewById(R.id.btn_pay_now);

        // 填充用户信息
        if (currentUser != null) {
            tvAddressName.setText(currentUser.getName() + " " + currentUser.getPhone());
            tvAddressDetail.setText(currentUser.getCommunityName() + currentUser.getBuilding() + currentUser.getRoomNumber());
        }

        // 填充商品基础信息
        if (product != null) {
            tvSheetName.setText(product.getName());
            String imgUrl = (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) ? product.getImageUrls().split(",")[0] : "";
            Glide.with(this).load(imgUrl).into(ivThumb);
        }

        // 根据类型显示选择区域
        if ("实物".equals(product.getType())) {
            containerService.setVisibility(View.GONE);
            containerSpecs.setVisibility(View.VISIBLE);
            loadPhysicalSpecs(); // 动态加载实物价格表
        } else {
            containerSpecs.setVisibility(View.GONE);
            containerService.setVisibility(View.VISIBLE);
            loadServiceLogic(tvServicePrice, tvServiceCount, btnServiceAdd);
        }

        btnPay.setOnClickListener(v -> handlePayment());

        bottomSheetDialog.show();
    }

    // 模拟加载实物规格表
    private void loadPhysicalSpecs() {
        containerSpecs.removeAllViews();
        try {
            // 假设 price 字段存的是 JSON: [{"spec":"5斤装","price":"50"},{"spec":"10斤装","price":"90"}]
            // 这里为了简化，我们手动模拟解析或者假设字段是用分号分割的 "规格,价格;规格,价格"
            // 若您的数据结构更复杂，请使用 Gson 解析
            // 模拟数据供演示：
            List<String[]> specs = new ArrayList<>();
            if (product.getPrice() != null && product.getPrice().contains(",")) {
                // 简单解析逻辑，实际请根据您的 JSON 结构调整
                // 假设存储格式为简单的 String 描述，这里造一些假数据测试 UI
                specs.add(new String[]{"标准规格", product.getPrice()});
            } else {
                specs.add(new String[]{"小份 (500g)", "20"});
                specs.add(new String[]{"中份 (1000g)", "35"});
                specs.add(new String[]{"大份 (2000g)", "60"});
            }

            for (String[] spec : specs) {
                View itemView = LayoutInflater.from(this).inflate(R.layout.item_spec_row, containerSpecs, false);
                TextView tvSpec = itemView.findViewById(R.id.tv_spec_name);
                TextView tvP = itemView.findViewById(R.id.tv_spec_price);
                LinearLayout llRow = itemView.findViewById(R.id.ll_spec_row);

                tvSpec.setText(spec[0]);
                tvP.setText("¥" + spec[1]);

                llRow.setOnClickListener(v -> {
                    // 重置所有选中状态
                    for (int i = 0; i < containerSpecs.getChildCount(); i++) {
                        View child = containerSpecs.getChildAt(i);
                        child.findViewById(R.id.ll_spec_row).setBackgroundResource(R.drawable.box_bg); // 默认背景
                        ((TextView) child.findViewById(R.id.tv_spec_name)).setTextColor(Color.BLACK);
                        ((TextView) child.findViewById(R.id.tv_spec_price)).setTextColor(Color.BLACK);
                    }
                    // 设置选中
                    llRow.setBackgroundResource(R.drawable.border_red); // 需要创建这个 drawable
                    tvSpec.setTextColor(Color.RED);
                    tvP.setTextColor(Color.RED);

                    // 更新价格
                    currentPrice = Double.parseDouble(spec[1]);
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
            // 服务商品：只能单选（这里理解为一种基础服务，只能加数量）
            double basePrice = 0;
            try { basePrice = Double.parseDouble(product.getPrice()); } catch (Exception e) { basePrice = 50.0; } // 默认值防止崩溃

            final double finalBasePrice = basePrice;
            tvPrice.setText("基础价格: ¥" + finalBasePrice + "/" + (product.getUnit() == null ? "次" : product.getUnit()));

            // 初始化
            serviceQuantity = 1;
            currentPrice = finalBasePrice;
            tvCount.setText("x" + serviceQuantity);
            updatePayButton();

            btnAdd.setOnClickListener(v -> {
                serviceQuantity++;
                currentPrice += finalBasePrice; // 价格累加
                tvCount.setText("x" + serviceQuantity);
                updatePayButton();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePayButton() {
        if (tvTotalPrice != null) {
            tvTotalPrice.setText("立即支付 ¥" + String.format("%.2f", currentPrice));
        }
    }

    private void handlePayment() {
        if (currentPrice <= 0) {
            Toast.makeText(this, "请选择商品规格", Toast.LENGTH_SHORT).show();
            return;
        }

        // 模拟支付过程
        Toast.makeText(this, "正在支付...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                Thread.sleep(1500); // 模拟网络

                Order order = new Order();
                order.orderNo = "ORD" + System.currentTimeMillis();
                order.residentId = String.valueOf(currentUser.getId());
                order.residentName = currentUser.getName();
                order.residentPhone = currentUser.getPhone();
                order.address = currentUser.getCommunityName() + currentUser.getBuilding() + currentUser.getRoomNumber();
                order.merchantId = product.getMerchantId(); // 假设 Product 有 merchantId 字段
                // 如果 Product 没有 merchantId, 需要从数据库关联查询或者传递过来
                // 这里假设 product.getMerchantId() 存在，如果没有请自行添加
                order.productId = String.valueOf(product.getId());
                order.productName = product.getName();
                order.productType = product.getType();
                order.productImageUrl = product.getImageUrls();

                if ("实物".equals(product.getType())) {
                    order.selectedSpec = selectedSpecStr;
                    order.serviceCount = 1;
                } else {
                    order.selectedSpec = "服务";
                    order.serviceCount = serviceQuantity;
                }

                order.payAmount = String.format("%.2f", currentPrice);
                order.status = "待接单";
                order.createTime = DateUtils.getCurrentDateTime();

                db.orderDao().insert(order);

                runOnUiThread(() -> {
                    Toast.makeText(this, "支付成功！", Toast.LENGTH_SHORT).show();
                    if (bottomSheetDialog != null) bottomSheetDialog.dismiss();
                    finish(); // 或者跳转到订单列表
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
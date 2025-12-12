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

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
// 移除了 Gson 引用，因为代码并未真正使用它
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Order;
import com.gxuwz.ccsa.model.Product;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class ResidentProductDetailActivity extends AppCompatActivity {

    private Product product;
    private AppDatabase db;
    private User currentUser;
    private Dialog bottomSheetDialog;

    // UI Components for Dialog
    private Button btnPay; // 修改为使用 Button
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
        setContentView(R.layout.activity_resident_product_detail); // 注意：去除 .xml 后缀

        db = AppDatabase.getInstance(this);
        long userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getLong("user_id", -1);
        new Thread(() -> currentUser = db.userDao().findById(userId)).start(); // 现在 Dao 中有 findById 了

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
            String imgUrl = "";
            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                imgUrl = product.getImageUrls().split(",")[0];
            }
            Glide.with(this).load(imgUrl).placeholder(R.drawable.ic_launcher_background).into(ivImage);
            tvPrice.setText("查看价格");
        }

        findViewById(R.id.btn_buy).setOnClickListener(v -> showPurchaseDialog());
    }

    private void showPurchaseDialog() {
        // 使用 androidx.appcompat.R.style
        bottomSheetDialog = new Dialog(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_purchase, null);

        Window window = bottomSheetDialog.getWindow();
        if (window != null) {
            window.setContentView(view);
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (getResources().getDisplayMetrics().heightPixels * 0.7));
            window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            // 简单起见，如果 Animation 报错可移除下面这行，或者使用系统默认
            window.setWindowAnimations(android.R.style.Animation_Dialog);
        }

        TextView tvAddressName = view.findViewById(R.id.tv_address_name);
        TextView tvAddressDetail = view.findViewById(R.id.tv_address_detail);
        ImageView ivThumb = view.findViewById(R.id.iv_thumb);
        TextView tvSheetName = view.findViewById(R.id.tv_sheet_name);
        containerSpecs = view.findViewById(R.id.ll_spec_container);
        containerService = view.findViewById(R.id.ll_service_container);
        tvServicePrice = view.findViewById(R.id.tv_service_base_price);
        tvServiceCount = view.findViewById(R.id.tv_service_count);
        ImageView btnServiceAdd = view.findViewById(R.id.btn_service_add);

        // 修复：直接获取 Button，不再寻找不存在的 tv_total_price
        btnPay = view.findViewById(R.id.btn_pay_now);

        if (currentUser != null) {
            tvAddressName.setText(currentUser.getName() + " " + currentUser.getPhone());
            tvAddressDetail.setText(currentUser.getCommunityName() + currentUser.getBuilding() + currentUser.getRoomNumber());
        }

        if (product != null) {
            tvSheetName.setText(product.getName());
            String imgUrl = (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) ? product.getImageUrls().split(",")[0] : "";
            Glide.with(this).load(imgUrl).into(ivThumb);
        }

        if ("实物".equals(product.getType())) {
            containerService.setVisibility(View.GONE);
            containerSpecs.setVisibility(View.VISIBLE);
            loadPhysicalSpecs();
        } else {
            containerSpecs.setVisibility(View.GONE);
            containerService.setVisibility(View.VISIBLE);
            loadServiceLogic(tvServicePrice, tvServiceCount, btnServiceAdd);
        }

        btnPay.setOnClickListener(v -> handlePayment());

        bottomSheetDialog.show();
    }

    private void loadPhysicalSpecs() {
        containerSpecs.removeAllViews();
        try {
            List<String[]> specs = new ArrayList<>();
            if (product.getPrice() != null && product.getPrice().contains(",")) {
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
                    for (int i = 0; i < containerSpecs.getChildCount(); i++) {
                        View child = containerSpecs.getChildAt(i);
                        child.findViewById(R.id.ll_spec_row).setBackgroundResource(R.drawable.box_bg);
                        ((TextView) child.findViewById(R.id.tv_spec_name)).setTextColor(Color.BLACK);
                        ((TextView) child.findViewById(R.id.tv_spec_price)).setTextColor(Color.BLACK);
                    }
                    llRow.setBackgroundResource(R.drawable.border_red);
                    tvSpec.setTextColor(Color.RED);
                    tvP.setTextColor(Color.RED);

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
            double basePrice = 0;
            try { basePrice = Double.parseDouble(product.getPrice()); } catch (Exception e) { basePrice = 50.0; }

            final double finalBasePrice = basePrice;
            tvPrice.setText("基础价格: ¥" + finalBasePrice + "/" + (product.getUnit() == null ? "次" : product.getUnit()));

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
            Toast.makeText(this, "请选择商品规格", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "正在支付...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                Thread.sleep(1500);

                Order order = new Order();
                order.orderNo = "ORD" + System.currentTimeMillis();
                order.residentId = String.valueOf(currentUser.getId());
                order.residentName = currentUser.getName();
                order.residentPhone = currentUser.getPhone();
                order.address = currentUser.getCommunityName() + currentUser.getBuilding() + currentUser.getRoomNumber();
                order.merchantId = product.getMerchantId();
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
                    finish();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
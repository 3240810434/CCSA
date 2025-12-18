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
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.gxuwz.ccsa.model.ProductReview; // 新增引入
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

    // 客服按钮引用
    private View btnContactService;

    // 【新增】评价相关控件
    private LinearLayout layoutReviewContainer;
    private TextView tvNoReviews;
    private TextView btnMoreReviews;

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

    // 【新增】每次页面显示时刷新评价（例如从评价列表页或发表评价页返回时）
    @Override
    protected void onResume() {
        super.onResume();
        loadReviewsPreview();
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

        // 绑定客服按钮事件
        btnContactService = findViewById(R.id.ll_contact_service);
        if (btnContactService != null) {
            btnContactService.setOnClickListener(v -> openChat());
        }

        // 【新增】评价模块初始化
        layoutReviewContainer = findViewById(R.id.layout_review_container);
        tvNoReviews = findViewById(R.id.tv_no_reviews);
        btnMoreReviews = findViewById(R.id.btn_more_reviews);

        // 点击更多评价跳转
        btnMoreReviews.setOnClickListener(v -> {
            if (product != null) {
                Intent intent = new Intent(this, ReviewListActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }
        });
    }

    // 【新增】加载最新的两条评价
    private void loadReviewsPreview() {
        if (product == null) return;
        long productId = product.getId();

        new Thread(() -> {
            // 获取最新的2条评论 (需要在 DAO 中实现 getTop2Reviews)
            List<ProductReview> reviews = db.productReviewDao().getTop2Reviews(productId);

            runOnUiThread(() -> {
                if (layoutReviewContainer == null) return;
                layoutReviewContainer.removeAllViews();

                if (reviews == null || reviews.isEmpty()) {
                    tvNoReviews.setVisibility(View.VISIBLE);
                    layoutReviewContainer.addView(tvNoReviews);
                } else {
                    tvNoReviews.setVisibility(View.GONE);
                    // 动态添加 View，复用 item_product_review.xml
                    for (ProductReview review : reviews) {
                        View view = LayoutInflater.from(this).inflate(R.layout.item_product_review, layoutReviewContainer, false);

                        ImageView ivAvatar = view.findViewById(R.id.img_avatar);
                        TextView tvName = view.findViewById(R.id.tv_username);
                        TextView tvContent = view.findViewById(R.id.tv_content);
                        TextView tvTime = view.findViewById(R.id.tv_time);
                        RatingBar rb = view.findViewById(R.id.item_rating);

                        tvName.setText(review.userName);
                        tvContent.setText(review.content);
                        // 注意：这里需要确保 DateUtils 有 formatDateTime 方法，或者自行处理日期格式化
                        tvTime.setText(DateUtils.formatDateTime(review.createTime));
                        rb.setRating(review.score / 2.0f); // 假设 score 是 1-10分，RatingBar 是 5 星

                        Glide.with(this).load(review.userAvatar)
                                .placeholder(R.drawable.ic_avatar) // 确保有默认头像资源
                                .into(ivAvatar);

                        layoutReviewContainer.addView(view);
                    }
                }
            });
        }).start();
    }

    private void openChat() {
        if (currentUser == null) {
            loadData();
            if (currentUser == null) {
                Toast.makeText(this, "正在获取用户信息，请稍后点击...", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (product == null) return;

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("myId", currentUser.getId());
        intent.putExtra("myRole", "RESIDENT");
        intent.putExtra("targetId", product.getMerchantId());
        intent.putExtra("targetRole", "MERCHANT");

        if (productMerchant != null) {
            intent.putExtra("targetName", productMerchant.getMerchantName());
            intent.putExtra("targetAvatar", productMerchant.getAvatar());
        } else {
            intent.putExtra("targetName", "商家");
        }

        startActivity(intent);
    }

    private void loadData() {
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
        if (currentUser == null) {
            long userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getLong("user_id", -1);
            if (userId == -1) {
                Toast.makeText(this, "未检测到登录状态，请先登录", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "正在加载用户信息，请稍候...", Toast.LENGTH_SHORT).show();
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
                order.orderNo = "ORD" + System.currentTimeMillis() + (int)(Math.random() * 1000);
                order.createTime = DateUtils.getCurrentDateTime();
                order.status = "待接单";

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
                order.tags = product.tag;

                boolean isService = "服务".equals(product.getType()) || "SERVICE".equalsIgnoreCase(product.getType());
                if (isService) {
                    order.productType = "服务";
                    order.serviceCount = serviceQuantity;
                    order.productUnit = (product.getUnit() != null && !product.getUnit().isEmpty()) ? product.getUnit() : "次";
                    order.unitPrice = String.format("%.2f", currentPrice / serviceQuantity);
                    order.selectedSpec = "标准服务";
                    order.deliveryMethod = "上门服务";
                } else {
                    order.productType = "实物";
                    order.selectedSpec = selectedSpecStr;
                    order.serviceCount = 1;
                    order.deliveryMethod = product.deliveryMethod == 0 ? "商家配送" : "到店自提";
                    order.productUnit = "份";
                    order.unitPrice = String.format("%.2f", currentPrice);
                }

                order.payAmount = String.format("%.2f", currentPrice);
                order.paymentMethod = payMethod;

                db.orderDao().insert(order);

                runOnUiThread(() -> {
                    Toast.makeText(this, "下单成功！", Toast.LENGTH_SHORT).show();
                    if (bottomSheetDialog != null) bottomSheetDialog.dismiss();

                    Intent intent = new Intent(this, ResidentOrdersActivity.class);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "下单失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
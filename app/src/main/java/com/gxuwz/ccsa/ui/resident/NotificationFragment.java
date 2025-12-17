package com.gxuwz.ccsa.ui.resident;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.BannerAdapter;
import com.gxuwz.ccsa.adapter.ProductAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Product;
import com.gxuwz.ccsa.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationFragment extends Fragment {
    // 轮播图相关
    private ViewPager2 viewPager;
    private LinearLayout indicatorLayout;
    private BannerAdapter bannerAdapter;
    private List<String> bannerImages = new ArrayList<>();
    private int currentPage = 0;
    private Timer timer;
    private User currentUser;

    // 商品展示相关
    private RecyclerView rvProducts;
    private ProductAdapter productAdapter;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> displayProducts = new ArrayList<>();
    private Timer productTimer;
    private int productDisplayIndex = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        // 获取当前用户信息
        if (getActivity() instanceof ResidentMainActivity) {
            currentUser = ((ResidentMainActivity) getActivity()).getUser();
        }

        initBanner(view);
        initFunctionButtons(view);
        initProductSection(view);

        return view;
    }

    private void initBanner(View view) {
        viewPager = view.findViewById(R.id.viewPager);
        indicatorLayout = view.findViewById(R.id.indicatorLayout);

        String packageName = requireContext().getPackageName();
        bannerImages.clear();
        bannerImages.add("android.resource://" + packageName + "/" + R.drawable.banner1);
        bannerImages.add("android.resource://" + packageName + "/" + R.drawable.banner2);
        bannerImages.add("android.resource://" + packageName + "/" + R.drawable.banner3);

        bannerAdapter = new BannerAdapter(getContext(), bannerImages);
        viewPager.setAdapter(bannerAdapter);

        addIndicators();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;
                updateIndicators();
            }
        });

        startAutoSlide();
    }

    private void addIndicators() {
        indicatorLayout.removeAllViews();
        for (int i = 0; i < bannerImages.size(); i++) {
            ImageView indicator = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(4, 0, 4, 0);
            indicator.setLayoutParams(params);
            indicator.setImageResource(R.drawable.dot_indicator);
            indicatorLayout.addView(indicator);
        }
        updateIndicators();
    }

    private void updateIndicators() {
        for (int i = 0; i < indicatorLayout.getChildCount(); i++) {
            ImageView indicator = (ImageView) indicatorLayout.getChildAt(i);
            if (i == currentPage) {
                indicator.setImageResource(R.drawable.dot_indicator_selected);
            } else {
                indicator.setImageResource(R.drawable.dot_indicator);
            }
        }
    }

    private void startAutoSlide() {
        if (timer != null) timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (bannerImages.size() > 0) {
                            currentPage = (currentPage + 1) % bannerImages.size();
                            viewPager.setCurrentItem(currentPage);
                        }
                    });
                }
            }
        }, 3000, 3000);
    }

    private void initProductSection(View view) {
        rvProducts = view.findViewById(R.id.rv_products);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        rvProducts.setLayoutManager(layoutManager);

        productAdapter = new ProductAdapter(getContext(), displayProducts);
        rvProducts.setAdapter(productAdapter);

        loadProducts();
    }

    // --- 修改点：加载商品时根据用户小区筛选 ---
    private void loadProducts() {
        new Thread(() -> {
            if (getContext() == null) return;
            AppDatabase db = AppDatabase.getInstance(getContext());

            List<Product> products;
            // 如果用户有小区信息，则只查询该小区商家的商品
            if (currentUser != null && !TextUtils.isEmpty(currentUser.getCommunity())) {
                products = db.productDao().getProductsByCommunity(currentUser.getCommunity());
            } else {
                // 如果没有用户信息（例如未完全登录或未绑定小区），这里选择显示所有或者不显示
                // 为了演示效果默认显示所有，实际可根据需求改为 new ArrayList<>()
                products = db.productDao().getAllProducts();
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    allProducts.clear();
                    if (products != null) {
                        allProducts.addAll(products);
                    }
                    updateDisplayedProducts();
                    startProductRefreshTimer();
                });
            }
        }).start();
    }

    private void startProductRefreshTimer() {
        if (productTimer != null) {
            productTimer.cancel();
            productTimer = null;
        }
        if (allProducts.size() <= 2) return;

        productTimer = new Timer();
        productTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        productDisplayIndex = (productDisplayIndex + 2) % allProducts.size();
                        updateDisplayedProducts();
                    });
                }
            }
        }, 5000, 5000);
    }

    private void updateDisplayedProducts() {
        displayProducts.clear();
        if (allProducts.isEmpty()) {
            productAdapter.notifyDataSetChanged();
            return;
        }

        if (allProducts.size() <= 2) {
            displayProducts.addAll(allProducts);
        } else {
            displayProducts.add(allProducts.get(productDisplayIndex % allProducts.size()));
            int secondIndex = (productDisplayIndex + 1) % allProducts.size();
            displayProducts.add(allProducts.get(secondIndex));
        }
        productAdapter.notifyDataSetChanged();
    }

    private void initFunctionButtons(View view) {
        view.findViewById(R.id.ll_notice).setOnClickListener(v -> {
            if (checkUser()) {
                Intent intent = new Intent(getContext(), NotificationActivity.class);
                intent.putExtra("user", currentUser);
                startActivity(intent);
            }
        });
        view.findViewById(R.id.ll_vote).setOnClickListener(v -> {
            if (checkUser()) {
                Intent intent = new Intent(getContext(), ResidentVoteActivity.class);
                intent.putExtra("user", currentUser);
                startActivity(intent);
            }
        });
        view.findViewById(R.id.ll_property_pay).setOnClickListener(v -> {
            if (checkUser()) {
                Intent intent = new Intent(getContext(), PayPropertyFeeActivity.class);
                intent.putExtra("user", currentUser);
                startActivity(intent);
            }
        });

        // 修改了这里：跳转到 PaymentDashboardActivity (有图的缴费页面)
        view.findViewById(R.id.ll_my_payment).setOnClickListener(v -> {
            if (checkUser()) {
                Intent intent = new Intent(getContext(), PaymentDashboardActivity.class);
                intent.putExtra("user", currentUser);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.ll_appeal).setOnClickListener(v -> {
            if (checkUser()) {
                Intent intent = new Intent(getContext(), PaymentAppealActivity.class);
                intent.putExtra("user", currentUser);
                startActivity(intent);
            }
        });
        view.findViewById(R.id.ll_contact_property).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ContactPropertyActivity.class);
            startActivity(intent);
        });
        view.findViewById(R.id.ll_repair).setOnClickListener(v -> {
            if (checkUser()) {
                Intent intent = new Intent(getContext(), RepairActivity.class);
                intent.putExtra("user", currentUser);
                startActivity(intent);
            }
        });
        view.findViewById(R.id.ll_more).setOnClickListener(v -> showMoreServiceDialog());

        TextView tvMerchantMore = view.findViewById(R.id.tv_merchant_more);
        if (tvMerchantMore != null) {
            tvMerchantMore.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ResidentProductBrowsingActivity.class);
                startActivity(intent);
            });
        }
    }

    private boolean checkUser() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "用户信息获取失败，请重新登录", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showMoreServiceDialog() {
        if (getContext() == null) return;
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_more_service);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        View ivProductService = dialog.findViewById(R.id.iv_product_service);
        View tvProductService = dialog.findViewById(R.id.tv_product_service);
        View.OnClickListener jumpListener = v -> {
            dialog.dismiss();
            Intent intent = new Intent(getContext(), ResidentProductBrowsingActivity.class);
            startActivity(intent);
        };
        if (ivProductService != null) ivProductService.setOnClickListener(jumpListener);
        if (tvProductService != null) tvProductService.setOnClickListener(jumpListener);
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timer != null) { timer.cancel(); timer = null; }
        if (productTimer != null) { productTimer.cancel(); productTimer = null; }
    }
}
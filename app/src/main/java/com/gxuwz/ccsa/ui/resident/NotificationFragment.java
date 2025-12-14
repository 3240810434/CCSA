package com.gxuwz.ccsa.ui.resident;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
    private Timer timer; // 轮播图定时器
    private User currentUser;

    // 商品展示相关
    private RecyclerView rvProducts;
    private ProductAdapter productAdapter;
    private List<Product> allProducts = new ArrayList<>();    // 所有的商品列表
    private List<Product> displayProducts = new ArrayList<>(); // 当前显示的2个商品
    private Timer productTimer; // 商品刷新定时器
    private int productDisplayIndex = 0; // 当前展示的商品索引指针

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        // 获取当前用户信息
        if (getActivity() instanceof ResidentMainActivity) {
            currentUser = ((ResidentMainActivity) getActivity()).getUser();
        }

        // 初始化各个模块
        initBanner(view);
        initFunctionButtons(view);
        initProductSection(view);

        return view;
    }

    // 初始化轮播图
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

    // 添加轮播图指示器
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
        if (timer != null) {
            timer.cancel();
        }
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

    // --- 初始化商品展示区域 ---
    private void initProductSection(View view) {
        rvProducts = view.findViewById(R.id.rv_products);
        // 设置两列网格布局
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        rvProducts.setLayoutManager(layoutManager);

        // 初始化适配器
        productAdapter = new ProductAdapter(getContext(), displayProducts);
        rvProducts.setAdapter(productAdapter);

        // 加载数据
        loadProducts();
    }

    // --- 从数据库加载所有商品 ---
    private void loadProducts() {
        new Thread(() -> {
            if (getContext() == null) return;
            AppDatabase db = AppDatabase.getInstance(getContext());
            // 获取所有商品
            List<Product> products = db.productDao().getAllProducts();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    allProducts.clear();
                    allProducts.addAll(products);

                    // 初始展示
                    updateDisplayedProducts();

                    // 启动刷新定时器
                    startProductRefreshTimer();
                });
            }
        }).start();
    }

    // --- 启动商品刷新定时器 ---
    private void startProductRefreshTimer() {
        // 先取消旧的
        if (productTimer != null) {
            productTimer.cancel();
            productTimer = null;
        }

        // 如果商品总数小于等于2，不刷新，直接停止
        if (allProducts.size() <= 2) {
            return;
        }

        productTimer = new Timer();
        // 【修改点】：延迟6秒后开始执行，每隔6秒执行一次 (6000ms)
        productTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        // 移动指针，循环逻辑
                        productDisplayIndex = (productDisplayIndex + 2) % allProducts.size();
                        updateDisplayedProducts();
                    });
                }
            }
        }, 6000, 6000);
    }

    // --- 更新当前展示的商品列表 ---
    private void updateDisplayedProducts() {
        displayProducts.clear();

        if (allProducts.isEmpty()) {
            // 没有商品，留空
            productAdapter.notifyDataSetChanged();
            return;
        }

        if (allProducts.size() <= 2) {
            // 小于等于2个，全部显示
            displayProducts.addAll(allProducts);
        } else {
            // 大于2个，取2个
            // 第一个商品
            displayProducts.add(allProducts.get(productDisplayIndex % allProducts.size()));

            // 第二个商品 (防止越界，取模)
            int secondIndex = (productDisplayIndex + 1) % allProducts.size();
            displayProducts.add(allProducts.get(secondIndex));
        }

        productAdapter.notifyDataSetChanged();
    }

    // 初始化功能按钮点击事件
    private void initFunctionButtons(View view) {
        // 1. 通知公告
        view.findViewById(R.id.ll_notice).setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(getContext(), "用户信息获取失败，请重新登录", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getContext(), NotificationActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });

        // 2. 小区投票
        view.findViewById(R.id.ll_vote).setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(getContext(), "用户信息获取失败，请重新登录", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getContext(), ResidentVoteActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });

        // 3. 物业缴费
        view.findViewById(R.id.ll_property_pay).setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(getContext(), "用户信息获取失败，请重新登录", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getContext(), PayPropertyFeeActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });

        // 4. 我的缴费
        view.findViewById(R.id.ll_my_payment).setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(getContext(), "用户信息获取失败，请重新登录", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getContext(), PaymentDetailActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });

        // 5. 缴费申诉
        view.findViewById(R.id.ll_appeal).setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(getContext(), "用户信息获取失败，请重新登录", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getContext(), PaymentAppealActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });

        // 6. 联系物业
        view.findViewById(R.id.ll_contact_property).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ContactPropertyActivity.class);
            startActivity(intent);
        });

        // 7. 报修
        view.findViewById(R.id.ll_repair).setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(getContext(), "用户信息获取失败，请重新登录", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getContext(), RepairActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });

        // 8. 更多按钮
        view.findViewById(R.id.ll_more).setOnClickListener(v -> showMoreServiceDialog());

        // 9. 周边商家 - 更多
        TextView tvMerchantMore = view.findViewById(R.id.tv_merchant_more);
        if (tvMerchantMore != null) {
            tvMerchantMore.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ResidentProductBrowsingActivity.class);
                startActivity(intent);
            });
        }
    }

    // 显示更多服务弹窗
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
        // 停止轮播图定时器
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        // 停止商品刷新定时器
        if (productTimer != null) {
            productTimer.cancel();
            productTimer = null;
        }
    }
}
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
import androidx.viewpager2.widget.ViewPager2;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.BannerAdapter;
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
    // 修正1：将 List<Integer> 改为 List<String> 以匹配新的 Adapter
    private List<String> bannerImages = new ArrayList<>();
    private int currentPage = 0;
    private Timer timer;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        // 获取当前用户信息
        if (getActivity() instanceof ResidentMainActivity) {
            currentUser = ((ResidentMainActivity) getActivity()).getUser();
        }

        // 初始化轮播图和功能按钮
        initBanner(view);
        initFunctionButtons(view);

        return view;
    }

    // 初始化轮播图
    private void initBanner(View view) {
        viewPager = view.findViewById(R.id.viewPager);
        indicatorLayout = view.findViewById(R.id.indicatorLayout);

        // 修正2：将资源ID转换为 String 路径
        // 格式：android.resource://包名/资源ID
        String packageName = requireContext().getPackageName();

        bannerImages.clear(); // 避免重复添加
        bannerImages.add("android.resource://" + packageName + "/" + R.drawable.banner1);
        bannerImages.add("android.resource://" + packageName + "/" + R.drawable.banner2);
        bannerImages.add("android.resource://" + packageName + "/" + R.drawable.banner3);

        bannerAdapter = new BannerAdapter(getContext(), bannerImages);
        viewPager.setAdapter(bannerAdapter);

        // 添加指示器
        addIndicators();

        // 设置页面切换监听
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;
                updateIndicators();
            }
        });

        // 自动轮播
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

    // 更新指示器状态
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

    // 启动自动轮播
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
        }, 3000, 3000); // 每3秒切换一次
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
        // 停止自动轮播
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
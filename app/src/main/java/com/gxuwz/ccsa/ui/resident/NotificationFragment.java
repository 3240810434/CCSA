package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import android.content.Intent;

public class NotificationFragment extends Fragment {
    // 轮播图相关
    private ViewPager2 viewPager;
    private LinearLayout indicatorLayout;
    private BannerAdapter bannerAdapter;
    private List<Integer> bannerImages = new ArrayList<>();
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

        // 添加轮播图资源
        bannerImages.add(R.drawable.banner1);
        bannerImages.add(R.drawable.banner2);
        bannerImages.add(R.drawable.banner3);

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
                        currentPage = (currentPage + 1) % bannerImages.size();
                        viewPager.setCurrentItem(currentPage);
                    });
                }
            }
        }, 3000, 3000); // 每3秒切换一次
    }

    // 初始化功能按钮点击事件
    private void initFunctionButtons(View view) {
        // 通知公告按钮：跳转到通知页面（修复部分）
        view.findViewById(R.id.ll_notice).setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(getContext(), "用户信息获取失败，请重新登录", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getContext(), NotificationActivity.class);
            intent.putExtra("user", currentUser); // 传递用户信息
            startActivity(intent);
        });

        // 小区投票按钮：跳转到小区投票页面
        view.findViewById(R.id.ll_vote).setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(getContext(), "用户信息获取失败，请重新登录", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getContext(), ResidentVoteActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });

        // 物业缴费按钮：跳转到在线缴纳物业费页面
        view.findViewById(R.id.ll_property_pay).setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(getContext(), "用户信息获取失败，请重新登录", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getContext(), PayPropertyFeeActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });

        // 我的缴费按钮：跳转到查看缴费明细页面
        view.findViewById(R.id.ll_my_payment).setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(getContext(), "用户信息获取失败，请重新登录", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getContext(), PaymentDetailActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });

        // 缴费申诉按钮：跳转到缴费异常申诉页面
        view.findViewById(R.id.ll_appeal).setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(getContext(), "用户信息获取失败，请重新登录", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getContext(), PaymentAppealActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });

        // 联系物业按钮：跳转到联系物业页面
        view.findViewById(R.id.ll_contact_property).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ContactPropertyActivity.class);
            startActivity(intent);
        });

        // 报修按钮：跳转到报修页面（已修改：添加用户信息传递和判空）
        view.findViewById(R.id.ll_repair).setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(getContext(), "用户信息获取失败，请重新登录", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getContext(), RepairActivity.class);
            intent.putExtra("user", currentUser); // 添加用户信息传递
            startActivity(intent);
        });

        // 更多按钮
        view.findViewById(R.id.ll_more).setOnClickListener(v ->
                Toast.makeText(getContext(), "更多功能", Toast.LENGTH_SHORT).show()
        );
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
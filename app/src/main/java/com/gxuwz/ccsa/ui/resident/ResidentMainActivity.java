package com.gxuwz.ccsa.ui.resident;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.User;

public class ResidentMainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnNotification, btnDynamic, btnMine;
    private ViewPager2 viewPager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_main);

        // 从Intent获取用户信息
        currentUser = (User) getIntent().getSerializableExtra("user");

        initViews();
        setupViewPager();
    }

    // 提供getter方法获取当前用户，供Fragment调用
    public User getUser() {
        return currentUser;
    }

    private void initViews() {
        btnNotification = findViewById(R.id.btn_notification);
        btnDynamic = findViewById(R.id.btn_dynamic);
        btnMine = findViewById(R.id.btn_mine);
        viewPager = findViewById(R.id.view_pager_main);

        btnNotification.setOnClickListener(this);
        btnDynamic.setOnClickListener(this);
        btnMine.setOnClickListener(this);
    }

    private void setupViewPager() {
        // 设置 ViewPager2 的适配器
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        return new NotificationFragment(); // 服务/主页
                    case 1:
                        return new DynamicFragment(); // 动态（内部包含生活动态和邻里互助的ViewPager）
                    default:
                        return new MineFragment(); // 我的
                }
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        });

        // 注册页面变动监听，滑动时更新底部导航栏状态
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateBottomNavigation(position);
            }
        });

        // 设置离屏加载数量，保持页面状态，避免滑动回来时重新刷新
        viewPager.setOffscreenPageLimit(2);

        // 默认不禁用滑动，ViewPager2 能够自动处理嵌套滑动（即DynamicFragment内部的ViewPager滑动到头后会自动触发外层滑动）
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        // 点击底部按钮切换页面
        // 第二个参数 false 表示点击时不使用平滑滚动动画，直接跳转，体验更像传统Tab切换
        if (id == R.id.btn_notification) {
            viewPager.setCurrentItem(0, false);
        } else if (id == R.id.btn_dynamic) {
            viewPager.setCurrentItem(1, false);
        } else if (id == R.id.btn_mine) {
            viewPager.setCurrentItem(2, false);
        }
    }

    // 更新底部导航选中状态
    private void updateBottomNavigation(int position) {
        // 重置所有按钮状态
        btnNotification.setSelected(false);
        btnDynamic.setSelected(false);
        btnMine.setSelected(false);

        // 设置当前选中按钮
        switch (position) {
            case 0:
                btnNotification.setSelected(true);
                break;
            case 1:
                btnDynamic.setSelected(true);
                break;
            case 2:
                btnMine.setSelected(true);
                break;
        }
    }
}
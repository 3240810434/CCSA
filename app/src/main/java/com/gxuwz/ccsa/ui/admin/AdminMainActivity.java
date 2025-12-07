package com.gxuwz.ccsa.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.AdminPagerAdapter;

public class AdminMainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;
    private String adminAccount; // 管理员账号
    private String community; // 负责的小区

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        // 1. 从登录页面获取并验证传递的参数
        adminAccount = getIntent().getStringExtra("adminAccount");
        community = getIntent().getStringExtra("community");

        // 参数校验与兜底处理
        if (adminAccount == null || adminAccount.isEmpty()) {
            adminAccount = "admin"; // 临时默认值，实际应从登录逻辑严格获取
        }
        if (community == null || community.isEmpty()) {
            community = "未知小区"; // 兜底值，避免空指针
        }

        // 2. 初始化控件
        initViews();
        // 3. 设置ViewPager2及适配器（传递完整参数）
        setupViewPager();
        // 4. 设置底部导航与ViewPager联动
        setupNavigationListener();
    }

    private void initViews() {
        viewPager = findViewById(R.id.view_pager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 移除对不存在控件btn_resident_repair的引用
    }

    private void setupViewPager() {
        // 初始化适配器并传递必要参数
        AdminPagerAdapter adapter = new AdminPagerAdapter(this, adminAccount, community);
        viewPager.setAdapter(adapter);

        // 禁止ViewPager滑动（可选，根据需求）
        viewPager.setUserInputEnabled(false);

        // 监听ViewPager页面变化，同步底部导航选中状态
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
            }
        });
    }

    private void setupNavigationListener() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_manage) {
                    viewPager.setCurrentItem(0); // 管理页面（AdminManageFragment）
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    viewPager.setCurrentItem(1); // 个人中心页面
                    return true;
                }
                return false;
            }
        });
    }
}

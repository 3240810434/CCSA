package com.gxuwz.ccsa.ui.admin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
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

        // 1. 获取参数
        adminAccount = getIntent().getStringExtra("adminAccount");
        community = getIntent().getStringExtra("community");

        if (adminAccount == null) adminAccount = "admin";
        if (community == null) community = "未知小区";

        // 保存管理员信息到 SharedPreferences，方便其他页面（如审核列表）获取
        saveAdminInfo();

        // 2. 初始化控件
        initViews();
        // 3. 设置ViewPager
        setupViewPager();
        // 4. 设置监听
        setupNavigationListener();
    }

    private void saveAdminInfo() {
        SharedPreferences sp = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("admin_account", adminAccount);
        editor.putString("admin_community", community);
        editor.apply();
    }

    private void initViews() {
        viewPager = findViewById(R.id.view_pager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupViewPager() {
        AdminPagerAdapter adapter = new AdminPagerAdapter(this, adminAccount, community);
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false); // 禁止滑动，防止手势冲突

        // 联动底部导航栏状态
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
                    viewPager.setCurrentItem(0);
                    return true;
                } else if (itemId == R.id.nav_message) {
                    viewPager.setCurrentItem(1); // 切换到信息页面
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    viewPager.setCurrentItem(2); // 切换到我的页面
                    return true;
                }
                return false;
            }
        });
    }
}
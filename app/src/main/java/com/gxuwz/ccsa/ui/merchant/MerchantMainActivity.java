package com.gxuwz.ccsa.ui.merchant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.MerchantPagerAdapter;
import com.gxuwz.ccsa.model.Merchant;

public class MerchantMainActivity extends AppCompatActivity implements View.OnClickListener {

    private ViewPager2 viewPager;
    private TextView btnStore, btnMessage, btnMine;
    private Merchant currentMerchant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_main);

        // 获取传递的商家信息
        currentMerchant = (Merchant) getIntent().getSerializableExtra("merchant");

        initViews();
        setupViewPager();
    }

    private void initViews() {
        viewPager = findViewById(R.id.view_pager);
        btnStore = findViewById(R.id.btn_nav_store);
        btnMessage = findViewById(R.id.btn_nav_message);
        btnMine = findViewById(R.id.btn_nav_mine);

        btnStore.setOnClickListener(this);
        btnMessage.setOnClickListener(this);
        btnMine.setOnClickListener(this);

        // 默认选中第一个
        updateBottomNavState(0);
    }

    private void setupViewPager() {
        MerchantPagerAdapter adapter = new MerchantPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // 注册页面滑动回调
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // 滑动页面时同步更新底部按钮状态
                updateBottomNavState(position);
            }
        });

        // 禁用预加载，或者根据需要设置
        viewPager.setOffscreenPageLimit(3);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_nav_store) {
            viewPager.setCurrentItem(0, true); // true 表示带平滑滚动动画
        } else if (id == R.id.btn_nav_message) {
            viewPager.setCurrentItem(1, true);
        } else if (id == R.id.btn_nav_mine) {
            viewPager.setCurrentItem(2, true);
        }
    }

    private void updateBottomNavState(int position) {
        // 利用 View 的 selected 状态配合 selector xml 改变颜色
        btnStore.setSelected(position == 0);
        btnMessage.setSelected(position == 1);
        btnMine.setSelected(position == 2);
    }

    // 提供给 Fragment 获取当前商家信息的方法
    public Merchant getCurrentMerchant() {
        return currentMerchant;
    }
}
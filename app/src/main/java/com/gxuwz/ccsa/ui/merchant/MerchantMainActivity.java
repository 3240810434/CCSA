package com.gxuwz.ccsa.ui.merchant;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.Merchant;

public class MerchantMainActivity extends AppCompatActivity implements View.OnClickListener {

    private ViewPager2 viewPager;
    // 底部导航按钮 (注意：XML中需保证这些ID存在)
    private View btnStore, btnMessage, btnProfile;
    private Merchant currentMerchant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_main);

        // 获取登录/注册传递过来的商家信息
        if (getIntent().hasExtra("merchant")) {
            currentMerchant = (Merchant) getIntent().getSerializableExtra("merchant");
        }

        initViews();
        setupViewPager();
    }

    private void initViews() {
        viewPager = findViewById(R.id.view_pager_merchant);

        // 绑定底部导航栏的点击区域 (id 需与 merchant_navigation_bottom.xml 一致)
        btnStore = findViewById(R.id.btn_store);
        btnMessage = findViewById(R.id.btn_message);
        btnProfile = findViewById(R.id.btn_profile);

        btnStore.setOnClickListener(this);
        btnMessage.setOnClickListener(this);
        btnProfile.setOnClickListener(this);
    }

    private void setupViewPager() {
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        // 首页展示店铺 Fragment
                        return new MerchantStoreFragment();
                    case 1:
                        return new MerchantMessageFragment();
                    default:
                        return new MerchantProfileFragment();
                }
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        });

        // 页面切换回调
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateBottomNavState(position);
            }
        });

        // 设置预加载页面数量，防止频繁销毁重建
        viewPager.setOffscreenPageLimit(3);
        // 默认选中第一页
        updateBottomNavState(0);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_store) {
            viewPager.setCurrentItem(0, false);
        } else if (id == R.id.btn_message) {
            viewPager.setCurrentItem(1, false);
        } else if (id == R.id.btn_profile) {
            viewPager.setCurrentItem(2, false);
        }
    }

    private void updateBottomNavState(int position) {
        // 更新底部导航选中状态
        if (btnStore != null) btnStore.setSelected(false);
        if (btnMessage != null) btnMessage.setSelected(false);
        if (btnProfile != null) btnProfile.setSelected(false);

        switch (position) {
            case 0:
                if (btnStore != null) btnStore.setSelected(true);
                break;
            case 1:
                if (btnMessage != null) btnMessage.setSelected(true);
                break;
            case 2:
                if (btnProfile != null) btnProfile.setSelected(true);
                break;
        }
    }

    // 供 Fragment 调用的公共方法
    public Merchant getCurrentMerchant() {
        return currentMerchant;
    }
}
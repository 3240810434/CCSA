package com.gxuwz.ccsa.ui.merchant;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.Merchant;

public class MerchantMainActivity extends AppCompatActivity implements View.OnClickListener {

    private ViewPager2 viewPager;
    private TextView btnStore, btnMessage, btnProfile;
    private Merchant currentMerchant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_main);

        // 获取登录/注册传递的商家信息
        if (getIntent().hasExtra("merchant")) {
            currentMerchant = (Merchant) getIntent().getSerializableExtra("merchant");
        }

        initViews();
        setupViewPager();
    }

    private void initViews() {
        viewPager = findViewById(R.id.view_pager_merchant);
        // 绑定底部导航按钮
        btnStore = findViewById(R.id.btn_store);
        btnMessage = findViewById(R.id.btn_message);
        btnProfile = findViewById(R.id.btn_profile);

        // 设置点击监听
        btnStore.setOnClickListener(this);
        btnMessage.setOnClickListener(this);
        btnProfile.setOnClickListener(this);
    }

    private void setupViewPager() {
        // 设置 Adapter
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        return new MerchantStoreFragment(); // 1. 店铺页面
                    case 1:
                        return new MerchantMessageFragment(); // 2. 消息页面
                    default:
                        return new MerchantProfileFragment(); // 3. 我的页面
                }
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        });

        // 页面滑动监听，同步底部按钮状态
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateBottomNavState(position);
            }
        });

        // 默认禁止预加载太多，但保持状态
        viewPager.setOffscreenPageLimit(3);

        // 初始化默认选中第一项（店铺页面）
        updateBottomNavState(0);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_store) {
            viewPager.setCurrentItem(0, false); // false 表示去掉滑动动画，跳转更干脆
        } else if (id == R.id.btn_message) {
            viewPager.setCurrentItem(1, false);
        } else if (id == R.id.btn_profile) {
            viewPager.setCurrentItem(2, false);
        }
    }

    /**
     * 更新底部导航栏的选中状态（改变图标颜色或文字颜色）
     */
    private void updateBottomNavState(int position) {
        // 重置所有按钮选中状态 (依赖 selector_nav_text_color 或类似的 selector)
        btnStore.setSelected(false);
        btnMessage.setSelected(false);
        btnProfile.setSelected(false);

        // 设置当前选中
        switch (position) {
            case 0:
                btnStore.setSelected(true);
                break;
            case 1:
                btnMessage.setSelected(true);
                break;
            case 2:
                btnProfile.setSelected(true);
                break;
        }
    }

    // 公开方法供 Fragment 获取当前商家信息
    public Merchant getCurrentMerchant() {
        return currentMerchant;
    }
}
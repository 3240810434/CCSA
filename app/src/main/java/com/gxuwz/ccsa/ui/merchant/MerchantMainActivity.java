package com.gxuwz.ccsa.ui.merchant;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView; // 也可以删掉这个导入，或者保留
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.Merchant;

public class MerchantMainActivity extends AppCompatActivity implements View.OnClickListener {

    private ViewPager2 viewPager;
    // 【修改点1】将类型从 TextView 改为 View，因为它们现在是 LinearLayout 容器
    private View btnStore, btnMessage, btnProfile;
    private Merchant currentMerchant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_main);

        if (getIntent().hasExtra("merchant")) {
            currentMerchant = (Merchant) getIntent().getSerializableExtra("merchant");
        }

        initViews();
        setupViewPager();
    }

    private void initViews() {
        viewPager = findViewById(R.id.view_pager_merchant);

        // 绑定底部导航按钮（容器）
        btnStore = findViewById(R.id.btn_store);
        btnMessage = findViewById(R.id.btn_message);
        btnProfile = findViewById(R.id.btn_profile);

        // 设置点击监听
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

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateBottomNavState(position);
            }
        });

        viewPager.setOffscreenPageLimit(3);
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
        // 【修改点2】setSelected 方法在 View 类中就有，所以这里直接调用即可
        btnStore.setSelected(false);
        btnMessage.setSelected(false);
        btnProfile.setSelected(false);

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

    public Merchant getCurrentMerchant() {
        return currentMerchant;
    }
}
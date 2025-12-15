package com.gxuwz.ccsa.ui.merchant;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Merchant;
import java.util.concurrent.Executors;

public class MerchantMainActivity extends AppCompatActivity implements View.OnClickListener {

    private ViewPager2 viewPager;
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

    @Override
    protected void onResume() {
        super.onResume();
        // 每次页面显示时，从数据库刷新最新的商家状态
        // 确保资质认证状态更新后，商家能立即获得权限
        reloadMerchantData();
    }

    private void reloadMerchantData() {
        if (currentMerchant == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            Merchant freshMerchant = AppDatabase.getInstance(this)
                    .merchantDao()
                    .findById(currentMerchant.getId());

            if (freshMerchant != null) {
                currentMerchant = freshMerchant;
            }
        });
    }

    private void initViews() {
        viewPager = findViewById(R.id.view_pager_merchant);

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

    public Merchant getCurrentMerchant() {
        return currentMerchant;
    }

    public void setCurrentMerchant(Merchant merchant) {
        this.currentMerchant = merchant;
    }
}
package com.gxuwz.ccsa.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.gxuwz.ccsa.ui.merchant.MerchantMessageFragment;
import com.gxuwz.ccsa.ui.merchant.MerchantMineFragment;
import com.gxuwz.ccsa.ui.merchant.MerchantStoreFragment;

import java.util.ArrayList;
import java.util.List;

public class MerchantPagerAdapter extends FragmentStateAdapter {

    private final List<Fragment> fragments = new ArrayList<>();

    public MerchantPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        // 初始化三个页面
        fragments.add(new MerchantStoreFragment());
        fragments.add(new MerchantMessageFragment());
        fragments.add(new MerchantMineFragment());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
}

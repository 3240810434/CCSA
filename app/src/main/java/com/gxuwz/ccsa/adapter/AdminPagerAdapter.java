package com.gxuwz.ccsa.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.gxuwz.ccsa.ui.admin.AdminManageFragment;
import com.gxuwz.ccsa.ui.admin.ProfileFragment;

public class AdminPagerAdapter extends FragmentStateAdapter {

    private String adminAccount;
    private String community;

    public AdminPagerAdapter(@NonNull FragmentActivity fragmentActivity, String adminAccount, String community) {
        super(fragmentActivity);
        this.adminAccount = adminAccount;
        this.community = community;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                // 修复：使用AdminManageFragment并传递完整参数
                return AdminManageFragment.newInstance(community, adminAccount);
            case 1:
                ProfileFragment profileFragment = new ProfileFragment();
                profileFragment.getArguments().putString("adminAccount", adminAccount);
                return profileFragment;
            default:
                return AdminManageFragment.newInstance(community, adminAccount);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
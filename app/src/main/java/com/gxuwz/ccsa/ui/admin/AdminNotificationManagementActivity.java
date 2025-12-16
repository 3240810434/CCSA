package com.gxuwz.ccsa.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.gxuwz.ccsa.R;

public class AdminNotificationManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notification_management);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        findViewById(R.id.tv_new_publish).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminNoticeEditActivity.class));
        });

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager = findViewById(R.id.view_pager);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                // 0: 已发布列表, 1: 草稿箱
                return AdminNoticeListFragment.newInstance(position == 0);
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "已发布通知" : "草稿箱");
        }).attach();
    }
}
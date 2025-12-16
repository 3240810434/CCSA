package com.gxuwz.ccsa.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.gxuwz.ccsa.R;

public class VoteManagementActivity extends AppCompatActivity {
    private String community;
    private String adminAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_management);

        community = getIntent().getStringExtra("community");
        adminAccount = getIntent().getStringExtra("adminAccount");

        // 初始化新建按钮
        findViewById(R.id.btn_add_vote).setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateVoteActivity.class);
            intent.putExtra("community", community);
            intent.putExtra("adminAccount", adminAccount);
            startActivity(intent);
        });

        // 初始化Tab和ViewPager
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager = findViewById(R.id.view_pager);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @Override
            public Fragment createFragment(int position) {
                // status: 1=Published, 0=Draft
                return VoteListFragment.newInstance(community, position == 0 ? 1 : 0, true);
            }
            @Override
            public int getItemCount() { return 2; }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "已发布" : "草稿箱");
        }).attach();

        // 返回按钮
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
    }
}
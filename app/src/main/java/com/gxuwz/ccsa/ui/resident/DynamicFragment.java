package com.gxuwz.ccsa.ui.resident;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gxuwz.ccsa.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 居民端-动态主页面
 * 包含"生活动态"和"邻里互助"两个子页面，支持滑动切换
 */
public class DynamicFragment extends Fragment {

    private TextView tvLifeDynamics;
    private TextView tvNeighborHelp;
    private ViewPager2 viewPager;
    private FloatingActionButton fabPublish;

    // 选中和未选中的颜色
    private static final int COLOR_SELECTED = Color.parseColor("#000000"); // 黑色
    private static final int COLOR_UNSELECTED = Color.parseColor("#888888"); // 灰色

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dynamic, container, false);
        initViews(view);
        setupViewPager();
        return view;
    }

    private void initViews(View view) {
        tvLifeDynamics = view.findViewById(R.id.tv_life_dynamics);
        tvNeighborHelp = view.findViewById(R.id.tv_neighbor_help);
        viewPager = view.findViewById(R.id.view_pager_dynamic);
        fabPublish = view.findViewById(R.id.fab_publish);

        // 点击标题切换页面
        tvLifeDynamics.setOnClickListener(v -> viewPager.setCurrentItem(0));
        tvNeighborHelp.setOnClickListener(v -> viewPager.setCurrentItem(1));

        // 发布按钮点击事件
        fabPublish.setOnClickListener(v -> {
            // 根据当前页面判断是发布动态还是发布求助
            int currentItem = viewPager.getCurrentItem();
            String type = currentItem == 0 ? "生活动态" : "邻里互助";
            Toast.makeText(getContext(), "准备发布：" + type, Toast.LENGTH_SHORT).show();
            // TODO: 跳转到发布页面
        });
    }

    private void setupViewPager() {
        // 创建适配器
        DynamicPagerAdapter adapter = new DynamicPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // 注册页面变更回调，实现滑动时改变文字颜色
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateTabStyles(position);
            }
        });
    }

    // 根据位置更新顶部文字样式
    private void updateTabStyles(int position) {
        if (position == 0) {
            // 选中生活动态
            tvLifeDynamics.setTextColor(COLOR_SELECTED);
            tvNeighborHelp.setTextColor(COLOR_UNSELECTED);
            tvLifeDynamics.setTextSize(18); // 选中稍微大一点
            tvNeighborHelp.setTextSize(17);
        } else {
            // 选中邻里互助
            tvLifeDynamics.setTextColor(COLOR_UNSELECTED);
            tvNeighborHelp.setTextColor(COLOR_SELECTED);
            tvLifeDynamics.setTextSize(17);
            tvNeighborHelp.setTextSize(18);
        }
    }

    // 内部适配器类
    private static class DynamicPagerAdapter extends FragmentStateAdapter {

        public DynamicPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new LifeDynamicsFragment(); // 生活动态子页面
            } else {
                return new NeighborHelpFragment(); // 邻里互助子页面
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
package com.gxuwz.ccsa.ui.resident;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.gxuwz.ccsa.R;

/**
 * 居民端-动态主页面
 * 包含"生活动态"和"邻里互助"两个子页面
 * 优化：解决了与主页ViewPager2的滑动冲突
 */
public class DynamicFragment extends Fragment {

    private TextView tvLifeDynamics;
    private TextView tvNeighborHelp;
    private ViewPager2 viewPager;

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

        // 点击标题切换页面
        tvLifeDynamics.setOnClickListener(v -> viewPager.setCurrentItem(0));
        tvNeighborHelp.setOnClickListener(v -> viewPager.setCurrentItem(1));
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

        // ================== 核心优化：解决同方向嵌套ViewPager滑动冲突 ==================
        // 获取ViewPager2内部的RecyclerView容器
        View child = viewPager.getChildAt(0);
        if (child instanceof RecyclerView) {
            child.setOnTouchListener(new View.OnTouchListener() {
                private float startX, startY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = event.getX();
                            startY = event.getY();
                            // 初始按下时，强制禁止父容器拦截，先让子容器判断意图
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float endX = event.getX();
                            float endY = event.getY();
                            float disX = Math.abs(endX - startX);
                            float disY = Math.abs(endY - startY);

                            // 如果是水平滑动 (且幅度大于垂直滑动)
                            if (disX > disY) {
                                int currentItem = viewPager.getCurrentItem();
                                int itemCount = viewPager.getAdapter().getItemCount();

                                if (endX < startX) {
                                    // 用户手指向左滑 (意图：看右边的页面 -> 邻里互助)
                                    if (currentItem < itemCount - 1) {
                                        // 如果还有右边的页面，子容器自己处理，禁止父容器拦截
                                        v.getParent().requestDisallowInterceptTouchEvent(true);
                                    } else {
                                        // 已经是最后一页(邻里互助)，放开拦截，让父容器(主页)处理，滑向"我的"
                                        v.getParent().requestDisallowInterceptTouchEvent(false);
                                    }
                                } else if (endX > startX) {
                                    // 用户手指向右滑 (意图：看左边的页面 -> 生活动态)
                                    if (currentItem > 0) {
                                        // 如果还有左边的页面，子容器自己处理
                                        v.getParent().requestDisallowInterceptTouchEvent(true);
                                    } else {
                                        // 已经是第一页(生活动态)，放开拦截，让父容器(主页)处理，滑向"服务"
                                        v.getParent().requestDisallowInterceptTouchEvent(false);
                                    }
                                }
                            } else {
                                // 如果是垂直滑动，交给父容器处理（可能有下拉刷新或外层滚动）
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            // 触摸结束，恢复默认
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                    return false;
                }
            });
        }
    }

    // 根据位置更新顶部文字样式
    private void updateTabStyles(int position) {
        if (position == 0) {
            tvLifeDynamics.setTextColor(COLOR_SELECTED);
            tvNeighborHelp.setTextColor(COLOR_UNSELECTED);
            tvLifeDynamics.setTextSize(18);
            tvNeighborHelp.setTextSize(17);
        } else {
            tvLifeDynamics.setTextColor(COLOR_UNSELECTED);
            tvNeighborHelp.setTextColor(COLOR_SELECTED);
            tvLifeDynamics.setTextSize(17);
            tvNeighborHelp.setTextSize(18);
        }
    }

    private static class DynamicPagerAdapter extends FragmentStateAdapter {
        public DynamicPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new LifeDynamicsFragment();
            } else {
                return new NeighborHelpFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
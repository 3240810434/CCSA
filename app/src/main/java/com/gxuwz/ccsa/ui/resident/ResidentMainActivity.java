package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Notification;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.ui.view.DraggableFloatingActionButton;
import com.gxuwz.ccsa.util.NotificationUtil;

import java.util.Date;

public class ResidentMainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnNotification, btnDynamic, btnMine;
    private ViewPager2 viewPager;
    private User currentUser;
    private DraggableFloatingActionButton fabEmergency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_main);

        // 从Intent获取用户信息
        currentUser = (User) getIntent().getSerializableExtra("user");

        initViews();
        setupViewPager();
        setupEmergencyAlarm();
    }

    public User getUser() {
        return currentUser;
    }

    private void initViews() {
        btnNotification = findViewById(R.id.btn_notification);
        btnDynamic = findViewById(R.id.btn_dynamic);
        btnMine = findViewById(R.id.btn_mine);
        viewPager = findViewById(R.id.view_pager_main);
        fabEmergency = findViewById(R.id.fab_emergency);

        btnNotification.setOnClickListener(this);
        btnDynamic.setOnClickListener(this);
        btnMine.setOnClickListener(this);
    }

    // 设置一键报警逻辑
    private void setupEmergencyAlarm() {
        fabEmergency.setOnClickListener(v -> {
            // 显示全屏红色警报Dialog
            new EmergencyAlarmDialog(ResidentMainActivity.this, () -> {
                // 回调：用户已经双击确认报警，且动画播放完毕
                performSimulationLogic();
            }).show();
        });
    }

    // 执行报警后的模拟闭环逻辑
    private void performSimulationLogic() {
        // 1. 立即反馈给用户
        Toast.makeText(this, "报警信息已发送至总控中心，请保持位置", Toast.LENGTH_LONG).show();

        // 2. 在数据库记录一条“我发出的报警” (这里为了演示简单，我们只记录一条收到的回执，或者你可以插入一条HistoryRecord)
        // ... (可选：写入报警记录表)

        // 3. 模拟“物业值班室”的反应：延迟5秒发送一条确认通知
        new Handler().postDelayed(() -> {
            simulatePropertyResponse();
        }, 5000);
    }

    // 模拟物业回应
    private void simulatePropertyResponse() {
        if (isDestroyed()) return;

        String title = "紧急报警回执";
        String content = "【系统】您的SOS报警已收到，安保组长(工号001)正在前往您的当前定位，请注意安全，保持电话畅通。";

        // 1. 插入数据库，这样用户在“通知”列表里能看到
        Notification reply = new Notification();
        reply.setCommunity(currentUser.getCommunityName()); // 假设User有这个字段，或者写死
        reply.setRecipientPhone(currentUser.getPhone());
        reply.setTitle(title);
        reply.setContent(content);
        reply.setType(2); // 设为管理员公告类型，或者新的类型
        reply.setPublisher("物业安保中心");
        reply.setCreateTime(new Date());
        reply.setRead(false);

        AppDatabase.getInstance(this).notificationDao().insert(reply);

        // 2. 触发系统通知栏通知（伴随声音/震动）
        NotificationUtil.sendVoteNotification(this, title, content);

        // 3. 既然收到新消息，如果当前在通知Fragment，可能需要刷新一下（通常Fragment `onResume` 会刷新，或者使用EventBus/LiveData，这里简化处理）
    }

    private void setupViewPager() {
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0: return new NotificationFragment();
                    case 1: return new DynamicFragment();
                    default: return new MineFragment();
                }
            }

            @Override
            public int getItemCount() { return 3; }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateBottomNavigation(position);
            }
        });
        viewPager.setOffscreenPageLimit(2);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_notification) {
            viewPager.setCurrentItem(0, false);
        } else if (id == R.id.btn_dynamic) {
            viewPager.setCurrentItem(1, false);
        } else if (id == R.id.btn_mine) {
            viewPager.setCurrentItem(2, false);
        }
    }

    private void updateBottomNavigation(int position) {
        btnNotification.setSelected(false);
        btnDynamic.setSelected(false);
        btnMine.setSelected(false);

        switch (position) {
            case 0: btnNotification.setSelected(true); break;
            case 1: btnDynamic.setSelected(true); break;
            case 2: btnMine.setSelected(true); break;
        }
    }
}
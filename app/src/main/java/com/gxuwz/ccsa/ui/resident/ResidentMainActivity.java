package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.User;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;

public class ResidentMainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnNotification, btnDynamic, btnMine;
    private List<Fragment> fragmentList = new ArrayList<>();
    private Fragment currentFragment;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_main);

        // 从Intent获取用户信息
        currentUser = (User) getIntent().getSerializableExtra("user");

        initViews();
        initFragments();

        // 默认显示服务页面（原通知页面）
        switchFragment(0);
    }

    // 提供getter方法获取当前用户
    public User getUser() {
        return currentUser;
    }

    private void initViews() {
        btnNotification = findViewById(R.id.btn_notification);
        btnDynamic = findViewById(R.id.btn_dynamic);
        btnMine = findViewById(R.id.btn_mine);

        btnNotification.setOnClickListener(this);
        btnDynamic.setOnClickListener(this);
        btnMine.setOnClickListener(this);
    }

    private void initFragments() {
        fragmentList.add(new NotificationFragment());  // 原通知Fragment现在显示在"服务"按钮
        fragmentList.add(new DynamicFragment());
        fragmentList.add(new MineFragment());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_notification) {
            switchFragment(0);  // 服务按钮对应原通知Fragment
        } else if (id == R.id.btn_dynamic) {
            switchFragment(1);
        } else if (id == R.id.btn_mine) {
            switchFragment(2);
        }
    }

    private void switchFragment(int position) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment targetFragment = fragmentList.get(position);

        // 先移除当前显示的Fragment
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }

        // 检查目标Fragment是否已添加
        if (!targetFragment.isAdded()) {
            transaction.add(R.id.fl_container, targetFragment);
        } else {
            transaction.show(targetFragment);
        }

        transaction.commit();
        currentFragment = targetFragment;

        // 更新底部导航选中状态
        updateBottomNavigation(position);
    }

    // 更新底部导航选中状态
    private void updateBottomNavigation(int position) {
        // 重置所有按钮状态
        btnNotification.setSelected(false);
        btnDynamic.setSelected(false);
        btnMine.setSelected(false);

        // 设置当前选中按钮
        switch (position) {
            case 0:
                btnNotification.setSelected(true);
                break;
            case 1:
                btnDynamic.setSelected(true);
                break;
            case 2:
                btnMine.setSelected(true);
                break;
        }
    }
}
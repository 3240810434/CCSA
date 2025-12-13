package com.gxuwz.ccsa.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.ui.merchant.MerchantMainActivity;

public class LoginActivity extends AppCompatActivity {

    private CardView cardResident;
    private CardView cardMerchant;
    private TextView tvAdminLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏默认标题栏（可选）
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
    }

    private void initViews() {
        // 绑定 XML 中的控件 ID
        cardResident = findViewById(R.id.card_resident);
        cardMerchant = findViewById(R.id.card_merchant);
        tvAdminLogin = findViewById(R.id.tv_admin_login);
    }

    private void setupListeners() {
        // 1. 居民入口点击事件
        cardResident.setOnClickListener(v -> {
            // 添加简单的缩放动画效果
            animateView(v);
            Intent intent = new Intent(LoginActivity.this, ResidentLoginActivity.class);
            startActivity(intent);
        });

        // 2. 商家入口点击事件
        cardMerchant.setOnClickListener(v -> {
            animateView(v);
            // 注意：这里通常跳转到商家登录页，如果已登录则去主页，这里先跳转到商家登录
            Intent intent = new Intent(LoginActivity.this, MerchantLoginActivity.class);
            startActivity(intent);
        });

        // 3. 管理员入口点击事件
        tvAdminLogin.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, AdminLoginActivity.class);
            startActivity(intent);
        });
    }

    // 简单的点击缩放动画辅助方法
    private void animateView(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() ->
                        view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                )
                .start();
    }
}
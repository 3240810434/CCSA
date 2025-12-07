package com.gxuwz.ccsa.login;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.RadioGroup;
import com.gxuwz.ccsa.R;

public class LoginActivity extends AppCompatActivity {

    private RadioGroup roleRadioGroup;
    private Button loginButton;
    private int selectedRole = 0; // 0:居民, 1:管理员, 2:商家

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // 布局路径调整

        // 初始化控件
        roleRadioGroup = findViewById(R.id.rg_role);
        loginButton = findViewById(R.id.btn_login);

        // 监听角色选择变化
        roleRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_resident:
                    selectedRole = 0;
                    break;
                case R.id.rb_admin:
                    selectedRole = 1;
                    break;
                case R.id.rb_merchant:
                    selectedRole = 2;
                    break;
            }
        });

        // 登录按钮点击事件（跳转对应角色页面）
        loginButton.setOnClickListener(v -> {
            Intent intent;
            switch (selectedRole) {
                case 0:
                    intent = new Intent(LoginActivity.this, ResidentLoginActivity.class);
                    break;
                case 1:
                    intent = new Intent(LoginActivity.this, AdminLoginActivity.class);
                    break;
                case 2:
                    intent = new Intent(LoginActivity.this, MerchantLoginActivity.class);
                    break;
                default:
                    intent = new Intent(LoginActivity.this, ResidentLoginActivity.class);
            }
            startActivity(intent);
        });
    }
}
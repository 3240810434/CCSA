/*
package com.gxuwz.ccsa.login;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Merchant;
import com.gxuwz.ccsa.ui.merchant.MerchantMainActivity;

public class MerchantLoginActivity extends AppCompatActivity {

    private EditText etPhone;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private TextView tvForgotPassword;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_login);

        db = AppDatabase.getInstance(this);
        initViews();
        setupListeners();
    }

    private void initViews() {
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> login());

        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "请联系管理员重置密码", Toast.LENGTH_SHORT).show()
        );

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MerchantLoginActivity.this, MerchantRegisterActivity.class);
            startActivity(intent);
        });
    }

    private void login() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入账号和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        // 数据库查询
        // 注意：如果 AppDatabase 未开启 allowMainThreadQueries()，建议将此操作放入子线程
        Merchant merchant = db.merchantDao().login(phone, password);

        if (merchant != null) {
            // 保存登录状态到 SharedPreferences
            SharedPreferences sp = getSharedPreferences("merchant_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();

            // 【修改处】：使用 getId() 方法获取主键，确保与 Merchant 实体类定义一致
            editor.putLong("merchant_id", merchant.getId());

            editor.apply();

            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MerchantLoginActivity.this, MerchantMainActivity.class);
            // 传递对象到下一个页面
            intent.putExtra("merchant", merchant);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "账号或密码错误", Toast.LENGTH_SHORT).show();
        }
    }
}*/
package com.gxuwz.ccsa.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gxuwz.ccsa.R;

public class LoginActivity extends AppCompatActivity {

    private CardView cardResident;
    private CardView cardMerchant;
    private TextView tvAdminLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏默认标题栏，让界面全屏沉浸感更强
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_login);

        // 初始化控件
        cardResident = findViewById(R.id.card_resident);
        cardMerchant = findViewById(R.id.card_merchant);
        tvAdminLogin = findViewById(R.id.tv_admin_login);

        // 设置点击事件 - 居民
        cardResident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 添加一个简单的点击缩放动画效果（可选，提升手感）
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    Intent intent = new Intent(LoginActivity.this, ResidentLoginActivity.class);
                    startActivity(intent);
                }).start();
            }
        });

        // 设置点击事件 - 商家
        cardMerchant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    Intent intent = new Intent(LoginActivity.this, MerchantMainActivity.class);
                    startActivity(intent);
                }).start();
            }
        });

        // 设置点击事件 - 管理员
        tvAdminLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, AdminLoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
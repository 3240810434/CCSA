// CCSA/app/src/main/java/com/gxuwz/ccsa/login/MerchantLoginActivity.java
package com.gxuwz.ccsa.login;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
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
        // 登录按钮点击事件
        btnLogin.setOnClickListener(v -> login());

        // 忘记密码点击事件
        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(MerchantLoginActivity.this, "请联系管理员重置密码", Toast.LENGTH_SHORT).show()
        );

        // 跳转到注册页面
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MerchantLoginActivity.this, MerchantRegisterActivity.class);
            startActivity(intent);
        });
    }

    private void login() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 输入校验
        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "手机号和密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 数据库查询验证
        Merchant merchant = db.merchantDao().login(phone, password);
        if (merchant != null) {
            // 登录成功，跳转到商家主界面
            Intent intent = new Intent(MerchantLoginActivity.this, MerchantMainActivity.class);
            intent.putExtra("merchant", merchant);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "手机号或密码错误", Toast.LENGTH_SHORT).show();
        }
    }
}
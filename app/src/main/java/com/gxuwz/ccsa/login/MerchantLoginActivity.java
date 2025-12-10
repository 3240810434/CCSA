package com.gxuwz.ccsa.login;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
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
        Merchant merchant = db.merchantDao().login(phone, password);
        if (merchant != null) {
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MerchantLoginActivity.this, MerchantMainActivity.class);
            intent.putExtra("merchant", merchant);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "账号或密码错误", Toast.LENGTH_SHORT).show();
        }
    }
}
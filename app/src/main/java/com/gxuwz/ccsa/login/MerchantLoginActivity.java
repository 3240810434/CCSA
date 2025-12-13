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
}
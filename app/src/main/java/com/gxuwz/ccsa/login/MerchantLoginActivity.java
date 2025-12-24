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
import com.gxuwz.ccsa.api.RetrofitClient; // 导入 RetrofitClient
import com.gxuwz.ccsa.common.Result;
import com.gxuwz.ccsa.model.Merchant;
import com.gxuwz.ccsa.ui.merchant.MerchantMainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MerchantLoginActivity extends AppCompatActivity {

    private EditText etPhone;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private TextView tvForgotPassword;
    // private AppDatabase db; // 不再需要本地数据库

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_login);
        // db = AppDatabase.getInstance(this); // 移除
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

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(MerchantLoginActivity.this, MerchantForgotPasswordActivity.class);
            startActivity(intent);
        });

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

        // 构建登录请求对象 (仅需手机号和密码)
        Merchant loginReq = new Merchant(null, null, null, null, phone, password);

        // 发起网络请求
        RetrofitClient.getInstance().getApi().merchantLogin(loginReq).enqueue(new Callback<Result<Merchant>>() {
            @Override
            public void onResponse(Call<Result<Merchant>> call, Response<Result<Merchant>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result<Merchant> result = response.body();
                    if (result.getCode() == 200) { // 假设 200 为成功码
                        Merchant merchant = result.getData();

                        // 保存登录状态
                        SharedPreferences sp = getSharedPreferences("merchant_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt("merchant_id", merchant.getId());
                        editor.apply();

                        Toast.makeText(MerchantLoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MerchantLoginActivity.this, MerchantMainActivity.class);
                        intent.putExtra("merchant", merchant);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(MerchantLoginActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MerchantLoginActivity.this, "服务器响应错误", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<Merchant>> call, Throwable t) {
                Toast.makeText(MerchantLoginActivity.this, "网络连接失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
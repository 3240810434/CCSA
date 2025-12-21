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
import com.gxuwz.ccsa.api.RetrofitClient;
import com.gxuwz.ccsa.common.Result;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.ui.resident.ResidentMainActivity;
import com.gxuwz.ccsa.util.SharedPreferencesUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResidentLoginActivity extends AppCompatActivity {

    private EditText etPhone, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_login);
        initViews();
        setupListeners();
    }

    private void initViews() {
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvRegister = findViewById(R.id.tv_register);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> login());

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(ResidentLoginActivity.this, ResidentForgotPasswordActivity.class);
            startActivity(intent);
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(ResidentLoginActivity.this, ResidentRegisterActivity.class);
            startActivity(intent);
        });
    }

    private void login() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "手机号和密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        User requestUser = new User();
        requestUser.setPhone(phone);
        requestUser.setPassword(password);

        RetrofitClient.getInstance().getApi().userLogin(requestUser)
                .enqueue(new Callback<Result<User>>() {
                    @Override
                    public void onResponse(Call<Result<User>> call, Response<Result<User>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Result<User> result = response.body();
                            if (result.getCode() == 200) {
                                User user = result.getData();

                                // 1. 兼容旧逻辑
                                SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                sharedPreferences.edit()
                                        .putLong("user_id", user.getId())
                                        .putString("user_name", user.getName())
                                        .apply();

                                // 2. 保存完整User对象
                                SharedPreferencesUtil.saveUser(ResidentLoginActivity.this, user);

                                // 3. 跳转
                                Intent intent = new Intent(ResidentLoginActivity.this, ResidentMainActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("user", user);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(ResidentLoginActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ResidentLoginActivity.this, "服务器错误", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Result<User>> call, Throwable t) {
                        Toast.makeText(ResidentLoginActivity.this, "网络连接失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
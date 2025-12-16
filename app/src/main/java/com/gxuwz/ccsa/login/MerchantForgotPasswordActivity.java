package com.gxuwz.ccsa.login;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Merchant;

public class MerchantForgotPasswordActivity extends AppCompatActivity {

    private EditText etPhone, etCode, etNewPassword;
    private Button btnGetCode, btnConfirm;
    private TextView tvBackLogin;
    private CountDownTimer countDownTimer;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_forgot_password);

        db = AppDatabase.getInstance(this);
        initViews();
        setupListeners();
    }

    private void initViews() {
        etPhone = findViewById(R.id.et_phone);
        etCode = findViewById(R.id.et_code);
        etNewPassword = findViewById(R.id.et_new_password);
        btnGetCode = findViewById(R.id.btn_get_code);
        btnConfirm = findViewById(R.id.btn_confirm);
        tvBackLogin = findViewById(R.id.tv_back_login);
    }

    private void setupListeners() {
        // 获取验证码
        btnGetCode.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "请先输入手机号", Toast.LENGTH_SHORT).show();
                return;
            }
           /* if (phone.length() != 11) {
                Toast.makeText(this, "请输入正确的11位手机号", Toast.LENGTH_SHORT).show();
                return;
            }*/

            // 模拟发送
            Toast.makeText(this, "验证码已发送", Toast.LENGTH_SHORT).show();
            startCountDown();
        });

        // 确认修改
        btnConfirm.setOnClickListener(v -> resetPassword());

        // 返回登录
        tvBackLogin.setOnClickListener(v -> finish());
    }

    private void startCountDown() {
        btnGetCode.setEnabled(false);
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                btnGetCode.setText(millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                btnGetCode.setText("获取验证码");
                btnGetCode.setEnabled(true);
            }
        };
        countDownTimer.start();
    }

    private void resetPassword() {
        String phone = etPhone.getText().toString().trim();
        String code = etCode.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "请输入新密码", Toast.LENGTH_SHORT).show();
            return;
        }

        // 模拟验证码校验
        if (!"1234".equals(code)) {
            Toast.makeText(this, "验证码错误", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            // 查询商家
            Merchant merchant = db.merchantDao().findByPhone(phone);
            if (merchant == null) {
                runOnUiThread(() -> Toast.makeText(this, "该手机号未注册商家账号", Toast.LENGTH_SHORT).show());
            } else {
                // 更新密码
                merchant.setPassword(newPassword);
                db.merchantDao().update(merchant);
                runOnUiThread(() -> {
                    Toast.makeText(this, "密码修改成功", Toast.LENGTH_SHORT).show();
                    finish(); // 返回登录页
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
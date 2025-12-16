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
import com.gxuwz.ccsa.model.User;

public class ResidentForgotPasswordActivity extends AppCompatActivity {

    private EditText etPhone, etCode, etNewPassword;
    private Button btnGetCode, btnConfirm;
    private TextView tvBackLogin;
    private CountDownTimer countDownTimer;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_forgot_password);

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
        // 获取验证码点击事件
        btnGetCode.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "请先输入手机号", Toast.LENGTH_SHORT).show();
                return;
            }

            // 模拟发送验证码
            Toast.makeText(this, "验证码已发送", Toast.LENGTH_SHORT).show();
            startCountDown();
        });

        // 确认修改点击事件
        btnConfirm.setOnClickListener(v -> resetPassword());

        // 返回登录
        tvBackLogin.setOnClickListener(v -> finish());
    }

    /**
     * 开始60秒倒计时
     */
    private void startCountDown() {
        btnGetCode.setEnabled(false); // 禁用按钮
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

    /**
     * 重置密码逻辑
     */
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

        // 验证码校验
        if (!"1234".equals(code)) {
            Toast.makeText(this, "验证码错误", Toast.LENGTH_SHORT).show();
            return;
        }

        // 数据库操作
        new Thread(() -> {
            // 查询该手机号是否存在
            User user = db.userDao().findByPhone(phone);
            if (user == null) {
                runOnUiThread(() -> Toast.makeText(this, "该手机号未注册", Toast.LENGTH_SHORT).show());
            } else {
                // 更新密码
                user.setPassword(newPassword);
                db.userDao().update(user);
                runOnUiThread(() -> {
                    Toast.makeText(this, "修改成功，请重新登录", Toast.LENGTH_SHORT).show();
                    finish(); // 关闭当前页面，返回登录页
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
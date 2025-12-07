// CCSA/app/src/main/java/com/gxuwz/ccsa/login/ResidentLoginActivity.java
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
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.ui.resident.ResidentMainActivity;

/**
 * 居民登录页面
 */
public class ResidentLoginActivity extends AppCompatActivity {

    private EditText etPhone; // 手机号输入框
    private EditText etPassword; // 密码输入框
    private Button btnLogin; // 登录按钮
    private TextView tvForgotPassword; // 忘记密码
    private TextView tvRegister; // 注册入口
    private AppDatabase db; // 数据库实例

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_login);

        // 初始化数据库
        db = AppDatabase.getInstance(this);
        // 初始化控件
        initViews();
        // 设置点击事件
        setupListeners();
    }

    /**
     * 初始化所有UI控件
     */
    private void initViews() {
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvRegister = findViewById(R.id.tv_register);
    }

    /**
     * 设置所有控件的点击事件
     */
    private void setupListeners() {
        // 登录按钮点击事件
        btnLogin.setOnClickListener(v -> login());

        // 忘记密码点击事件
        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(ResidentLoginActivity.this, "请联系管理员重置密码", Toast.LENGTH_SHORT).show()
        );

        // 跳转到注册页面
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(ResidentLoginActivity.this, ResidentRegisterActivity.class);
            startActivity(intent);
        });
    }

    /**
     * 登录逻辑处理
     */
    private void login() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 输入校验
        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "手机号和密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 数据库查询用户（验证账号密码）
        User user = db.userDao().login(phone, password);
        if (user != null) {
            // 登录成功：使用Bundle传递User对象（避免序列化报错）
            Intent intent = new Intent(ResidentLoginActivity.this, ResidentMainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("user", user); // 显式序列化传递
            intent.putExtras(bundle);
            startActivity(intent);
            finish(); // 关闭登录页面，避免返回
        } else {
            Toast.makeText(this, "手机号或密码错误", Toast.LENGTH_SHORT).show();
        }
    }
}
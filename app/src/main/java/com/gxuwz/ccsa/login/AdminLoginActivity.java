package com.gxuwz.ccsa.login;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Admin;
import com.gxuwz.ccsa.ui.admin.AdminMainActivity;

public class AdminLoginActivity extends AppCompatActivity {

    private Spinner communitySpinner;
    private EditText accountEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView forgotPasswordTextView;
    private String selectedCommunity;
    private AppDatabase db;
    private String[] communities = {
            "悦景小区", "梧桐小区", "阳光小区", "锦园小区", "幸福小区",
            "芳邻小区", "逸景小区", "康城小区", "沁园小区", "静安小区"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        // 初始化数据库
        db = AppDatabase.getInstance(this);

        // 初始化控件
        initViews();

        // 设置小区下拉选择框
        setupCommunitySpinner();

        // 设置登录按钮点击事件
        setupLoginButton();

        // 设置忘记密码点击事件
        setupForgotPassword();
    }

    private void initViews() {
        communitySpinner = findViewById(R.id.spinner_community);
        accountEditText = findViewById(R.id.et_account);
        passwordEditText = findViewById(R.id.et_password);
        loginButton = findViewById(R.id.btn_login);
        forgotPasswordTextView = findViewById(R.id.tv_forgot_password);
    }

    private void setupCommunitySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                communities
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        communitySpinner.setAdapter(adapter);

        // 设置默认选中第一个小区
        communitySpinner.setSelection(0);
        selectedCommunity = communities[0];

        // 监听选择变化
        communitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCommunity = communities[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCommunity = communities[0];
            }
        });
    }

    private void setupLoginButton() {
        loginButton.setOnClickListener(v -> {
            String account = accountEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // 验证账号密码
            if (account.isEmpty() || password.isEmpty()) {
                Toast.makeText(AdminLoginActivity.this, "账号和密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            // 从数据库查询管理员
            Admin admin = db.adminDao().findByAccount(account);
            if (admin != null) {
                // 验证密码
                if (admin.getPassword().equals(password)) {
                    // 验证小区管理权限
                    if (admin.getCommunity().equals(selectedCommunity)) {
                        // 登录成功，跳转到管理员首界面
                        Intent intent = new Intent(AdminLoginActivity.this, AdminMainActivity.class);
                        intent.putExtra("community", selectedCommunity);
                        intent.putExtra("adminAccount", account);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(AdminLoginActivity.this,
                                "该账号没有" + selectedCommunity + "的管理权限",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminLoginActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AdminLoginActivity.this, "账号不存在", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupForgotPassword() {
        forgotPasswordTextView.setOnClickListener(v -> {
            Toast.makeText(AdminLoginActivity.this, "请联系系统管理员重置密码", Toast.LENGTH_SHORT).show();
        });
    }
}
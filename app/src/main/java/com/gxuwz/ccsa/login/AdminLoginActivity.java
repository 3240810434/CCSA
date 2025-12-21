package com.gxuwz.ccsa.login;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.api.RetrofitClient;
import com.gxuwz.ccsa.common.Result;
import com.gxuwz.ccsa.model.Admin;
import com.gxuwz.ccsa.ui.admin.AdminMainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminLoginActivity extends AppCompatActivity {

    private Spinner communitySpinner;
    private EditText accountEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private String selectedCommunity;

    // 保持原来的小区列表
    private String[] communities = {
            "悦景小区", "梧桐小区", "阳光小区", "锦园小区", "幸福小区",
            "芳邻小区", "逸景小区", "康城小区", "沁园小区", "静安小区"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);
        initViews();
        setupCommunitySpinner();
        setupLoginButton();
    }

    private void initViews() {
        communitySpinner = findViewById(R.id.spinner_community);
        accountEditText = findViewById(R.id.et_account);
        passwordEditText = findViewById(R.id.et_password);
        loginButton = findViewById(R.id.btn_login);
    }

    private void setupCommunitySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, communities
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        communitySpinner.setAdapter(adapter);
        communitySpinner.setSelection(0);
        selectedCommunity = communities[0];

        communitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCommunity = communities[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupLoginButton() {
        loginButton.setOnClickListener(v -> {
            String account = accountEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (account.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "账号和密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            // 构建请求对象
            Admin requestAdmin = new Admin();
            requestAdmin.setAccount(account);
            requestAdmin.setPassword(password);

            // 发起网络请求
            RetrofitClient.getInstance().getApi().adminLogin(requestAdmin)
                    .enqueue(new Callback<Result<Admin>>() {
                        @Override
                        public void onResponse(Call<Result<Admin>> call, Response<Result<Admin>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Result<Admin> result = response.body();
                                if (result.getCode() == 200) {
                                    Admin admin = result.getData();
                                    // 校验小区权限
                                    if (selectedCommunity.equals(admin.getCommunity())) {
                                        // 登录成功，保存本地
                                        SharedPreferences sp = getSharedPreferences("admin_prefs", MODE_PRIVATE);
                                        sp.edit()
                                                .putString("community", selectedCommunity)
                                                .putString("adminAccount", account)
                                                .apply();

                                        Intent intent = new Intent(AdminLoginActivity.this, AdminMainActivity.class);
                                        intent.putExtra("community", selectedCommunity);
                                        intent.putExtra("adminAccount", account);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(AdminLoginActivity.this,
                                                "该账号没有" + selectedCommunity + "的管理权限", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(AdminLoginActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(AdminLoginActivity.this, "服务器错误", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Result<Admin>> call, Throwable t) {
                            Toast.makeText(AdminLoginActivity.this, "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
// CCSA/app/src/main/java/com/gxuwz/ccsa/login/MerchantRegisterActivity.java
package com.gxuwz.ccsa.login;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import java.util.ArrayList;
import java.util.List;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Merchant;
import com.gxuwz.ccsa.ui.merchant.MerchantMainActivity;

public class MerchantRegisterActivity extends AppCompatActivity {

    // UI控件
    private Spinner spinnerCommunity;
    private EditText etMerchantName;
    private EditText etContactName;
    private RadioGroup rgGender;
    private EditText etPhone;
    private EditText etPassword;
    private EditText etVerificationCode;
    private TextView tvGetCode;
    private Button btnRegister;

    // 数据相关
    private AppDatabase db;
    private String gender = "男";
    private String[] communities = {
            "悦景小区", "梧桐小区", "阳光小区", "锦园小区", "幸福小区",
            "芳邻小区", "逸景小区", "康城小区", "沁园小区", "静安小区"
    };
    private List<String> selectedCommunities = new ArrayList<>();
    private String selectedCommunityText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_register);

        db = AppDatabase.getInstance(this);
        initViews();
        setupSpinners();
        setupListeners();
    }

    private void initViews() {
        spinnerCommunity = findViewById(R.id.spinner_community);
        etMerchantName = findViewById(R.id.et_merchant_name);
        etContactName = findViewById(R.id.et_contact_name);
        rgGender = findViewById(R.id.rg_gender);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etVerificationCode = findViewById(R.id.et_verification_code);
        tvGetCode = findViewById(R.id.tv_get_code);
        btnRegister = findViewById(R.id.btn_register);

        // 默认选中男性
        rgGender.check(R.id.rb_male);
    }

    private void setupSpinners() {
        ArrayAdapter<String> communityAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, communities
        );
        communityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCommunity.setAdapter(communityAdapter);

        // 小区选择监听（支持多选，用逗号分隔）
        spinnerCommunity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String community = communities[position];
                if (!selectedCommunities.contains(community)) {
                    selectedCommunities.add(community);
                }
                // 更新选中的小区文本（用逗号分隔）
                selectedCommunityText = android.text.TextUtils.join(",", selectedCommunities);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupListeners() {
        // 性别选择监听
        rgGender.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_male) {
                gender = "男";
            } else {
                gender = "女";
            }
        });

        // 获取验证码点击事件
        tvGetCode.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (phone.isEmpty() || phone.length() != 11) {
                Toast.makeText(this, "请输入正确的11位手机号", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "验证码已发送（默认1234）", Toast.LENGTH_SHORT).show();
        });

        // 注册按钮点击事件
        btnRegister.setOnClickListener(v -> register());
    }

    private void register() {
        // 获取输入内容
        String merchantName = etMerchantName.getText().toString().trim();
        String contactName = etContactName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String code = etVerificationCode.getText().toString().trim();

        // 校验所有字段不为空
        if (merchantName.isEmpty() || contactName.isEmpty() || phone.isEmpty() ||
                password.isEmpty() || code.isEmpty() || selectedCommunities.isEmpty()) {
            Toast.makeText(this, "请填写所有必填信息", Toast.LENGTH_SHORT).show();
            return;
        }

        // 校验手机号格式
        if (phone.length() != 11) {
            Toast.makeText(this, "请输入正确的11位手机号", Toast.LENGTH_SHORT).show();
            return;
        }

        // 校验验证码
        if (!"1234".equals(code)) {
            Toast.makeText(this, "验证码错误（正确为1234）", Toast.LENGTH_SHORT).show();
            return;
        }

        // 校验手机号是否已注册
        if (db.merchantDao().findByPhone(phone) != null) {
            Toast.makeText(this, "该手机号已注册，请直接登录", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建商家并保存到数据库
        Merchant merchant = new Merchant(
                selectedCommunityText,
                merchantName,
                contactName,
                gender,
                phone,
                password
        );
        db.merchantDao().insert(merchant);

        // 注册成功，跳转到商家首界面
        Toast.makeText(this, "注册成功！", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MerchantRegisterActivity.this, MerchantMainActivity.class);
        intent.putExtra("merchant", merchant);
        startActivity(intent);
        finish();
    }
}
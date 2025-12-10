package com.gxuwz.ccsa.login;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Merchant;
import com.gxuwz.ccsa.ui.merchant.MerchantMainActivity;

public class MerchantRegisterActivity extends AppCompatActivity {

    private TextView tvCommunitySelect;
    private EditText etMerchantName;
    private EditText etContactName;
    private RadioGroup rgGender;
    private EditText etPhone;
    private EditText etPassword;
    private Button btnRegister;

    private AppDatabase db;
    private String gender = "男";

    // 小区数据
    private String[] communities = {
            "悦景小区", "梧桐小区", "阳光小区", "锦园小区", "幸福小区",
            "芳邻小区", "逸景小区", "康城小区", "沁园小区", "静安小区"
    };
    // 记录选中状态的布尔数组
    private boolean[] checkedCommunities;
    // 最终选中的小区列表
    private List<String> selectedCommunityList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_register);

        db = AppDatabase.getInstance(this);
        // 初始化选中状态数组
        checkedCommunities = new boolean[communities.length];

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvCommunitySelect = findViewById(R.id.tv_community_select);
        etMerchantName = findViewById(R.id.et_merchant_name);
        etContactName = findViewById(R.id.et_contact_name);
        rgGender = findViewById(R.id.rg_gender);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_register);

        rgGender.check(R.id.rb_male);
    }

    private void setupListeners() {
        // 小区多选点击事件
        tvCommunitySelect.setOnClickListener(v -> showCommunityDialog());

        // 性别选择
        rgGender.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_male) gender = "男";
            else gender = "女";
        });

        // 注册按钮
        btnRegister.setOnClickListener(v -> register());
    }

    // 弹出多选对话框
    private void showCommunityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择服务小区");
        builder.setMultiChoiceItems(communities, checkedCommunities, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                // 更新选中状态
                checkedCommunities[which] = isChecked;
            }
        });

        builder.setPositiveButton("确定", (dialog, which) -> {
            // 清空旧数据
            selectedCommunityList.clear();
            // 遍历所有选项，将选中的加入列表
            for (int i = 0; i < communities.length; i++) {
                if (checkedCommunities[i]) {
                    selectedCommunityList.add(communities[i]);
                }
            }
            // 更新UI显示
            if (selectedCommunityList.isEmpty()) {
                tvCommunitySelect.setText("");
                tvCommunitySelect.setHint("点击选择服务小区 (可多选)");
            } else {
                tvCommunitySelect.setText(TextUtils.join(",", selectedCommunityList));
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void register() {
        String communityStr = tvCommunitySelect.getText().toString().trim();
        String merchantName = etMerchantName.getText().toString().trim();
        String contactName = etContactName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 校验
        if (selectedCommunityList.isEmpty()) {
            Toast.makeText(this, "请至少选择一个服务小区", Toast.LENGTH_SHORT).show();
            return;
        }
        if (merchantName.isEmpty() || contactName.isEmpty() || phone.isEmpty() ||
                password.isEmpty()) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查是否已注册
        if (db.merchantDao().findByPhone(phone) != null) {
            Toast.makeText(this, "该手机号已注册", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建对象
        Merchant merchant = new Merchant(
                communityStr,
                merchantName,
                contactName,
                gender,
                phone,
                password
        );

        // 插入数据库
        new Thread(() -> {
            db.merchantDao().insert(merchant);
            runOnUiThread(() -> {
                Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MerchantRegisterActivity.this, MerchantMainActivity.class);
                intent.putExtra("merchant", merchant);
                startActivity(intent);
                finish();
            });
        }).start();
    }
}
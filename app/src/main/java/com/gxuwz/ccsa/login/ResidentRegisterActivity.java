// CCSA/app/src/main/java/com/gxuwz/ccsa/login/ResidentRegisterActivity.java
package com.gxuwz.ccsa.login;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.ui.resident.ResidentMainActivity;

/**
 * 居民注册页面
 */
public class ResidentRegisterActivity extends AppCompatActivity {

    // UI控件
    private EditText etName;
    private RadioGroup rgGender;
    private EditText etPhone;
    private EditText etPassword;
    private Spinner spinnerCommunity;
    private Spinner spinnerBuilding;
    private Spinner spinnerRoom;
    private EditText etVerificationCode;
    private TextView tvGetCode;
    private Button btnRegister;

    // 数据相关
    private AppDatabase db;
    private String gender = "男"; // 默认性别：男
    // 小区列表（10个固定小区）
    private final String[] communities = {
            "悦景小区", "梧桐小区", "阳光小区", "锦园小区", "幸福小区",
            "芳邻小区", "逸景小区", "康城小区", "沁园小区", "静安小区"
    };
    // 楼栋列表（1-10栋）
    private final String[] buildings = {
            "1栋", "2栋", "3栋", "4栋", "5栋", "6栋", "7栋", "8栋", "9栋", "10栋"
    };
    // 房号列表（每栋20个房间示例）
    private final String[] rooms = {
            "101", "102", "201", "202", "301", "302", "401", "402", "501", "502",
            "601", "602", "701", "702", "801", "802", "901", "902", "1001", "1002"
    };
    // 选中的地址信息
    private String selectedCommunity;
    private String selectedBuilding;
    private String selectedRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_register);

        // 初始化数据库
        db = AppDatabase.getInstance(this);
        // 初始化控件
        initViews();
        // 初始化下拉选择框
        setupSpinners();
        // 设置点击事件
        setupListeners();
    }

    /**
     * 初始化所有UI控件
     */
    private void initViews() {
        etName = findViewById(R.id.et_name);
        rgGender = findViewById(R.id.rg_gender);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        spinnerCommunity = findViewById(R.id.spinner_community);
        spinnerBuilding = findViewById(R.id.spinner_building);
        spinnerRoom = findViewById(R.id.spinner_room);
        etVerificationCode = findViewById(R.id.et_verification_code);
        tvGetCode = findViewById(R.id.tv_get_code);
        btnRegister = findViewById(R.id.btn_register);

        // 默认选中男性
        rgGender.check(R.id.rb_male);
    }

    /**
     * 初始化小区、楼栋、房号下拉选择框
     */
    private void setupSpinners() {
        // 小区选择框
        ArrayAdapter<String> communityAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, communities
        );
        communityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCommunity.setAdapter(communityAdapter);
        selectedCommunity = communities[0]; // 默认选中第一个小区

        // 楼栋选择框
        ArrayAdapter<String> buildingAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, buildings
        );
        buildingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBuilding.setAdapter(buildingAdapter);
        selectedBuilding = buildings[0]; // 默认选中1栋

        // 房号选择框
        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, rooms
        );
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoom.setAdapter(roomAdapter);
        selectedRoom = rooms[0]; // 默认选中101室

        // 小区选择监听
        spinnerCommunity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCommunity = communities[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 楼栋选择监听
        spinnerBuilding.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBuilding = buildings[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 房号选择监听
        spinnerRoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRoom = rooms[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * 设置所有控件的点击事件
     */
    private void setupListeners() {
        // 性别选择监听
        rgGender.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_male) {
                gender = "男";
            } else {
                gender = "女";
            }
        });

        // 获取验证码点击事件（模拟发送，实际验证码为1234）
        tvGetCode.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (phone.isEmpty()) { // 修改：仅校验不为空
                Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "验证码已发送（默认1234）", Toast.LENGTH_SHORT).show();
        });

        // 注册按钮点击事件
        btnRegister.setOnClickListener(v -> register());
    }

    /**
     * 注册逻辑处理（校验+数据库存储）
     */
    private void register() {
        // 获取输入内容
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String code = etVerificationCode.getText().toString().trim();

        // 1. 校验所有字段不为空
        if (name.isEmpty() || phone.isEmpty() || password.isEmpty() || code.isEmpty()) {
            Toast.makeText(this, "请填写所有必填信息", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. 移除手机号11位校验

        // 3. 校验验证码（固定为1234）
        if (!"1234".equals(code)) {
            Toast.makeText(this, "验证码错误（正确为1234）", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. 校验手机号是否已注册
        if (db.userDao().findByPhone(phone) != null) {
            Toast.makeText(this, "该手机号已注册，请直接登录", Toast.LENGTH_SHORT).show();
            return;
        }

        // 5. 所有校验通过，创建用户并保存到数据库
        User user = new User(
                name,
                gender,
                phone,
                password,
                selectedCommunity,
                selectedBuilding,
                selectedRoom
        );
        db.userDao().insert(user);

        // 6. 注册成功，跳转到居民首界面（使用Bundle传递用户信息）
        Toast.makeText(this, "注册成功！", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ResidentRegisterActivity.this, ResidentMainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", user); // 显式序列化传递
        intent.putExtras(bundle);
        startActivity(intent);
        finish(); // 关闭注册页面
    }
}
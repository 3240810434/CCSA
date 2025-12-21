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
import android.widget.Toast;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.api.RetrofitClient;
import com.gxuwz.ccsa.common.Result;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.ui.resident.ResidentMainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResidentRegisterActivity extends AppCompatActivity {

    private EditText etName, etPhone, etPassword;
    private RadioGroup rgGender;
    private Spinner spinnerCommunity, spinnerBuilding, spinnerRoom;
    private Button btnRegister;
    private String gender = "男";
    private String selectedCommunity, selectedBuilding, selectedRoom;

    private final String[] communities = { "悦景小区", "梧桐小区", "阳光小区", "锦园小区", "幸福小区", "芳邻小区", "逸景小区", "康城小区", "沁园小区", "静安小区" };
    private final String[] buildings = { "1栋", "2栋", "3栋", "4栋", "5栋", "6栋", "7栋", "8栋", "9栋", "10栋" };
    private final String[] rooms = { "101", "102", "201", "202", "301", "302", "401", "402", "501", "502", "601", "602", "701", "702", "801", "802", "901", "902", "1001", "1002" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_register);
        initViews();
        setupSpinners();
        setupListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.et_name);
        rgGender = findViewById(R.id.rg_gender);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        spinnerCommunity = findViewById(R.id.spinner_community);
        spinnerBuilding = findViewById(R.id.spinner_building);
        spinnerRoom = findViewById(R.id.spinner_room);
        btnRegister = findViewById(R.id.btn_register);
        rgGender.check(R.id.rb_male);
    }

    private void setupSpinners() {
        // 设置 Adapter 代码与原代码一致，此处简化省略，请保留原有逻辑
        ArrayAdapter<String> cAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, communities);
        cAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCommunity.setAdapter(cAdapter);
        selectedCommunity = communities[0];

        ArrayAdapter<String> bAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, buildings);
        bAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBuilding.setAdapter(bAdapter);
        selectedBuilding = buildings[0];

        ArrayAdapter<String> rAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rooms);
        rAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoom.setAdapter(rAdapter);
        selectedRoom = rooms[0];

        spinnerCommunity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { selectedCommunity = communities[pos]; }
            public void onNothingSelected(AdapterView<?> p) {}
        });
        spinnerBuilding.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { selectedBuilding = buildings[pos]; }
            public void onNothingSelected(AdapterView<?> p) {}
        });
        spinnerRoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { selectedRoom = rooms[pos]; }
            public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void setupListeners() {
        rgGender.setOnCheckedChangeListener((group, checkedId) -> gender = (checkedId == R.id.rb_male) ? "男" : "女");

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请填写所有必填信息", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = new User();
            user.setName(name);
            user.setGender(gender);
            user.setPhone(phone);
            user.setPassword(password);
            user.setCommunity(selectedCommunity);
            user.setBuilding(selectedBuilding);
            user.setRoom(selectedRoom);
            // 构造方法被我简化了，请使用 setter 或者保留原有构造，注意 User 对象需要能被 Gson 序列化

            RetrofitClient.getInstance().getApi().userRegister(user)
                    .enqueue(new Callback<Result<User>>() {
                        @Override
                        public void onResponse(Call<Result<User>> call, Response<Result<User>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Result<User> result = response.body();
                                if (result.getCode() == 200) {
                                    Toast.makeText(ResidentRegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();

                                    // 注册成功后自动跳转或返回登录
                                    User newUser = result.getData();
                                    Intent intent = new Intent(ResidentRegisterActivity.this, ResidentMainActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("user", newUser);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(ResidentRegisterActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ResidentRegisterActivity.this, "注册失败，服务器错误", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Result<User>> call, Throwable t) {
                            Toast.makeText(ResidentRegisterActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.util.SharedPreferencesUtil;

public class ResidentChangePasswordActivity extends AppCompatActivity {

    private EditText etOldPass, etNewPass, etConfirmPass;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_change_password);

        currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        etOldPass = findViewById(R.id.et_old_password);
        etNewPass = findViewById(R.id.et_new_password);
        etConfirmPass = findViewById(R.id.et_confirm_password);
        Button btnConfirm = findViewById(R.id.btn_confirm);

        btnConfirm.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String oldPass = etOldPass.getText().toString().trim();
        String newPass = etNewPass.getText().toString().trim();
        String confirmPass = etConfirmPass.getText().toString().trim();

        if (TextUtils.isEmpty(oldPass) || TextUtils.isEmpty(newPass)) {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!oldPass.equals(currentUser.getPassword())) {
            Toast.makeText(this, "原密码错误", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "两次新密码输入不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新数据库
        currentUser.setPassword(newPass);
        new Thread(() -> {
            AppDatabase.getInstance(this).userDao().update(currentUser);
            SharedPreferencesUtil.saveUser(this, currentUser);
            runOnUiThread(() -> {
                Toast.makeText(this, "密码修改成功", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}
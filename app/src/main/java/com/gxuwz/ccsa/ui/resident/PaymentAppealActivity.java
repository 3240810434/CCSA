package com.gxuwz.ccsa.ui.resident;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.PaymentAppeal;
import com.gxuwz.ccsa.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaymentAppealActivity extends AppCompatActivity {

    private User currentUser;
    private Spinner spinnerAppealType;
    private EditText etPeriod;
    private EditText etAmount;
    private EditText etContent;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_appeal);

        // 获取当前用户信息
        currentUser = (User) getIntent().getSerializableExtra("user");

        if (currentUser == null) {
            Toast.makeText(this, "获取用户信息失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        spinnerAppealType = findViewById(R.id.spinner_appeal_type);
        etPeriod = findViewById(R.id.et_period);
        etAmount = findViewById(R.id.et_amount);
        etContent = findViewById(R.id.et_content);
        btnSubmit = findViewById(R.id.btn_submit);
    }

    private void setupListeners() {
        btnSubmit.setOnClickListener(v -> submitAppeal());
    }

    private void submitAppeal() {
        String appealType = spinnerAppealType.getSelectedItem().toString();
        String period = etPeriod.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        // 输入验证
        if (period.isEmpty() || amountStr.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "请填写完整申诉信息", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的金额", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建申诉记录
        PaymentAppeal appeal = new PaymentAppeal(
                currentUser.getPhone(),
                currentUser.getName(),
                currentUser.getCommunity(),
                currentUser.getBuilding(),
                currentUser.getRoom(),
                appealType,
                content,
                period,
                amount,
                0, // 待处理状态
                System.currentTimeMillis(),
                "", // 回复内容为空
                0, // 回复时间为0
                "" // 处理人为空
        );

        // 保存到数据库
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase.getInstance(this).paymentAppealDao().insert(appeal);

            runOnUiThread(() -> {
                Toast.makeText(this, "申诉提交成功，请等待处理", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}

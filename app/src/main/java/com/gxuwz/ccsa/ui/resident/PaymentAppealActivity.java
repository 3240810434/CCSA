package com.gxuwz.ccsa.ui.resident;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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
    private TextView tvRecords; // 新增

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_appeal);

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
        tvRecords = findViewById(R.id.tv_records); // 绑定新增按钮
    }

    private void setupListeners() {
        btnSubmit.setOnClickListener(v -> submitAppeal());

        // 点击跳转到申诉记录列表
        tvRecords.setOnClickListener(v -> {
            Intent intent = new Intent(this, ResidentAppealListActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });
    }

    private void submitAppeal() {
        String appealType = spinnerAppealType.getSelectedItem() != null ? spinnerAppealType.getSelectedItem().toString() : "其他";
        String period = etPeriod.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String content = etContent.getText().toString().trim();

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

        PaymentAppeal appeal = new PaymentAppeal(
                currentUser.getPhone(), // 假设Phone作为userId
                currentUser.getName(),
                currentUser.getCommunity(),
                currentUser.getBuilding(),
                currentUser.getRoom(),
                appealType,
                content,
                period,
                amount,
                0, // 0-待处理
                System.currentTimeMillis(),
                "",
                0,
                ""
        );

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase.getInstance(this).paymentAppealDao().insert(appeal);
            runOnUiThread(() -> {
                Toast.makeText(this, "申诉提交成功，请在申诉记录中查看进度", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
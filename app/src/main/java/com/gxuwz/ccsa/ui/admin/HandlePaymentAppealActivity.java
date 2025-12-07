package com.gxuwz.ccsa.ui.admin;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.PaymentAppeal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HandlePaymentAppealActivity extends AppCompatActivity {

    private long appealId;
    private String adminAccount;
    private TextView tvAppealInfo;
    private EditText etReplyContent;
    private Button btnApprove;
    private Button btnReject;
    private PaymentAppeal currentAppeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 确保布局文件存在且名称正确
        setContentView(R.layout.activity_handle_payment_appeal);

        appealId = getIntent().getLongExtra("appealId", -1);
        adminAccount = getIntent().getStringExtra("adminAccount");

        if (appealId == -1) {
            Toast.makeText(this, "获取申诉信息失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadAppealData();
        setupListeners();
    }

    private void initViews() {
        // 确保控件ID与布局文件一致
        tvAppealInfo = findViewById(R.id.tv_appeal_info);
        etReplyContent = findViewById(R.id.et_reply_content);
        btnApprove = findViewById(R.id.btn_approve);
        btnReject = findViewById(R.id.btn_reject);
    }

    private void loadAppealData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // 确保PaymentAppealDao中存在getById方法
            currentAppeal = AppDatabase.getInstance(this).paymentAppealDao().getById(appealId);

            runOnUiThread(() -> {
                if (currentAppeal == null) {
                    Toast.makeText(this, "获取申诉信息失败", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                String info = String.format(
                        "申诉人：%s\n地址：%s %s %s\n周期：%s\n金额：%.2f元\n类型：%s\n内容：%s",
                        currentAppeal.getUserName(),
                        currentAppeal.getCommunity(),
                        currentAppeal.getBuilding(),
                        currentAppeal.getRoom(),
                        currentAppeal.getRelatedPeriod(),
                        currentAppeal.getRelatedAmount(),
                        currentAppeal.getAppealType(),
                        currentAppeal.getAppealContent()
                );
                tvAppealInfo.setText(info);
            });
        });
    }

    private void setupListeners() {
        btnApprove.setOnClickListener(v -> handleAppeal(2));
        btnReject.setOnClickListener(v -> handleAppeal(3));
    }

    private void handleAppeal(int status) {
        String reply = etReplyContent.getText().toString().trim();
        if (reply.isEmpty()) {
            Toast.makeText(this, "请输入处理意见", Toast.LENGTH_SHORT).show();
            return;
        }

        currentAppeal.setStatus(status);
        currentAppeal.setReplyContent(reply);
        currentAppeal.setReplyTime(System.currentTimeMillis());
        currentAppeal.setHandler(adminAccount);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // 确保PaymentAppealDao中存在update方法
            AppDatabase.getInstance(this).paymentAppealDao().update(currentAppeal);

            runOnUiThread(() -> {
                Toast.makeText(this, "申诉处理完成", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}

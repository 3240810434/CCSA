package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.AfterSalesRecord;
import com.gxuwz.ccsa.util.DateUtils;

public class ResidentApplyAfterSalesActivity extends AppCompatActivity {

    private Spinner spType, spReason;
    private EditText etDescription;
    private Long orderId;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_apply_after_sales);

        db = AppDatabase.getInstance(this);
        orderId = getIntent().getLongExtra("orderId", -1);

        if (orderId == -1) {
            Toast.makeText(this, "订单参数错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initView();
    }

    private void initView() {
        spType = findViewById(R.id.sp_type);
        spReason = findViewById(R.id.sp_reason);
        etDescription = findViewById(R.id.et_description);

        // 初始化下拉框数据
        String[] types = {"仅退款", "退货退款", "换货"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spType.setAdapter(typeAdapter);

        String[] reasons = {"质量问题", "发错货", "不想要了", "其他"};
        ArrayAdapter<String> reasonAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, reasons);
        spReason.setAdapter(reasonAdapter);

        findViewById(R.id.btn_submit).setOnClickListener(v -> submitApplication());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void submitApplication() {
        String type = spType.getSelectedItem().toString();
        String reason = spReason.getSelectedItem().toString();
        String description = etDescription.getText().toString();

        if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "请简要描述问题", Toast.LENGTH_SHORT).show();
            return;
        }

        // 异步操作数据库
        new Thread(() -> {
            // 1. 插入售后记录
            AfterSalesRecord record = new AfterSalesRecord(
                    orderId, type, reason, description, "", DateUtils.getCurrentDate()
            );
            db.afterSalesRecordDao().insert(record);

            // 2. 更新订单状态为 1 (售后待处理)
            db.orderDao().updateAfterSalesStatus(orderId, 1);

            runOnUiThread(() -> {
                Toast.makeText(this, "申请已提交", Toast.LENGTH_SHORT).show();
                finish(); // 关闭页面，返回订单列表
            });
        }).start();
    }
}
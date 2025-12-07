package com.gxuwz.ccsa.ui.resident;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.PaymentRecordAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.PaymentRecord;
import com.gxuwz.ccsa.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaymentDetailActivity extends AppCompatActivity {

    private static final String TAG = "PaymentDetailActivity";
    private User currentUser;
    private RecyclerView recyclerView;
    private PaymentRecordAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_detail);

        // 获取用户信息
        currentUser = (User) getIntent().getSerializableExtra("user");
        if (currentUser == null) {
            Log.e(TAG, "用户信息获取失败");
            Toast.makeText(this, "用户信息错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d(TAG, "查询缴费记录 - 用户手机号: " + currentUser.getPhone()); // 新增日志：确认查询手机号

        initViews();
        loadPaymentRecords();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_records);
        tvEmpty = findViewById(R.id.tv_empty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaymentRecordAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    /**
     * 加载当前用户的缴费记录（增加详细日志）
     */
    private void loadPaymentRecords() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // 1. 执行查询
                List<PaymentRecord> records = AppDatabase.getInstance(this)
                        .paymentRecordDao()
                        .getByPhone(currentUser.getPhone());

                // 2. 打印查询结果日志
                Log.d(TAG, "查询到的缴费记录数量: " + records.size());
                for (PaymentRecord record : records) {
                    Log.d(TAG, "记录详情 - 周期: " + record.getPeriod() + ", 金额: " + record.getAmount() + ", 手机号: " + record.getPhone());
                }

                // 3. 更新UI
                runOnUiThread(() -> {
                    if (records.isEmpty()) {
                        // 空状态处理
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        Log.d(TAG, "无缴费记录，显示空状态");
                    } else {
                        // 有数据时更新适配器
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.updateData(records);
                        Log.d(TAG, "已更新适配器数据，记录数: " + records.size());
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "加载缴费记录失败", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "加载记录失败，请重试", Toast.LENGTH_SHORT).show()
                );
            } finally {
                executor.shutdown();
            }
        });
    }
}
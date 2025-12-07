// CCSA/app/src/main/java/com/gxuwz/ccsa/ui/admin/PaymentAppealListActivity.java
package com.gxuwz.ccsa.ui.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.PaymentAppealAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.PaymentAppeal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaymentAppealListActivity extends AppCompatActivity {
    private String community;
    private RecyclerView rvAppeals;
    private PaymentAppealAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 修复：布局文件名错误（移除空格）
        setContentView(R.layout.activity_payment_appeal_list);

        community = getIntent().getStringExtra("community");
        initViews();
        loadAppeals();
    }

    private void initViews() {
        // 修复：控件ID引用错误
        rvAppeals = findViewById(R.id.rv_appeals);
        rvAppeals.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadAppeals() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<PaymentAppeal> appeals = AppDatabase.getInstance(this)
                    .paymentAppealDao()
                    .getByCommunity(community);

            runOnUiThread(() -> {
                if (appeals.isEmpty()) {
                    Toast.makeText(this, "暂无申诉记录", Toast.LENGTH_SHORT).show();
                } else {
                    // 修复：适配器引用错误
                    adapter = new PaymentAppealAdapter(appeals, appealId -> {
                        // 修复：方法名拼写错误 getldO() -> getId()
                        Intent intent = new Intent(this, HandlePaymentAppealActivity.class);
                        intent.putExtra("appealId", appealId);
                        startActivity(intent);
                    });
                    // 修复：适配器设置错误
                    rvAppeals.setAdapter(adapter);
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppeals(); // 刷新数据
    }
}

package com.gxuwz.ccsa.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Merchant;
import java.util.List;
import java.util.concurrent.Executors;

public class MerchantAuditListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MerchantAuditAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_audit_list);
        setTitle("商家审核");

        recyclerView = findViewById(R.id.rv_merchant_audit);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // 查询状态为1（审核中）的商家
            // 确保 MerchantDao 中的 findPendingAudits() SQL 是: SELECT * FROM merchant WHERE qualificationStatus = 1
            List<Merchant> pendingList = AppDatabase.getInstance(this).merchantDao().findPendingAudits();

            runOnUiThread(() -> {
                if (pendingList == null || pendingList.isEmpty()) {
                    Toast.makeText(MerchantAuditListActivity.this, "暂无待审核商家", Toast.LENGTH_SHORT).show();
                    // 这里可以设置一个空视图 visible，把 recyclerView 设为 gone
                }

                // 重新创建 adapter 或者调用 adapter.setList(pendingList)
                // 简单起见，这里重新创建 Adapter 确保数据是最新的
                adapter = new MerchantAuditAdapter(this, pendingList);
                recyclerView.setAdapter(adapter);
            });
        });
    }
}
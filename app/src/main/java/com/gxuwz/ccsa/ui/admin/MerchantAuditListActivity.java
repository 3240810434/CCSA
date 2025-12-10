package com.gxuwz.ccsa.ui.admin;

import android.os.Bundle;
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
            List<Merchant> pendingList = AppDatabase.getInstance(this).merchantDao().findPendingAudits();
            runOnUiThread(() -> {
                adapter = new MerchantAuditAdapter(this, pendingList);
                recyclerView.setAdapter(adapter);
            });
        });
    }
}
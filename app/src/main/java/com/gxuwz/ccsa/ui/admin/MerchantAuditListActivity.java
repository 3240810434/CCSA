package com.gxuwz.ccsa.ui.admin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    private String adminCommunity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_audit_list);
        setTitle("商家审核");

        // 获取当前管理员负责的小区
        SharedPreferences sp = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        adminCommunity = sp.getString("admin_community", "");

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
            List<Merchant> pendingList;

            // 如果能获取到管理员小区，则进行筛选；否则显示所有（兼容处理）
            if (adminCommunity != null && !adminCommunity.isEmpty()) {
                // 只要商家选择的小区列表包含当前管理员的小区，就能看到该申请
                pendingList = AppDatabase.getInstance(this).merchantDao().findPendingAuditsByCommunity(adminCommunity);
            } else {
                pendingList = AppDatabase.getInstance(this).merchantDao().findPendingAudits();
            }

            runOnUiThread(() -> {
                if (pendingList == null || pendingList.isEmpty()) {
                    Toast.makeText(MerchantAuditListActivity.this, "暂无待审核商家", Toast.LENGTH_SHORT).show();
                }

                // 重新创建 adapter 确保数据是最新的
                adapter = new MerchantAuditAdapter(this, pendingList);
                recyclerView.setAdapter(adapter);
            });
        });
    }
}
package com.gxuwz.ccsa.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.AdminRepairListAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Repair;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminRepairListActivity extends AppCompatActivity implements AdminRepairListAdapter.OnItemClickListener {
    private RecyclerView rvRepairs;
    private AdminRepairListAdapter adapter;
    private TextView tvTitle;
    private String community;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_repair_list);

        community = getIntent().getStringExtra("community");
        if (community == null || community.isEmpty()) {
            community = "未知小区";
        }

        // 初始化视图
        initViews();

        // 加载待受理数据
        loadPendingRepairData();
    }

    private void initViews() {
        // 顶部导航
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        tvTitle = findViewById(R.id.tv_title);

        // 历史按钮点击事件
        Button btnHistory = findViewById(R.id.btn_history);
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminRepairHistoryActivity.class);
            intent.putExtra("community", community);
            startActivity(intent);
        });

        // 列表
        rvRepairs = findViewById(R.id.rv_repairs);
        rvRepairs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminRepairListAdapter(this, null, this);
        rvRepairs.setAdapter(adapter);
    }

    // 修改：只加载待受理(状态为0)的报修数据
    private void loadPendingRepairData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                List<Repair> repairs;
                // 只查询待受理的报修
                if (!community.equals("未知小区")) {
                    repairs = AppDatabase.getInstance(this).repairDao().getPendingByCommunity(community);
                } else {
                    repairs = AppDatabase.getInstance(this).repairDao().getAllPending();
                }

                // 获取待处理数量
                int pendingCount = repairs != null ? repairs.size() : 0;

                runOnUiThread(() -> {
                    tvTitle.setText("待处理报修：" + pendingCount + "条");

                    if (repairs != null && !repairs.isEmpty()) {
                        adapter.updateData(repairs);
                    } else {
                        Toast.makeText(this, "暂无待处理报修", Toast.LENGTH_SHORT).show();
                        adapter.updateData(null);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "加载失败，请重试", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @Override
    public void onViewClick(Repair repair) {
        Intent intent = new Intent(this, AdminRepairDetailActivity.class);
        intent.putExtra("repair", repair);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPendingRepairData();
    }
}
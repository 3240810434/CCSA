package com.gxuwz.ccsa.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class AdminRepairHistoryActivity extends AppCompatActivity implements AdminRepairListAdapter.OnItemClickListener {
    private RecyclerView rvHistoryRepairs;
    private AdminRepairListAdapter adapter;
    private String community;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_repair_history);

        community = getIntent().getStringExtra("community");
        if (community == null || community.isEmpty()) {
            community = "未知小区";
        }

        // 初始化视图
        initViews();

        // 加载历史数据
        loadHistoryRepairData();
    }

    private void initViews() {
        // 顶部导航
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("历史报修记录");

        // 列表
        rvHistoryRepairs = findViewById(R.id.rv_history_repairs);
        rvHistoryRepairs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminRepairListAdapter(this, null, this);
        rvHistoryRepairs.setAdapter(adapter);
    }

    // 加载已完成(状态为1)的报修数据
    private void loadHistoryRepairData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                List<Repair> repairs;
                // 只查询已完成的报修
                if (!community.equals("未知小区")) {
                    repairs = AppDatabase.getInstance(this).repairDao().getCompletedByCommunity(community);
                } else {
                    repairs = AppDatabase.getInstance(this).repairDao().getAllCompleted();
                }

                runOnUiThread(() -> {
                    if (repairs != null && !repairs.isEmpty()) {
                        adapter.updateData(repairs);
                    } else {
                        Toast.makeText(this, "暂无历史报修记录", Toast.LENGTH_SHORT).show();
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
        // 点击查看详情
        Intent intent = new Intent(this, AdminRepairDetailActivity.class);
        intent.putExtra("repair", repair);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistoryRepairData();
    }
}

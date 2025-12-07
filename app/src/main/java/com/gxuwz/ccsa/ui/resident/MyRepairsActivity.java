package com.gxuwz.ccsa.ui.resident;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.RepairListAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Repair;
import com.gxuwz.ccsa.model.User;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyRepairsActivity extends AppCompatActivity implements RepairListAdapter.OnItemClickListener {
    private User currentUser;
    private RecyclerView rvRepairs;
    private RepairListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_repairs);

        // 获取当前用户
        currentUser = (User) getIntent().getSerializableExtra("user");
        if (currentUser == null) {
            Toast.makeText(this, "用户信息获取失败，请重新登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化视图
        initViews();

        // 加载数据
        loadRepairData();
    }

    private void initViews() {
        // 顶部导航
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("我的报修");

        // 列表
        rvRepairs = findViewById(R.id.rv_repairs);
        rvRepairs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RepairListAdapter(this, null, this);
        rvRepairs.setAdapter(adapter);
    }

    private void loadRepairData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                List<Repair> repairs = AppDatabase.getInstance(this)
                        .repairDao()
                        .getByUserId(currentUser.getPhone());

                runOnUiThread(() -> {
                    if (repairs != null && !repairs.isEmpty()) {
                        adapter.updateData(repairs);
                    } else {
                        Toast.makeText(this, "暂无报修记录", Toast.LENGTH_SHORT).show();
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
    public void onItemClick(Repair repair) {
        // 点击整个卡片进入详情
        Intent intent = new Intent(this, RepairDetailActivity.class);
        intent.putExtra("repair", repair);
        startActivity(intent);
    }

    @Override
    public void onDetailClick(Repair repair) {
        // 点击"查看详情"进入详情
        Intent intent = new Intent(this, RepairDetailActivity.class);
        intent.putExtra("repair", repair);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 重新加载数据
        loadRepairData();
    }
}
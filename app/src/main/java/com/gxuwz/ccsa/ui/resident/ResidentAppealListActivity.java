package com.gxuwz.ccsa.ui.resident;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.ResidentAppealAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.PaymentAppeal;
import com.gxuwz.ccsa.model.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResidentAppealListActivity extends AppCompatActivity {

    private RecyclerView rvAppealList;
    private TextView tvEmpty;
    private User currentUser;
    private ResidentAppealAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_appeal_list);

        currentUser = (User) getIntent().getSerializableExtra("user");
        if (currentUser == null) {
            finish();
            return;
        }

        initViews();
    }

    private void initViews() {
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("我的申诉记录");

        // 返回按钮逻辑
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        rvAppealList = findViewById(R.id.rv_appeal_list);
        tvEmpty = findViewById(R.id.tv_empty);
        rvAppealList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // 根据 userId 查询数据 (Dao中已有 getByUserId 方法)
            List<PaymentAppeal> list = AppDatabase.getInstance(this)
                    .paymentAppealDao()
                    .getByUserId(currentUser.getPhone()); // 注意: 这里使用Phone作为ID，需保持一致

            runOnUiThread(() -> {
                if (list == null || list.isEmpty()) {
                    rvAppealList.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvAppealList.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.GONE);

                    // 初始化适配器
                    adapter = new ResidentAppealAdapter(this, list);
                    adapter.setOnItemClickListener(appeal -> {
                        // 跳转详情页查看管理员回复
                        Intent intent = new Intent(this, ResidentAppealDetailActivity.class);
                        intent.putExtra("appeal", appeal);
                        startActivity(intent);
                    });
                    rvAppealList.setAdapter(adapter);
                }
            });
        });
    }
}

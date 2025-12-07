// CCSA/app/src/main/java/com/gxuwz/ccsa/ui/resident/ResidentListActivity.java
package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.ResidentListAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.User;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResidentListActivity extends AppCompatActivity {

    private String community; // 当前小区
    private RecyclerView rvResidents;
    private ResidentListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_list);

        // 初始化RecyclerView
        rvResidents = findViewById(R.id.rv_residents);
        rvResidents.setLayoutManager(new LinearLayoutManager(this));

        // 接收小区信息
        community = getIntent().getStringExtra("community");
        if (community == null || community.isEmpty()) {
            Toast.makeText(this, "小区信息错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 查询该小区的居民数据（子线程中执行）
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // 调用UserDao中已定义的findByCommunity方法
            List<User> residents = AppDatabase.getInstance(this)
                    .userDao()
                    .findByCommunity(community);

            // 主线程更新UI
            runOnUiThread(() -> {
                if (residents.isEmpty()) {
                    Toast.makeText(this, "该小区暂无注册居民", Toast.LENGTH_SHORT).show();
                } else {
                    adapter = new ResidentListAdapter(residents);
                    rvResidents.setAdapter(adapter);
                }
            });
        });
    }
}
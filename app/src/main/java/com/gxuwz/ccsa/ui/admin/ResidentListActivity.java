// CCSA/app/src/main/java/com/gxuwz/ccsa/ui/admin/ResidentListActivity.java
package com.gxuwz.ccsa.ui.admin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.ResidentListAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.User;
import java.util.List;

public class ResidentListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private String mCommunity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_list);

        // 1. 获取传递的小区信息
        mCommunity = getIntent().getStringExtra("community");
        if (mCommunity == null || mCommunity.isEmpty()) {
            Toast.makeText(this, "未获取到小区信息", Toast.LENGTH_SHORT).show();
            finish(); // 关闭页面，避免错误
            return;
        }

        // 2. 初始化列表
        mRecyclerView = findViewById(R.id.rv_residents);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 3. 加载数据（使用已验证的查询方法）
        loadResidents();
    }

    private void loadResidents() {
        new Thread(() -> {
            try {
                // 关键：使用正确的查询方法（若findResidentsByCommunitySorted不存在则替换为findByCommunity）
                List<User> residents = AppDatabase.getInstance(this)
                        .userDao()
                        .findByCommunity(mCommunity); // 改用居民包中已验证的方法

                runOnUiThread(() -> {
                    if (residents.isEmpty()) {
                        Toast.makeText(this, "该小区暂无居民注册", Toast.LENGTH_SHORT).show();
                    } else {
                        mRecyclerView.setAdapter(new ResidentListAdapter(residents));
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "加载失败：" + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
                e.printStackTrace();
            }
        }).start();
    }
}

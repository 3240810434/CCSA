package com.gxuwz.ccsa.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.ResidentListAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.User;
import java.util.List;

public class ResidentListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private String mCommunity;
    private ResidentListAdapter mAdapter;
    private List<User> mResidents; // 维护当前列表数据

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_list);

        // 1. 获取传递的小区信息
        mCommunity = getIntent().getStringExtra("community");
        if (mCommunity == null || mCommunity.isEmpty()) {
            Toast.makeText(this, "未获取到小区信息", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. 初始化视图
        mRecyclerView = findViewById(R.id.rv_residents);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 绑定返回按钮
        ImageView ivBack = findViewById(R.id.iv_back);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        // 3. 加载数据
        loadResidents();
    }

    private void loadResidents() {
        new Thread(() -> {
            try {
                // 查询该小区的居民
                mResidents = AppDatabase.getInstance(this)
                        .userDao()
                        .findByCommunity(mCommunity);

                runOnUiThread(() -> {
                    if (mResidents.isEmpty()) {
                        Toast.makeText(this, "该小区暂无居民注册", Toast.LENGTH_SHORT).show();
                        // 即使没有数据也要设置空适配器或清空，防止显示旧数据
                        mRecyclerView.setAdapter(null);
                    } else {
                        // 初始化适配器并传入监听器
                        // 修改：删除了 onChatClick 回调
                        mAdapter = new ResidentListAdapter(mResidents, new ResidentListAdapter.OnItemClickListener() {
                            @Override
                            public void onDeleteClick(User user) {
                                showDeleteConfirmDialog(user);
                            }
                        });
                        mRecyclerView.setAdapter(mAdapter);
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

    // 显示注销确认弹窗
    private void showDeleteConfirmDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("注销账号")
                .setMessage("确定要注销居民 " + user.getName() + " 的账号吗？此操作不可恢复。")
                .setPositiveButton("确定", (dialog, which) -> deleteUser(user))
                .setNegativeButton("取消", null)
                .show();
    }

    // 执行删除操作
    private void deleteUser(User user) {
        new Thread(() -> {
            AppDatabase.getInstance(this).userDao().delete(user);
            runOnUiThread(() -> {
                Toast.makeText(this, "已注销该用户", Toast.LENGTH_SHORT).show();
                // 重新加载列表
                loadResidents();
            });
        }).start();
    }

    // 已删除 goToChat 方法
}
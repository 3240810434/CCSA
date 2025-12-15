package com.gxuwz.ccsa.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.MerchantManagementAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Merchant;
import java.util.List;

public class MerchantListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private String mCommunity;
    private MerchantManagementAdapter mAdapter;
    private List<Merchant> mMerchants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_list);

        // 1. 获取传递的小区信息
        mCommunity = getIntent().getStringExtra("community");
        if (mCommunity == null || mCommunity.isEmpty()) {
            Toast.makeText(this, "未获取到小区信息", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. 初始化视图
        mRecyclerView = findViewById(R.id.rv_merchants);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 绑定返回按钮
        ImageView ivBack = findViewById(R.id.iv_back);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        // 3. 加载数据
        loadMerchants();
    }

    private void loadMerchants() {
        new Thread(() -> {
            try {
                // 查询该小区的商家
                mMerchants = AppDatabase.getInstance(this)
                        .merchantDao()
                        .findByCommunity(mCommunity);

                runOnUiThread(() -> {
                    if (mMerchants == null || mMerchants.isEmpty()) {
                        Toast.makeText(this, "该小区暂无商家入驻", Toast.LENGTH_SHORT).show();
                        mRecyclerView.setAdapter(null);
                    } else {
                        // 初始化适配器
                        mAdapter = new MerchantManagementAdapter(mMerchants, this::showDeleteConfirmDialog);
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
    private void showDeleteConfirmDialog(Merchant merchant) {
        new AlertDialog.Builder(this)
                .setTitle("注销商家账号")
                .setMessage("确定要注销商家 [" + merchant.getMerchantName() + "] 吗？\n联系人：" + merchant.getContactName() + "\n此操作不可恢复，且该商家的所有商品和服务数据可能会受到影响。")
                .setPositiveButton("确定注销", (dialog, which) -> deleteMerchant(merchant))
                .setNegativeButton("取消", null)
                .show();
    }

    // 执行删除操作
    private void deleteMerchant(Merchant merchant) {
        new Thread(() -> {
            AppDatabase.getInstance(this).merchantDao().delete(merchant);
            runOnUiThread(() -> {
                Toast.makeText(this, "已注销该商家", Toast.LENGTH_SHORT).show();
                // 重新加载列表
                loadMerchants();
            });
        }).start();
    }
}
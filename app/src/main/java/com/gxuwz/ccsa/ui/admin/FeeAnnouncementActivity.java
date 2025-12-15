// 文件路径: app/src/main/java/com/gxuwz/ccsa/ui/admin/FeeAnnouncementActivity.java
package com.gxuwz.ccsa.ui.admin;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.FeeAnnouncementAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.FeeAnnouncement;

import java.util.ArrayList;
import java.util.List;

public class FeeAnnouncementActivity extends AppCompatActivity {

    private String community;
    private String adminAccount;
    private RecyclerView recyclerView;
    private FeeAnnouncementAdapter adapter;
    private List<FeeAnnouncement> announcementList = new ArrayList<>();
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fee_announcement); // 使用新的列表布局

        community = getIntent().getStringExtra("community");
        adminAccount = getIntent().getStringExtra("adminAccount");
        db = AppDatabase.getInstance(this);

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // 每次回到页面刷新列表
    }

    private void initViews() {
        // 标题栏返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 标题栏发布按钮
        findViewById(R.id.btn_add_announcement).setOnClickListener(v -> {
            Intent intent = new Intent(this, FeeAnnouncementPublishActivity.class);
            intent.putExtra("community", community);
            intent.putExtra("adminAccount", adminAccount);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FeeAnnouncementAdapter(this, announcementList);
        // 点击查看详情
        adapter.setOnItemClickListener(announcement -> {
            Intent intent = new Intent(this, FeeAnnouncementDetailActivity.class);
            intent.putExtra("announcement", announcement);
            startActivity(intent);
        });
        // 长按删除
        adapter.setOnItemLongClickListener(announcement -> showDeleteDialog(announcement));
        recyclerView.setAdapter(adapter);
    }

    private void loadData() {
        new Thread(() -> {
            List<FeeAnnouncement> list = db.feeAnnouncementDao().getByCommunity(community);
            runOnUiThread(() -> {
                announcementList.clear();
                if (list != null) {
                    announcementList.addAll(list);
                }
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void showDeleteDialog(FeeAnnouncement announcement) {
        new AlertDialog.Builder(this)
                .setTitle("删除公示")
                .setMessage("确定要删除这条费用公示吗？")
                .setPositiveButton("删除", (dialog, which) -> deleteAnnouncement(announcement))
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteAnnouncement(FeeAnnouncement announcement) {
        new Thread(() -> {
            db.feeAnnouncementDao().delete(announcement);
            runOnUiThread(() -> {
                Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                loadData();
            });
        }).start();
    }
}
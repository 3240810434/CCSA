package com.gxuwz.ccsa.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.ImageGridAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Repair;
import com.gxuwz.ccsa.util.DateUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminRepairDetailActivity extends AppCompatActivity {
    private Repair repair;
    private Button btnStatusOperation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_repair_detail);

        // 获取报修信息
        repair = (Repair) getIntent().getSerializableExtra("repair");
        if (repair == null) {
            finish();
            return;
        }

        // 初始化视图
        initViews();
    }

    private void initViews() {
        // 顶部导航
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("报修详情");

        // 报修信息
        TextView tvRepairNo = findViewById(R.id.tv_repair_no);
        TextView tvStatus = findViewById(R.id.tv_status);
        TextView tvSubmitTime = findViewById(R.id.tv_submit_time);
        TextView tvTitleContent = findViewById(R.id.tv_title_content);
        TextView tvDescription = findViewById(R.id.tv_description);
        TextView tvLocation = findViewById(R.id.tv_location);
        TextView tvContact = findViewById(R.id.tv_contact);

        tvRepairNo.setText("报修单号：" + repair.getRepairNo());
        tvSubmitTime.setText("提交时间：" + DateUtils.formatTime(repair.getSubmitTime()));
        tvTitleContent.setText(repair.getTitle());
        tvDescription.setText(repair.getDescription());
        tvLocation.setText("地点：" + repair.getCommunity() + " " + repair.getBuilding() + repair.getRoom());
        tvContact.setText("联系人：" + repair.getUserName() + " " + repair.getUserPhone());

        // 状态
        btnStatusOperation = findViewById(R.id.btn_status_operation);
        updateStatusUI();

        // 状态操作按钮点击事件
        btnStatusOperation.setOnClickListener(v -> {
            if (repair.getStatus() == 0) {
                // 标记为已完成
                markAsCompleted();
            }
        });

        // 图片展示
        RecyclerView rvImages = findViewById(R.id.rv_images);
        rvImages.setLayoutManager(new GridLayoutManager(this, 3));

        // 处理图片路径
        List<String> imagePaths = new ArrayList<>();
        if (repair.getImageUrls() != null && !repair.getImageUrls().isEmpty()) {
            imagePaths = Arrays.asList(repair.getImageUrls().split(","));
        }

        // 使用ImageGridAdapter但禁用添加和删除功能
        ImageGridAdapter adapter = new ImageGridAdapter(this, new ImageGridAdapter.OnItemClickListener() {
            @Override
            public void onAddClick() {
                // 空实现，不允许添加
            }

            @Override
            public void onDeleteClick(int position) {
                // 空实现，不允许删除
            }
        });

        // 添加图片
        for (String path : imagePaths) {
            adapter.addImage(path);
        }

        rvImages.setAdapter(adapter);
    }

    // 更新状态UI
    private void updateStatusUI() {
        TextView tvStatus = findViewById(R.id.tv_status);

        if (repair.getStatus() == 0) {
            tvStatus.setText("待受理");
            tvStatus.setBackgroundResource(R.drawable.status_pending);
            btnStatusOperation.setText("标记已维修");
            btnStatusOperation.setVisibility(View.VISIBLE);
        } else {
            tvStatus.setText("已维修");
            tvStatus.setBackgroundResource(R.drawable.status_completed);
            btnStatusOperation.setVisibility(View.GONE);

            // 显示完成时间
            TextView tvCompleteTime = findViewById(R.id.tv_complete_time);
            tvCompleteTime.setVisibility(View.VISIBLE);
            tvCompleteTime.setText("完成时间：" + DateUtils.formatTime(repair.getCompleteTime()));
        }
    }

    // 标记为已完成
    private void markAsCompleted() {
        repair.setStatus(1);
        repair.setCompleteTime(new Date().getTime());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                AppDatabase.getInstance(this).repairDao().update(repair);

                runOnUiThread(() -> {
                    Toast.makeText(this, "已标记为完成", Toast.LENGTH_SHORT).show();
                    updateStatusUI();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "操作失败，请重试", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}
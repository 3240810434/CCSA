package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.ImageGridAdapter;
import com.gxuwz.ccsa.model.Repair;
import com.gxuwz.ccsa.util.DateUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RepairDetailActivity extends AppCompatActivity {
    private Repair repair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repair_detail);

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
        if (repair.getStatus() == 0) {
            tvStatus.setText("处理中");
            tvStatus.setBackgroundResource(R.drawable.status_processing);
        } else {
            tvStatus.setText("已完成");
            tvStatus.setBackgroundResource(R.drawable.status_completed);
        }

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
}
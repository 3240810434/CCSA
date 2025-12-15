// 文件路径: app/src/main/java/com/gxuwz/ccsa/ui/admin/FeeAnnouncementDetailActivity.java
package com.gxuwz.ccsa.ui.admin;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.FeeAnnouncement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FeeAnnouncementDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fee_announcement_detail);

        FeeAnnouncement announcement = (FeeAnnouncement) getIntent().getSerializableExtra("announcement");

        initViews(announcement);
    }

    private void initViews(FeeAnnouncement data) {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        if (data == null) return;

        TextView tvTitle = findViewById(R.id.tv_detail_title);
        TextView tvPublishTime = findViewById(R.id.tv_detail_publish_time);
        TextView tvPeriod = findViewById(R.id.tv_detail_period);
        TextView tvContent = findViewById(R.id.tv_detail_content);

        tvTitle.setText(data.getTitle());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        tvPublishTime.setText("发布时间：" + sdf.format(new Date(data.getPublishTime())));

        tvPeriod.setText("公示周期：" + data.getStartTime() + " 至 " + data.getEndTime());
        tvContent.setText(data.getContent());
    }
}

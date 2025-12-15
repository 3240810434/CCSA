package com.gxuwz.ccsa.ui.resident;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.PaymentAppeal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ResidentAppealDetailActivity extends AppCompatActivity {

    private PaymentAppeal appeal;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_appeal_detail);

        appeal = (PaymentAppeal) getIntent().getSerializableExtra("appeal");
        if (appeal == null) {
            finish();
            return;
        }

        initViews();
    }

    private void initViews() {
        // 顶部标题
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("申诉详情");
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        // 绑定数据
        TextView tvType = findViewById(R.id.tv_detail_type);
        TextView tvPeriod = findViewById(R.id.tv_detail_period);
        TextView tvAmount = findViewById(R.id.tv_detail_amount);
        TextView tvSubmitTime = findViewById(R.id.tv_detail_submit_time);
        TextView tvContent = findViewById(R.id.tv_detail_content);

        // 管理员回复部分
        LinearLayout layoutReply = findViewById(R.id.layout_reply);
        TextView tvReplyContent = findViewById(R.id.tv_reply_content);
        TextView tvReplyTime = findViewById(R.id.tv_reply_time);
        TextView tvHandler = findViewById(R.id.tv_handler);

        tvType.setText(appeal.getAppealType());
        tvPeriod.setText("涉及周期：" + appeal.getRelatedPeriod());
        tvAmount.setText(String.format("涉及金额：¥%.2f", appeal.getRelatedAmount()));
        tvSubmitTime.setText("提交时间：" + sdf.format(new Date(appeal.getSubmitTime())));
        tvContent.setText(appeal.getAppealContent());

        // 如果已经处理（状态不是0待处理），则显示回复区域
        if (appeal.getStatus() != 0) {
            layoutReply.setVisibility(View.VISIBLE);

            // 根据状态设置回复标题颜色
            TextView tvResultTitle = findViewById(R.id.tv_result_title);
            if(appeal.getStatus() == 2) {
                tvResultTitle.setText("处理结果：已通过");
                tvResultTitle.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if (appeal.getStatus() == 3) {
                tvResultTitle.setText("处理结果：已驳回");
                tvResultTitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }

            tvReplyContent.setText("管理员回复：" + (appeal.getReplyContent() == null ? "无回复内容" : appeal.getReplyContent()));

            if(appeal.getReplyTime() > 0) {
                tvReplyTime.setText("处理时间：" + sdf.format(new Date(appeal.getReplyTime())));
            }
            tvHandler.setText("处理人：" + (appeal.getHandler() == null ? "管理员" : appeal.getHandler()));
        } else {
            layoutReply.setVisibility(View.GONE);
        }
    }
}
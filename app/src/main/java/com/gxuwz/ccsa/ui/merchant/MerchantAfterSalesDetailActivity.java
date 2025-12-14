package com.gxuwz.ccsa.ui.merchant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.AfterSalesRecord;
import com.gxuwz.ccsa.model.Order;
import com.gxuwz.ccsa.ui.resident.ChatActivity;

public class MerchantAfterSalesDetailActivity extends AppCompatActivity {

    private Long orderId;
    private AppDatabase db;
    private Order currentOrder;
    private AfterSalesRecord currentRecord;

    private TextView tvType, tvReason, tvDesc, tvTime, tvStatusHint;
    private LinearLayout layoutActions;
    private Button btnAgree, btnReject, btnContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_after_sales_detail);

        orderId = getIntent().getLongExtra("orderId", -1);
        db = AppDatabase.getInstance(this);

        initView();
        loadData();
    }

    private void initView() {
        tvType = findViewById(R.id.tv_record_type);
        tvReason = findViewById(R.id.tv_record_reason);
        tvDesc = findViewById(R.id.tv_record_desc);
        tvTime = findViewById(R.id.tv_record_time);
        tvStatusHint = findViewById(R.id.tv_status_hint);
        layoutActions = findViewById(R.id.layout_actions);
        btnAgree = findViewById(R.id.btn_agree);
        btnReject = findViewById(R.id.btn_reject);
        btnContact = findViewById(R.id.btn_contact);

        btnAgree.setOnClickListener(v -> showAgreeDialog());
        btnReject.setOnClickListener(v -> showRejectDialog());

        // 联系买家逻辑
        btnContact.setOnClickListener(v -> {
            if (currentOrder != null) {
                Intent intent = new Intent(this, ChatActivity.class);
                // 这里的Intent参数需要根据你的ChatActivity实际需求调整
                intent.putExtra("targetId", currentOrder.residentId);
                intent.putExtra("name", currentOrder.residentName);
                startActivity(intent);
            }
        });
    }

    private void loadData() {
        new Thread(() -> {
            currentOrder = db.orderDao().getOrderById(orderId);
            currentRecord = db.afterSalesRecordDao().getRecordByOrderId(orderId);

            runOnUiThread(() -> {
                if (currentOrder == null || currentRecord == null) {
                    Toast.makeText(this, "数据加载失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                tvType.setText("类型: " + currentRecord.type);
                tvReason.setText("原因: " + currentRecord.reason);
                tvDesc.setText("描述: " + currentRecord.description);
                tvTime.setText("申请时间: " + currentRecord.createTime);

                updateUIByStatus(currentOrder.afterSalesStatus);
            });
        }).start();
    }

    private void updateUIByStatus(int status) {
        if (status == 1) { // 待处理
            tvStatusHint.setText("当前状态：待处理");
            layoutActions.setVisibility(View.VISIBLE);
        } else if (status == 3) {
            tvStatusHint.setText("已同意退款");
            layoutActions.setVisibility(View.GONE);
        } else if (status == 4) {
            tvStatusHint.setText("已拒绝申请");
            layoutActions.setVisibility(View.GONE);
        } else {
            tvStatusHint.setText("协商中 / 其他");
            layoutActions.setVisibility(View.GONE);
        }
    }

    private void showAgreeDialog() {
        new AlertDialog.Builder(this)
                .setTitle("确认同意")
                .setMessage("同意后将自动退款给用户，订单售后状态变更为成功。")
                .setPositiveButton("确认", (dialog, which) -> {
                    updateStatus(3, "商家已同意");
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showRejectDialog() {
        final EditText input = new EditText(this);
        input.setHint("请输入拒绝理由 (必填)");
        new AlertDialog.Builder(this)
                .setTitle("拒绝申请")
                .setView(input)
                .setPositiveButton("确认拒绝", (dialog, which) -> {
                    String reason = input.getText().toString();
                    if (reason.isEmpty()) {
                        Toast.makeText(this, "拒绝理由不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateStatus(4, reason);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateStatus(int newStatus, String reply) {
        new Thread(() -> {
            // 1. 更新订单表
            db.orderDao().updateAfterSalesStatus(orderId, newStatus);
            // 2. 更新记录表的商家回复
            if (currentRecord != null) {
                currentRecord.merchantReply = reply;
                db.afterSalesRecordDao().update(currentRecord);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show();
                loadData(); // 刷新界面
            });
        }).start();
    }
}
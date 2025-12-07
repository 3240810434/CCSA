package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.PaymentRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentRecordAdapter extends RecyclerView.Adapter<PaymentRecordAdapter.ViewHolder> {

    private Context context;
    private List<PaymentRecord> recordList;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public PaymentRecordAdapter(Context context, List<PaymentRecord> recordList) {
        this.context = context;
        this.recordList = recordList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_payment_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentRecord record = recordList.get(position);
        if (record != null) {
            // 移除对 tvHouseInfo 的引用（布局中无此控件）

            // 设置缴费周期
            holder.tvPeriod.setText("缴费周期：" + record.getPeriod());

            // 设置缴费金额
            holder.tvAmount.setText(String.format("%.2f元", record.getAmount()));

            // 关键修复：将状态值转换为文本显示
            int status = record.getStatus();
            if (status == 1) {
                holder.tvStatus.setText("已缴");
            } else {
                holder.tvStatus.setText("未知状态");
            }

            // 设置支付时间
            if (record.getPayTime() > 0) {
                holder.tvTime.setText(sdf.format(new Date(record.getPayTime())));
            } else {
                holder.tvTime.setText("暂无时间");
            }

            // 设置收据编号
            holder.tvReceiptNo.setText(record.getReceiptNumber());
        }
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    // 确保updateData方法存在
    public void updateData(List<PaymentRecord> newRecords) {
        this.recordList.clear();
        this.recordList.addAll(newRecords);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // 移除 tvHouseInfo 的定义（布局中无此控件）
        TextView tvPeriod;
        TextView tvAmount;
        TextView tvStatus;
        TextView tvTime;
        TextView tvReceiptNo;

        public ViewHolder(View itemView) {
            super(itemView);
            // 移除 tvHouseInfo 的初始化（布局中无此控件）
            tvPeriod = itemView.findViewById(R.id.tv_period);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvReceiptNo = itemView.findViewById(R.id.tv_receipt);
        }
    }
}
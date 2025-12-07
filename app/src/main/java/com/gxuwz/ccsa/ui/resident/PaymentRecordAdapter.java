// CCSA/app/src/main/java/com/gxuwz/ccsa/ui/resident/PaymentRecordAdapter.java
package com.gxuwz.ccsa.ui.resident;

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
import java.util.List;
import java.util.Locale;
import java.util.Date;
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentRecord record = recordList.get(position);
        holder.tvPeriod.setText("缴费周期：" + record.getPeriod());
        holder.tvAmount.setText("金额：" + record.getAmount() + "元");

        // 状态比较改为整数比较（与实体类保持一致）
        String status = record.getStatus() == 1 ? "已支付" : "未支付";
        holder.tvStatus.setText("状态：" + status);

        // 移除支付方式相关代码（实体类中无该字段）
        holder.tvMethod.setVisibility(View.GONE);

        // 修正时间获取方法
        if (record.getPayTime() > 0) {
            holder.tvTime.setText("支付时间：" + sdf.format(new Date(record.getPayTime())));
        } else {
            holder.tvTime.setText("支付时间：暂无记录");
        }
        holder.tvReceipt.setText("收据编号：" + record.getReceiptNumber());
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public void updateData(List<PaymentRecord> newRecords) {
        this.recordList.clear();
        this.recordList.addAll(newRecords);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPeriod;
        TextView tvAmount;
        TextView tvStatus;
        TextView tvMethod;
        TextView tvTime;
        TextView tvReceipt;

        public ViewHolder(View itemView) {
            super(itemView);
            tvPeriod = itemView.findViewById(R.id.tv_period);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvMethod = itemView.findViewById(R.id.tv_method);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvReceipt = itemView.findViewById(R.id.tv_receipt);
        }
    }
}
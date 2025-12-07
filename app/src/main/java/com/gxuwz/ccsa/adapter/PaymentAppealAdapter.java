// CCSA/app/src/main/java/com/gxuwz/ccsa/adapter/PaymentAppealAdapter.java
package com.gxuwz.ccsa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.PaymentAppeal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PaymentAppealAdapter extends RecyclerView.Adapter<PaymentAppealAdapter.ViewHolder> {
    private List<PaymentAppeal> appeals;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(long appealId);
    }

    public PaymentAppealAdapter(List<PaymentAppeal> appeals, OnItemClickListener listener) {
        this.appeals = appeals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_appeal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentAppeal appeal = appeals.get(position);
        holder.tvUser.setText("申诉人：" + appeal.getUserName());
        holder.tvType.setText("类型：" + appeal.getAppealType());
        holder.tvPeriod.setText("周期：" + appeal.getRelatedPeriod());
        holder.tvTime.setText(sdf.format(appeal.getSubmitTime()));

        // 显示状态
        String status;
        switch (appeal.getStatus()) {
            case 0: status = "待处理"; break;
            case 1: status = "处理中"; break;
            case 2: status = "已解决"; break;
            case 3: status = "已驳回"; break;
            default: status = "未知";
        }
        holder.tvStatus.setText("状态：" + status);

        // 点击事件
        holder.itemView.setOnClickListener(v -> listener.onItemClick(appeal.getId()));
    }

    @Override
    public int getItemCount() {
        return appeals.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvType, tvPeriod, tvTime, tvStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tv_appeal_user);
            tvType = itemView.findViewById(R.id.tv_appeal_type);
            tvPeriod = itemView.findViewById(R.id.tv_appeal_period);
            tvTime = itemView.findViewById(R.id.tv_appeal_time);
            tvStatus = itemView.findViewById(R.id.tv_appeal_status);
        }
    }
}

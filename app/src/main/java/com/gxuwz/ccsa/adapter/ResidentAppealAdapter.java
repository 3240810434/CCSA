package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.PaymentAppeal;
import com.gxuwz.ccsa.util.DateUtils; // 假设有这个工具类，如果没有可用 SimpleDateFormat

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResidentAppealAdapter extends RecyclerView.Adapter<ResidentAppealAdapter.ViewHolder> {

    private Context context;
    private List<PaymentAppeal> list;
    private OnItemClickListener listener;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public ResidentAppealAdapter(Context context, List<PaymentAppeal> list) {
        this.context = context;
        this.list = list;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_resident_appeal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentAppeal item = list.get(position);

        holder.tvType.setText(item.getAppealType());
        holder.tvDate.setText(sdf.format(new Date(item.getSubmitTime())));
        holder.tvContent.setText(item.getAppealContent());

        // 处理状态显示
        switch (item.getStatus()) {
            case 0: // 待处理
                holder.tvStatus.setText("待处理");
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800")); // 橙色
                break;
            case 1: // 处理中
                holder.tvStatus.setText("处理中");
                holder.tvStatus.setTextColor(Color.parseColor("#2196F3")); // 蓝色
                break;
            case 2: // 已解决
                holder.tvStatus.setText("已通过");
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // 绿色
                break;
            case 3: // 已驳回
                holder.tvStatus.setText("已驳回");
                holder.tvStatus.setTextColor(Color.parseColor("#F44336")); // 红色
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDate, tvStatus, tvContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tv_type);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvContent = itemView.findViewById(R.id.tv_content);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(PaymentAppeal appeal);
    }
}
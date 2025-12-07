package com.gxuwz.ccsa.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.PaymentItem;

import java.text.DecimalFormat;
import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {

    private List<PaymentItem> paymentList;
    private DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

    public PaymentAdapter(List<PaymentItem> paymentList) {
        this.paymentList = paymentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentItem item = paymentList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBuildingRoom, tvOwner, tvPhone, tvPeriod, tvReceivable, tvStatus, tvDate;
        View statusIndicator;
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBuildingRoom = itemView.findViewById(R.id.tv_building_room);
            tvOwner = itemView.findViewById(R.id.tv_owner);
            tvPhone = itemView.findViewById(R.id.tv_phone); // 新增电话号码控件
            tvPeriod = itemView.findViewById(R.id.tv_period);
            tvReceivable = itemView.findViewById(R.id.tv_receivable);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvDate = itemView.findViewById(R.id.tv_date);
            statusIndicator = itemView.findViewById(R.id.status_indicator);
        }

        public void bind(PaymentItem item) {
            // 显示楼栋-房号
            tvBuildingRoom.setText(item.getBuilding() + "-" + item.getRoom());
            // 显示业主姓名
            tvOwner.setText(item.getOwner());
            // 显示完整电话号码（不隐藏）
            tvPhone.setText(item.getPhone());
            // 显示缴费周期
            tvPeriod.setText(item.getPeriod());
            // 显示应收金额
            tvReceivable.setText("¥" + decimalFormat.format(item.getReceivable()));
            // 显示缴费状态
            tvStatus.setText(item.getStatus());

            // 设置状态样式
            if ("已缴".equals(item.getStatus())) {
                statusIndicator.setBackgroundColor(Color.parseColor("#4CAF50"));
                tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                tvStatus.setText("已缴 ✔");
                tvDate.setVisibility(View.VISIBLE);
                tvDate.setText(item.getDate());
                itemView.setBackgroundColor(Color.parseColor("#f0fff4"));
            } else {
                statusIndicator.setBackgroundColor(Color.parseColor("#F44336"));
                tvStatus.setTextColor(Color.parseColor("#F44336"));
                tvStatus.setText("未缴 ❌");
                tvDate.setVisibility(View.GONE);
                itemView.setBackgroundColor(Color.parseColor("#fff0f0"));
            }
        }
    }
}
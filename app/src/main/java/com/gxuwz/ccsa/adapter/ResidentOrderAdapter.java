package com.gxuwz.ccsa.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.Order;

import java.util.List;

public class ResidentOrderAdapter extends RecyclerView.Adapter<ResidentOrderAdapter.OrderViewHolder> {

    private List<Order> orderList;

    public ResidentOrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    public void updateList(List<Order> newList) {
        this.orderList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resident_order_card, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvMerchantName.setText(order.merchantName != null ? order.merchantName : "未知商家");
        holder.tvStatus.setText(order.status);

        // 状态颜色处理
        if ("待接单".equals(order.status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#FF9800")); // Orange
        } else if ("已完成".equals(order.status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else if ("配送中".equals(order.status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#2196F3")); // Blue
        } else {
            holder.tvStatus.setTextColor(Color.GRAY);
        }

        holder.tvProductName.setText(order.productName);

        // 显示规格或服务数量
        if ("服务".equals(order.productType) || "SERVICE".equals(order.productType)) {
            holder.tvSpecInfo.setText("服务数量：" + order.serviceCount);
        } else {
            holder.tvSpecInfo.setText("规格：" + (order.selectedSpec != null ? order.selectedSpec : "默认"));
        }

        holder.tvPayAmount.setText("¥ " + order.payAmount);
        holder.tvPayMethod.setText(order.paymentMethod != null ? order.paymentMethod : "在线支付");

        holder.tvCreateTime.setText("下单时间：" + order.createTime);
        holder.tvOrderNo.setText("订单号：" + order.orderNo);

        if (order.productImageUrl != null) {
            Glide.with(holder.itemView.getContext())
                    .load(order.productImageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.ivProductImg);
        }
    }

    @Override
    public int getItemCount() {
        return orderList == null ? 0 : orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvMerchantName, tvStatus, tvProductName, tvSpecInfo;
        TextView tvPayAmount, tvPayMethod, tvCreateTime, tvOrderNo;
        ImageView ivProductImg;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMerchantName = itemView.findViewById(R.id.tv_merchant_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvSpecInfo = itemView.findViewById(R.id.tv_spec_info);
            tvPayAmount = itemView.findViewById(R.id.tv_pay_amount);
            tvPayMethod = itemView.findViewById(R.id.tv_pay_method);
            tvCreateTime = itemView.findViewById(R.id.tv_create_time);
            tvOrderNo = itemView.findViewById(R.id.tv_order_no);
            ivProductImg = itemView.findViewById(R.id.iv_product_img);
        }
    }
}
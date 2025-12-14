package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.Order;
import com.gxuwz.ccsa.ui.resident.ResidentApplyAfterSalesActivity;

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
        Context context = holder.itemView.getContext();

        holder.tvMerchantName.setText(order.merchantName != null ? order.merchantName : "未知商家");
        holder.tvStatus.setText(order.status);

        // 状态颜色处理
        if ("待接单".equals(order.status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#FF9800")); // Orange
            holder.layoutActionButtons.setVisibility(View.GONE);
        } else if ("已完成".equals(order.status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
            holder.layoutActionButtons.setVisibility(View.VISIBLE); // 显示操作按钮
        } else if ("配送中".equals(order.status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#2196F3")); // Blue
            holder.layoutActionButtons.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setTextColor(Color.GRAY);
            holder.layoutActionButtons.setVisibility(View.GONE);
        }

        holder.tvProductName.setText(order.productName);

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
            Glide.with(context)
                    .load(order.productImageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.ivProductImg);
        }

        // --- 核心：售后状态流转逻辑 ---
        if ("已完成".equals(order.status)) {
            // 根据 afterSalesStatus 改变按钮状态
            switch (order.afterSalesStatus) {
                case 0: // 无售后 -> 显示“申请售后”
                    holder.btnApply.setText("申请售后");
                    holder.btnApply.setTextColor(Color.parseColor("#666666")); // 灰色字
                    holder.btnApply.setOnClickListener(v -> {
                        Intent intent = new Intent(context, ResidentApplyAfterSalesActivity.class);
                        intent.putExtra("orderId", order.id);
                        context.startActivity(intent);
                    });
                    break;
                case 1: // 待处理
                    holder.btnApply.setText("售后待处理");
                    holder.btnApply.setTextColor(Color.parseColor("#FF9800")); // 橙色
                    holder.btnApply.setOnClickListener(v -> {
                        Toast.makeText(context, "您的申请正在等待商家处理", Toast.LENGTH_SHORT).show();
                    });
                    break;
                case 2: // 协商中
                    holder.btnApply.setText("售后协商中");
                    holder.btnApply.setTextColor(Color.RED);
                    holder.btnApply.setOnClickListener(v -> {
                        // 可以跳转到聊天页面，这里简单提示
                        Toast.makeText(context, "请查看消息并与商家沟通", Toast.LENGTH_SHORT).show();
                    });
                    break;
                case 3: // 成功
                    holder.btnApply.setText("售后成功");
                    holder.btnApply.setTextColor(Color.parseColor("#4CAF50")); // 绿色
                    holder.btnApply.setOnClickListener(null);
                    break;
                case 4: // 关闭
                    holder.btnApply.setText("售后已关闭");
                    holder.btnApply.setTextColor(Color.GRAY);
                    holder.btnApply.setOnClickListener(null);
                    break;
            }
        }

        holder.btnEvaluate.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "评价功能开发中", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return orderList == null ? 0 : orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvMerchantName, tvStatus, tvProductName, tvSpecInfo;
        TextView tvPayAmount, tvPayMethod, tvCreateTime, tvOrderNo;
        ImageView ivProductImg;
        LinearLayout layoutActionButtons;
        TextView btnEvaluate, btnApply;

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
            layoutActionButtons = itemView.findViewById(R.id.layout_action_buttons);
            btnEvaluate = itemView.findViewById(R.id.btn_evaluate);
            btnApply = itemView.findViewById(R.id.btn_apply);
        }
    }
}
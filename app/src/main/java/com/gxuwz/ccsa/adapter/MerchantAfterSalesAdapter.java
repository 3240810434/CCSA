package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.Order;
import com.gxuwz.ccsa.ui.merchant.MerchantAfterSalesDetailActivity;

import java.util.List;

public class MerchantAfterSalesAdapter extends RecyclerView.Adapter<MerchantAfterSalesAdapter.ViewHolder> {

    private List<Order> orderList;
    private Context context;

    public MerchantAfterSalesAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_merchant_after_sales, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderId.setText("订单号：" + order.orderNo);
        holder.tvProductName.setText(order.productName);
        holder.tvResidentInfo.setText("买家: " + order.residentName + " " + order.residentPhone);

        if (order.productImageUrl != null) {
            Glide.with(context).load(order.productImageUrl).into(holder.ivProduct);
        }

        // 状态文字
        String statusText = "未知";
        int color = Color.GRAY;
        switch (order.afterSalesStatus) {
            case 1: statusText = "待处理"; color = Color.parseColor("#FF9800"); break;
            case 2: statusText = "协商中"; color = Color.RED; break;
            case 3: statusText = "已退款"; color = Color.GREEN; break;
            case 4: statusText = "已关闭"; color = Color.GRAY; break;
        }
        holder.tvStatus.setText(statusText);
        holder.tvStatus.setTextColor(color);

        // 点击按钮进入详情
        holder.btnHandle.setOnClickListener(v -> {
            Intent intent = new Intent(context, MerchantAfterSalesDetailActivity.class);
            intent.putExtra("orderId", order.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList == null ? 0 : orderList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvProductName, tvResidentInfo;
        ImageView ivProduct;
        Button btnHandle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvStatus = itemView.findViewById(R.id.tv_as_status);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvResidentInfo = itemView.findViewById(R.id.tv_resident_info);
            ivProduct = itemView.findViewById(R.id.iv_product);
            btnHandle = itemView.findViewById(R.id.btn_handle);
        }
    }
}
package com.gxuwz.ccsa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.Order;
import java.util.List;

public class MerchantPendingOrderAdapter extends RecyclerView.Adapter<MerchantPendingOrderAdapter.ViewHolder> {
    private List<Order> list;
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onAccept(Order order);
    }

    public void setOnOrderActionListener(OnOrderActionListener listener) {
        this.listener = listener;
    }

    public MerchantPendingOrderAdapter(List<Order> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_merchant_pending_order, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = list.get(position);

        holder.tvOrderNo.setText("订单编号: " + order.orderNo);
        holder.tvTime.setText("时间: " + order.createTime);
        holder.tvAddress.setText("地址: " + order.address + "\n" + order.residentName + " " + order.residentPhone);

        // 商品清单内容
        String detail = "";
        if ("实物".equals(order.productType)) {
            detail = "商品: " + order.productName + "\n规格: " + order.selectedSpec;
        } else {
            detail = "服务: " + order.productName + "\n数量: " + order.serviceCount + "次";
        }
        holder.tvDetails.setText(detail);
        holder.tvAmount.setText("¥" + order.payAmount);

        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) listener.onAccept(order);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNo, tvTime, tvAddress, tvDetails, tvAmount;
        Button btnAccept;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNo = itemView.findViewById(R.id.tv_order_no);
            tvTime = itemView.findViewById(R.id.tv_create_time);
            tvAddress = itemView.findViewById(R.id.tv_address_info);
            tvDetails = itemView.findViewById(R.id.tv_product_details);
            tvAmount = itemView.findViewById(R.id.tv_pay_amount);
            btnAccept = itemView.findViewById(R.id.btn_accept_order);
        }
    }
}
package com.gxuwz.ccsa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.Order;
import java.util.List;

public class ResidentOrderAdapter extends RecyclerView.Adapter<ResidentOrderAdapter.ViewHolder> {
    private List<Order> list;

    public ResidentOrderAdapter(List<Order> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_resident_order, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = list.get(position);
        holder.tvMerchant.setText(order.merchantName == null ? "未知商家" : order.merchantName);
        holder.tvStatus.setText(order.status);
        holder.tvProduct.setText(order.productName);
        holder.tvAmount.setText("¥" + order.payAmount);
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMerchant, tvStatus, tvProduct, tvAmount;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMerchant = itemView.findViewById(R.id.tv_merchant_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvProduct = itemView.findViewById(R.id.tv_product_info);
            tvAmount = itemView.findViewById(R.id.tv_amount);
        }
    }
}

package com.gxuwz.ccsa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
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

        // 1. 头部基础信息
        holder.tvOrderNo.setText("订单号: " + order.orderNo);
        holder.tvStatus.setText(order.status);
        holder.tvCreateTime.setText("下单: " + order.createTime);

        // 2. 图片
        Glide.with(holder.itemView.getContext())
                .load(order.productImageUrl)
                .placeholder(R.drawable.ic_launcher_background) // 请替换为默认图
                .into(holder.ivProductImg);

        holder.tvProductName.setText(order.productName);
        holder.tvTags.setText("标签: " + (order.tags == null || order.tags.isEmpty() ? "无" : order.tags));

        // 3. 区分实物与服务
        boolean isService = "服务".equals(order.productType) || "SERVICE".equalsIgnoreCase(order.productType);

        if (isService) {
            // 显示服务布局，隐藏实物布局
            holder.llServiceInfo.setVisibility(View.VISIBLE);
            holder.llPhysicalInfo.setVisibility(View.GONE);

            // 服务详情
            holder.tvServicePriceUnit.setText("价格: ¥" + order.unitPrice + "/" + order.productUnit + " × " + order.serviceCount);
            holder.tvServiceType.setText("服务类型: 上门服务"); // 或使用 order.productType

        } else {
            // 显示实物布局，隐藏服务布局
            holder.llPhysicalInfo.setVisibility(View.VISIBLE);
            holder.llServiceInfo.setVisibility(View.GONE);

            // 实物详情
            holder.tvPhysicalSpec.setText("规格: " + order.selectedSpec);
            holder.tvPhysicalDelivery.setText("方式: " + (order.deliveryMethod == null ? "商家配送" : order.deliveryMethod));
        }

        // 4. 居民信息
        holder.tvResidentInfo.setText(order.residentName + "  " + order.residentPhone);
        holder.tvAddress.setText("地址: " + order.address);

        // 5. 底部支付信息
        holder.tvPayAmount.setText("¥" + order.payAmount);
        holder.tvPayMethod.setText(order.paymentMethod);

        // 6. 接单按钮事件
        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) listener.onAccept(order);
        });
    }

    @Override
    public int getItemCount() { return list == null ? 0 : list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNo, tvStatus, tvCreateTime;
        ImageView ivProductImg;
        TextView tvProductName, tvTags;

        // 实物容器
        LinearLayout llPhysicalInfo;
        TextView tvPhysicalSpec, tvPhysicalDelivery;

        // 服务容器
        LinearLayout llServiceInfo;
        TextView tvServicePriceUnit, tvServiceType;

        // 居民与支付
        TextView tvResidentInfo, tvAddress;
        TextView tvPayAmount, tvPayMethod;
        Button btnAccept;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNo = itemView.findViewById(R.id.tv_order_no);
            tvStatus = itemView.findViewById(R.id.tv_order_status);
            tvCreateTime = itemView.findViewById(R.id.tv_create_time);
            ivProductImg = itemView.findViewById(R.id.iv_product_img);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvTags = itemView.findViewById(R.id.tv_tags);

            llPhysicalInfo = itemView.findViewById(R.id.ll_physical_info);
            tvPhysicalSpec = itemView.findViewById(R.id.tv_physical_spec);
            tvPhysicalDelivery = itemView.findViewById(R.id.tv_physical_delivery);

            llServiceInfo = itemView.findViewById(R.id.ll_service_info);
            tvServicePriceUnit = itemView.findViewById(R.id.tv_service_price_unit);
            tvServiceType = itemView.findViewById(R.id.tv_service_type);

            tvResidentInfo = itemView.findViewById(R.id.tv_resident_info);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvPayAmount = itemView.findViewById(R.id.tv_pay_amount);
            tvPayMethod = itemView.findViewById(R.id.tv_pay_method);
            btnAccept = itemView.findViewById(R.id.btn_accept_order);
        }
    }
}
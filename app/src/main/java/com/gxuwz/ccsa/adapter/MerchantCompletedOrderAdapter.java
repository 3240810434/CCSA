package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import androidx.annotation.NonNull;
import com.gxuwz.ccsa.model.Order;
import java.util.List;

public class MerchantCompletedOrderAdapter extends MerchantProcessingOrderAdapter {

    public MerchantCompletedOrderAdapter(Context context, List<Order> list) {
        super(context, list);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position); // 先调用父类方法填充基本数据

        // 这里现在可以正常访问 list 了，因为父类中它是 protected 的
        Order order = list.get(position);

        // 覆盖按钮样式和逻辑
        holder.btnAccept.setText("已完成");
        holder.btnAccept.setEnabled(false); // 不可点击
        holder.btnAccept.setBackgroundColor(Color.parseColor("#CCCCCC")); // 灰色背景
        holder.btnAccept.setTextColor(Color.WHITE);

        // 移除点击事件
        holder.btnAccept.setOnClickListener(null);
    }
}
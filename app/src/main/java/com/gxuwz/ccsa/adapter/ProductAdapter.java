package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.Product;
import com.gxuwz.ccsa.ui.resident.ResidentProductDetailActivity;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private List<Product> productList;
    private Context context;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 使用修改后的 item_product_card 布局
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        // --- 1. 处理名称显示 (只显示前5个字) ---
        String name = product.getName();
        if (name != null && name.length() > 5) {
            name = name.substring(0, 5) + "...";
        }
        holder.tvName.setText(name);

        // --- 2. 处理价格显示 ---
        String priceStr = "暂无报价";
        if ("实物".equals(product.getType())) {
            // 实物商品：价格
            if (product.getPrice() != null) {
                // 如果是区间价格，可能包含逗号，这里简单处理
                priceStr = "¥" + product.getPrice();
            }
        } else {
            // 服务商品：基础价格 + 单位 (例如 50元/次)
            String price = product.getPrice() != null ? product.getPrice() : "0";
            String unit = product.getUnit() != null ? product.getUnit() : "次";
            priceStr = price + "元/" + unit;
        }
        holder.tvPrice.setText(priceStr);


        // --- 3. 设置封面图 ---
        String imageUrl = product.getFirstImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.shopping)
                    .into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(R.drawable.shopping);
        }

        // --- 4. 点击跳转 ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ResidentProductDetailActivity.class);
            intent.putExtra("product", product);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList == null ? 0 : productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvName, tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_product_cover);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
        }
    }
}
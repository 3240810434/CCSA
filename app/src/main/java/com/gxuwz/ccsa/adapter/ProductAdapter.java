package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
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
        // 加载修改后的 item_product_card 布局
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        // --- 1. 设置名称 ---
        holder.tvName.setText(product.getName());

        // --- 2. 设置价格显示逻辑 ---
        String priceStr;
        String type = product.getType(); // 获取类型
        String price = product.getPrice() != null ? product.getPrice() : "0";

        // 判断是否为实物商品 (防止 type 为 null 导致崩溃)
        // 注意：数据库中存储的类型字符串必须准确，例如 "实物" 或 "PHYSICAL"
        if (type != null && (type.trim().equals("实物") || type.trim().equalsIgnoreCase("PHYSICAL"))) {
            // 【实物商品】：只显示价格，例如 "¥ 100"
            priceStr = "¥ " + price;
        } else {
            // 【服务商品】：显示价格 + 单位，例如 "50元/次"
            // 如果单位为空，默认为 "次"
            String unit = !TextUtils.isEmpty(product.getUnit()) ? product.getUnit() : "次";
            // 确保显示格式整洁
            priceStr = price + "元/" + unit;
        }

        holder.tvPrice.setText(priceStr);

        // --- 3. 设置封面图 ---
        String imageUrl = product.getFirstImage();
        if (!TextUtils.isEmpty(imageUrl)) {
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
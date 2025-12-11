package com.gxuwz.ccsa.adapter;

import android.net.Uri;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private List<Product> productList;

    public void setProductList(List<Product> productList) {
        this.productList = productList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvName.setText(product.name);

        // --- 修复价格显示逻辑 ---
        // 尝试解析 JSON 价格表
        boolean isJsonPrice = false;
        try {
            if (product.priceTableJson != null) {
                JSONArray ja = new JSONArray(product.priceTableJson);
                if (ja.length() > 0) {
                    JSONObject jo = ja.getJSONObject(0);
                    holder.tvPrice.setText("¥ " + jo.optString("price"));
                    isJsonPrice = true;
                }
            }
        } catch (Exception e) {
            // ignore
        }

        // 如果不是JSON或者解析失败，尝试使用旧字段
        if (!isJsonPrice) {
            holder.tvPrice.setText(product.price != null ? "¥ " + product.price : "¥ --");
        }

        // --- 修复图片显示逻辑 ---
        String imageUrl = product.getFirstImage(); // 使用我们在 Product 实体中新加的方法
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // 使用 Glide 加载 (推荐)
            Glide.with(holder.itemView.getContext()).load(imageUrl).into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(R.drawable.shopping);
        }
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
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
        }
    }
}
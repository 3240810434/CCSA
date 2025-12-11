package com.gxuwz.ccsa.adapter;

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

    // --- 新增：上下文对象，用于Glide加载图片等（可选，目前代码中使用了 holder.itemView.getContext() 也可以） ---
    // private Context context;

    private List<Product> productList;

    // --- 构造函数 ---
    // 如果你在 Activity 中是这样调用的：new ProductAdapter(this, productList);
    // 请保留或添加这个构造函数。如果只用无参构造，可以忽略参数。
    public ProductAdapter(android.content.Context context, List<Product> productList) {
        // this.context = context;
        this.productList = productList;
    }

    // 无参构造函数（为了兼容某些旧代码）
    public ProductAdapter() {
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载布局 item_product_card
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        // 设置商品名称
        if (holder.tvName != null) {
            holder.tvName.setText(product.name);
        }

        // --- 修复价格显示逻辑 ---
        boolean isJsonPrice = false;
        try {
            if (product.priceTableJson != null) {
                JSONArray ja = new JSONArray(product.priceTableJson);
                if (ja.length() > 0) {
                    JSONObject jo = ja.getJSONObject(0);
                    if (holder.tvPrice != null) {
                        holder.tvPrice.setText("¥ " + jo.optString("price"));
                    }
                    isJsonPrice = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 如果不是JSON或者解析失败，尝试使用旧字段
        if (!isJsonPrice && holder.tvPrice != null) {
            holder.tvPrice.setText(product.price != null ? "¥ " + product.price : "¥ --");
        }

        // --- 修复图片显示逻辑 ---
        String imageUrl = product.getFirstImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
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
            // --- 修正点：这里的 ID 必须与 item_product_card.xml 中的 ID 一致 ---
            ivCover = itemView.findViewById(R.id.iv_product_cover); // 原代码是 iv_cover (错误)
            tvName = itemView.findViewById(R.id.tv_product_name);   // 原代码是 tv_name (错误)
            tvPrice = itemView.findViewById(R.id.tv_product_price); // 原代码是 tv_price (错误)
        }
    }
}
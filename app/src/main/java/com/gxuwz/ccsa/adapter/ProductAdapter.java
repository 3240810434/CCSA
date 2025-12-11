package com.gxuwz.ccsa.adapter;

import android.content.Context;
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
    private Context context;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    public ProductAdapter() {
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 使用修改后的商家商品卡片布局 item_product_card_merchant
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_card_merchant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        // 设置名称
        if (holder.tvName != null) {
            holder.tvName.setText(product.name);
        }

        // --- 设置价格和单位 (格式：100元/次) ---
        boolean isJsonPrice = false;
        try {
            if (product.priceTableJson != null && !product.priceTableJson.isEmpty()) {
                JSONArray ja = new JSONArray(product.priceTableJson);
                if (ja.length() > 0) {
                    JSONObject jo = ja.getJSONObject(0);
                    String price = jo.optString("price", "0");
                    String unit = jo.optString("desc", ""); // 假设 desc 字段存的是单位（如"次"、"小时"）

                    if (!unit.isEmpty()) {
                        holder.tvPrice.setText(price + "元/" + unit);
                    } else {
                        holder.tvPrice.setText(price + "元");
                    }
                    isJsonPrice = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 如果解析失败，使用旧字段
        if (!isJsonPrice && holder.tvPrice != null) {
            holder.tvPrice.setText(product.price != null ? product.price + "元" : "暂无报价");
        }

        // --- 设置封面图 (默认第一张) ---
        String imageUrl = product.getFirstImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.shopping) // 加载占位图
                    .into(holder.ivCover);
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
            // 绑定 item_product_card_merchant.xml 中的控件 ID
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
        }
    }
}
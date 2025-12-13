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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载修改后的 item_product_card 布局
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        // --- 1. 设置名称 ---
        holder.tvName.setText(product.getName());

        // --- 2. 封面图设置 (使用 Product 类中定义的 getFirstImage 方法或直接分割字符串) ---
        String imageUrl = product.getFirstImage();
        // 如果 Model 中没有 getFirstImage，也可以用下面的逻辑：
        // if (product.imagePaths != null && !product.imagePaths.isEmpty()) {
        //     imageUrl = product.imagePaths.split(",")[0];
        // }

        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.shopping)
                    .into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(R.drawable.shopping);
        }

        // --- 3. 价格显示逻辑 (严格区分实物和服务) ---
        // 获取商品类型，兼容数据库中可能存在的 "SERVICE" 或 "服务" 两种写法
        String type = product.getType();
        boolean isService = "SERVICE".equalsIgnoreCase(type) || "服务".equals(type);

        if (isService) {
            // 【服务商品逻辑】：解析 priceTableJson 获取第一个规格的价格和单位
            try {
                if (product.priceTableJson != null && !product.priceTableJson.isEmpty()) {
                    JSONArray jsonArray = new JSONArray(product.priceTableJson);
                    if (jsonArray.length() > 0) {
                        JSONObject firstRow = jsonArray.getJSONObject(0);
                        String price = firstRow.optString("price");
                        String unit = firstRow.optString("unit");
                        // 格式：50元/次
                        holder.tvPrice.setText(price + "元/" + unit);
                    } else {
                        // JSON 为空数组时的兜底
                        holder.tvPrice.setText("¥" + product.getPrice());
                    }
                } else {
                    // 没有 JSON 数据时的兜底
                    holder.tvPrice.setText("¥" + product.getPrice());
                }
            } catch (Exception e) {
                // 解析异常时的兜底
                e.printStackTrace();
                holder.tvPrice.setText("¥" + product.getPrice());
            }
        } else {
            // 【实物商品逻辑】：直接显示单价
            // 格式：¥ 100
            String priceStr = product.getPrice();
            if (TextUtils.isEmpty(priceStr)) {
                priceStr = "0.00";
            }
            holder.tvPrice.setText("¥ " + priceStr);
        }

        // --- 4. 点击跳转 ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ResidentProductDetailActivity.class);
            // 传递整个对象
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
            // ID 必须与 item_product_card.xml 中的一致
            ivCover = itemView.findViewById(R.id.iv_product_cover);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
        }
    }
}
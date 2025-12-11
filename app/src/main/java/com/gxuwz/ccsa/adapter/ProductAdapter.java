package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.Product;

import java.util.List;

// 关键点：必须继承 RecyclerView.Adapter
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private Context context;
    private List<Product> productList;

    // 关键点：构造函数必须存在且公开
    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载布局 item_product_card
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        // 设置名称和价格
        if (product.name != null) {
            holder.tvName.setText(product.name);
        }
        if (product.price != null) {
            holder.tvPrice.setText("¥ " + product.price);
        }

        // 设置封面图片
        if (product.coverImage != null && !product.coverImage.isEmpty()) {
            try {
                holder.ivCover.setImageURI(Uri.parse(product.coverImage));
            } catch (Exception e) {
                holder.ivCover.setImageResource(R.drawable.ic_add_photo);
            }
        } else {
            holder.ivCover.setImageResource(R.drawable.ic_add_photo);
        }
    }

    @Override
    public int getItemCount() {
        return productList == null ? 0 : productList.size();
    }

    // ViewHolder 内部类
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvName;
        TextView tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 绑定视图 ID
            ivCover = itemView.findViewById(R.id.iv_product_cover);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
        }
    }
}
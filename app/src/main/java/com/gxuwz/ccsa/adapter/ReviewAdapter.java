package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.ProductReview;
import com.gxuwz.ccsa.util.DateUtils; // 假设有日期工具类，否则用简单format
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private Context context;
    private List<ProductReview> list;

    public ReviewAdapter(Context context, List<ProductReview> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductReview review = list.get(position);
        holder.tvName.setText(review.userName);
        holder.tvContent.setText(review.content);
        // 分数转星星 (10分=5星)
        holder.ratingBar.setRating(review.score / 2.0f);
        holder.tvTime.setText(DateUtils.formatDateTime(review.createTime)); // 需确保DateUtils可用，或自行格式化

        Glide.with(context)
                .load(review.userAvatar)
                .placeholder(R.drawable.ic_avatar)
                .into(holder.ivAvatar);

        // TODO: 如果需要显示评价图片，可以在这里处理 holder.recyclerImages
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvContent, tvTime;
        RatingBar ratingBar;
        RecyclerView recyclerImages;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.img_avatar);
            tvName = itemView.findViewById(R.id.tv_username);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
            ratingBar = itemView.findViewById(R.id.item_rating);
            recyclerImages = itemView.findViewById(R.id.item_recycler_images);
        }
    }
}
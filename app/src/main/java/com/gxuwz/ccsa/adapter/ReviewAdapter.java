package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.ProductReview;
import com.gxuwz.ccsa.util.DateUtils;

import java.util.ArrayList;
import java.util.Arrays;
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

        // 格式化时间
        holder.tvTime.setText(DateUtils.formatDateTime(review.createTime));

        // 加载用户头像
        Glide.with(context)
                .load(review.userAvatar)
                .placeholder(R.drawable.ic_avatar)
                .into(holder.ivAvatar);

        // --- 处理评价图片显示逻辑 ---
        if (!TextUtils.isEmpty(review.imagePaths)) {
            holder.recyclerImages.setVisibility(View.VISIBLE);

            // 将逗号分隔的字符串转为List
            String[] paths = review.imagePaths.split(",");
            List<String> imageList = new ArrayList<>(Arrays.asList(paths));

            // 优化要求：当带有多张图片的评论，每行只显示3张
            GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 3);
            holder.recyclerImages.setLayoutManager(gridLayoutManager);

            // 设置图片适配器
            ReviewImageAdapter imageAdapter = new ReviewImageAdapter(context, imageList);
            holder.recyclerImages.setAdapter(imageAdapter);

        } else {
            // 没有图片时隐藏RecyclerView
            holder.recyclerImages.setVisibility(View.GONE);
        }
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

    // --- 内部类：用于显示评价中的图片列表 ---
    class ReviewImageAdapter extends RecyclerView.Adapter<ReviewImageAdapter.ImageViewHolder> {
        private Context mContext;
        private List<String> mPaths;

        public ReviewImageAdapter(Context context, List<String> paths) {
            this.mContext = context;
            this.mPaths = paths;
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_image_preview_small, parent, false);

            // 优化布局：修改宽度为 MatchParent，并移除 marginEnd，适配 Grid 3列显示
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            if (layoutParams != null) {
                layoutParams.setMarginEnd(0);
                // 增加一点底部间距
                layoutParams.setMargins(0, 0, 0, 10);
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                // 设置固定高度，防止图片高度不一导致乱对其，例如100dp
                layoutParams.height = 300; // 这里的单位是px，建议在dimens中定义或动态计算，示例暂定固定值或保持 xml 定义
                // 如果 xml 中 height 是 wrap_content，建议设为固定高度或正方形

                view.setLayoutParams(layoutParams);
            }

            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            String path = mPaths.get(position);
            Glide.with(mContext)
                    .load(path)
                    .centerCrop() // 裁剪以填满方格
                    .placeholder(R.drawable.ic_add_photo)
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return mPaths == null ? 0 : mPaths.size();
        }

        class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ImageView btnDelete;

            public ImageViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.iv_image);
                // 评论列表中不需要显示删除按钮
                btnDelete = itemView.findViewById(R.id.btn_delete);
                if (btnDelete != null) {
                    btnDelete.setVisibility(View.GONE);
                }
            }
        }
    }
}
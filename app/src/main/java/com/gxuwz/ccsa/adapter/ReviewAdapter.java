package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.DisplayMetrics;
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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.ProductReview;
import com.gxuwz.ccsa.ui.resident.ImagePreviewActivity; // 引入预览页面
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
        // 分数转星星 (假设后台数据是10分制，如果是5分制请直接用review.score)
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

            // 网格布局，3列
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
            // 注意：这里请确保 item_image_preview_small 或 item_image_grid 存在
            // 如果你的文件名是 item_image_grid.xml，请改为 R.layout.item_image_grid
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_image_grid, parent, false);

            // 动态计算每个格子的宽度，使其成为正方形
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            if (layoutParams != null) {
                // 1. 获取屏幕宽度
                DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
                int screenWidth = displayMetrics.widthPixels;

                // 2. 减去父布局的 padding (根据 item_product_review.xml，左右各 16dp，共 32dp)
                //    再加上一些估算的 Grid 间距冗余，这里取 40dp 比较稳妥
                int totalPadding = (int) (40 * displayMetrics.density);

                // 3. 计算单个 Item 大小 (屏幕宽 - 间距) / 3列
                int itemSize = (screenWidth - totalPadding) / 3;

                layoutParams.width = itemSize;
                layoutParams.height = itemSize; // 高度等于宽度，设为正方形

                // 设置 Grid 间距
                layoutParams.setMargins(0, 0, (int)(4 * displayMetrics.density), (int)(8 * displayMetrics.density));

                view.setLayoutParams(layoutParams);
            }

            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            String path = mPaths.get(position);

            // 解决问题1：使用 fitCenter 完整显示图片，而不是 centerCrop
            Glide.with(mContext)
                    .load(path)
                    .fitCenter() // 重点：完整显示，不裁剪
                    // .apply(RequestOptions.bitmapTransform(new RoundedCorners(10))) // 如果需要圆角可以取消注释
                    .placeholder(R.drawable.ic_add_photo)
                    .into(holder.imageView);

            // 解决问题2：添加点击事件，跳转到预览大图页面
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, ImagePreviewActivity.class);
                // 传递当前图片列表
                intent.putStringArrayListExtra("images", (ArrayList<String>) mPaths);
                // 传递当前点击的位置
                intent.putExtra("position", position);
                mContext.startActivity(intent);
            });
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
                // 确保你的 XML id 匹配，此处根据你提供的 xml 使用 iv_image
                imageView = itemView.findViewById(R.id.iv_image);
                btnDelete = itemView.findViewById(R.id.iv_delete); // 或者是 iv_delete

                // 浏览模式下隐藏删除按钮
                if (btnDelete != null) {
                    btnDelete.setVisibility(View.GONE);
                }
            }
        }
    }
}
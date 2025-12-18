package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import java.util.ArrayList;
import java.util.List;

public class ImageGridAdapter extends RecyclerView.Adapter<ImageGridAdapter.ViewHolder> {
    private Context context;
    private List<String> imagePaths = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onAddClick();
        void onDeleteClick(int position);
    }

    public ImageGridAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        // 初始添加一个"add"占位符，代表添加按钮
        imagePaths.add("add");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_image_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String path = imagePaths.get(position);

        // 判断是否为"添加"按钮
        if ("add".equals(path)) {
            // 1. 设置为您指定的 photo_album 图标
            holder.ivImage.setImageResource(R.drawable.photo_album);

            // 2. 隐藏删除按钮 (去掉右上角的 x)
            holder.ivDelete.setVisibility(View.GONE);

            // 点击事件：触发添加图片操作
            holder.itemView.setOnClickListener(v -> listener.onAddClick());
        } else {
            // 加载用户选择的实际图片
            Glide.with(context)
                    .load(path)
                    .into(holder.ivImage);

            // 显示删除按钮
            holder.ivDelete.setVisibility(View.VISIBLE);

            // 点击删除按钮：触发删除操作
            holder.ivDelete.setOnClickListener(v ->
                    listener.onDeleteClick(position));

            // 图片本身不响应点击（或者您可以添加预览图片的逻辑）
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public void addImage(String path) {
        // 在"add"占位符之前插入新图片
        imagePaths.add(imagePaths.size() - 1, path);
        notifyDataSetChanged();
    }

    public void removeImage(int position) {
        imagePaths.remove(position);
        notifyDataSetChanged();
    }

    // 获取实际上传的图片路径列表（不包含末尾的"add"）
    public List<String> getImagePaths() {
        List<String> actualImages = new ArrayList<>();
        for (int i = 0; i < imagePaths.size() - 1; i++) {
            actualImages.add(imagePaths.get(i));
        }
        return actualImages;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        ImageView ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }
}
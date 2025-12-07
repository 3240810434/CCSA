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
        // 初始添加一个"添加"按钮占位符
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
        if ("add".equals(path)) {
            // 显示添加图标
            holder.ivImage.setImageResource(R.drawable.ic_add_photo);
            holder.ivDelete.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(v -> listener.onAddClick());
        } else {
            // 使用Glide加载图片
            Glide.with(context)
                    .load(path)
                    .into(holder.ivImage);
            holder.ivDelete.setVisibility(View.VISIBLE);
            holder.ivDelete.setOnClickListener(v ->
                    listener.onDeleteClick(position));
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public void addImage(String path) {
        // 在添加按钮前插入新图片
        imagePaths.add(imagePaths.size() - 1, path);
        notifyDataSetChanged();
    }

    public void removeImage(int position) {
        imagePaths.remove(position);
        notifyDataSetChanged();
    }

    // 新增：获取实际图片路径列表（排除最后一个"add"占位符）
    public List<String> getImagePaths() {
        List<String> actualImages = new ArrayList<>();
        // 遍历所有路径，排除最后一个"add"占位符
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

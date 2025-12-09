package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.HelpPostMedia;
import com.gxuwz.ccsa.ui.resident.ImagePreviewActivity;
import com.gxuwz.ccsa.ui.resident.VideoFullScreenActivity;
import java.util.ArrayList;
import java.util.List;

public class HelpPostMediaAdapter extends RecyclerView.Adapter<HelpPostMediaAdapter.ViewHolder> {
    private Context context;
    private List<HelpPostMedia> list;

    public HelpPostMediaAdapter(Context context, List<HelpPostMedia> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 复用 item_media_grid 布局，它只包含 ImageView 和 PlayIcon，是通用的
        View view = LayoutInflater.from(context).inflate(R.layout.item_media_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HelpPostMedia media = list.get(position);

        // 1. 加载缩略图
        Glide.with(context)
                .load(media.url)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(android.R.color.darker_gray)
                .into(holder.ivImage);

        // 2. 视频图标显示逻辑
        if (holder.ivPlay != null && holder.vMask != null) {
            if (media.type == 2) { // 假设 2 是视频 (或根据你的逻辑可能是 1)
                holder.ivPlay.setVisibility(View.VISIBLE);
                holder.vMask.setVisibility(View.VISIBLE);
            } else {
                holder.ivPlay.setVisibility(View.GONE);
                holder.vMask.setVisibility(View.GONE);
            }
        }

        // 3. 在展示页面通常不需要 "选中/序号" 逻辑，隐藏掉
        if (holder.vRing != null) holder.vRing.setVisibility(View.GONE);
        if (holder.tvIndex != null) holder.tvIndex.setVisibility(View.GONE);

        // 4. 点击查看大图或播放视频
        holder.itemView.setOnClickListener(v -> {
            if (media.type == 2) { // 视频
                Intent intent = new Intent(context, VideoFullScreenActivity.class);
                intent.putExtra("videoPath", media.url);
                context.startActivity(intent);
            } else { // 图片
                // 收集所有图片路径用于预览
                ArrayList<String> imageUrls = new ArrayList<>();
                int currentImgPos = 0;
                int idx = 0;
                for (HelpPostMedia m : list) {
                    if (m.type != 2) {
                        imageUrls.add(m.url);
                        if (m == media) currentImgPos = idx;
                        idx++;
                    }
                }
                Intent intent = new Intent(context, ImagePreviewActivity.class);
                intent.putStringArrayListExtra("images", imageUrls);
                intent.putExtra("position", currentImgPos);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        ImageView ivPlay;
        View vMask;
        View vRing;     // 可能复用布局里有这些View，需要定义避免空指针，但在展示页隐藏
        View tvIndex;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            ivPlay = itemView.findViewById(R.id.iv_play_icon);
            vMask = itemView.findViewById(R.id.v_mask);
            vRing = itemView.findViewById(R.id.view_ring);
            tvIndex = itemView.findViewById(R.id.tv_index);
        }
    }
}
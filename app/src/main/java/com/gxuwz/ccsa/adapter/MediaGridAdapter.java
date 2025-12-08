package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.PostMedia;
import java.util.ArrayList;
import java.util.List;

public class MediaGridAdapter extends RecyclerView.Adapter<MediaGridAdapter.ViewHolder> {
    private Context context;
    private List<PostMedia> list;
    private List<PostMedia> selectedList = new ArrayList<>();

    public MediaGridAdapter(Context context, List<PostMedia> list) {
        this.context = context;
        this.list = list;
    }

    public List<PostMedia> getSelectedItems() {
        return selectedList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostMedia media = list.get(position);

        // 1. 加载图片/视频缩略图
        Glide.with(context)
                .load(media.url)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(android.R.color.darker_gray)
                .into(holder.ivImage);

        // 2. 根据类型显示/隐藏视频图标和遮罩
        // 确保 vMask 不为空再调用
        if (holder.vMask != null && holder.ivPlay != null) {
            if (media.type == 2) { // 2代表视频
                holder.ivPlay.setVisibility(View.VISIBLE);
                holder.vMask.setVisibility(View.VISIBLE);
            } else {
                holder.ivPlay.setVisibility(View.GONE);
                holder.vMask.setVisibility(View.GONE);
            }
        }

        // 3. 处理选中状态
        int index = selectedList.indexOf(media);
        if (index != -1) {
            holder.tvIndex.setVisibility(View.VISIBLE);
            holder.tvIndex.setText(String.valueOf(index + 1));
            holder.vRing.setBackgroundResource(R.drawable.red_circle);
        } else {
            holder.tvIndex.setVisibility(View.GONE);
            holder.vRing.setBackgroundResource(R.drawable.circle_alarm_bg);
        }

        // 4. 点击逻辑
        holder.vRing.setOnClickListener(v -> {
            if (selectedList.contains(media)) {
                selectedList.remove(media);
            } else {
                if (isSelectionValid(media)) {
                    selectedList.add(media);
                }
            }
            notifyDataSetChanged();
        });

        holder.itemView.setOnClickListener(v -> holder.vRing.performClick());
    }

    private boolean isSelectionValid(PostMedia newMedia) {
        if (selectedList.isEmpty()) return true;
        PostMedia firstSelected = selectedList.get(0);

        if (firstSelected.type != newMedia.type) {
            Toast.makeText(context, "不能同时选择照片和视频", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (firstSelected.type == 2) {
            Toast.makeText(context, "最多选择1个视频", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedList.size() >= 10) {
            Toast.makeText(context, "最多选择10张照片", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        ImageView ivPlay; // 视频播放图标
        View vRing;       // 选中圈
        View vMask;       // 视频遮罩层 (关键修复)
        TextView tvIndex; // 选中序号

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            ivPlay = itemView.findViewById(R.id.iv_play_icon);
            vRing = itemView.findViewById(R.id.view_ring);
            vMask = itemView.findViewById(R.id.v_mask); // 确保 ID 与 XML 一致
            tvIndex = itemView.findViewById(R.id.tv_index);
        }
    }
}
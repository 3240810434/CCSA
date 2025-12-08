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

        // 1. 加载图片/视频缩略图 (Glide可以自动处理content:// URI)
        Glide.with(context)
                .load(media.url) // 这里现在是Uri字符串
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(android.R.color.darker_gray) // 加载中占位图
                .into(holder.ivImage);

        // 2. 判断是否是视频，显示图标
        if (media.type == 2) { // 2代表视频
            holder.ivPlay.setVisibility(View.VISIBLE);
            holder.vMask.setVisibility(View.VISIBLE);
        } else {
            holder.ivPlay.setVisibility(View.GONE);
            holder.vMask.setVisibility(View.GONE);
        }

        // 3. 处理选中状态
        int index = selectedList.indexOf(media);
        if (index != -1) {
            holder.tvIndex.setVisibility(View.VISIBLE);
            holder.tvIndex.setText(String.valueOf(index + 1));
            holder.vRing.setBackgroundResource(R.drawable.red_circle); // 选中变红
        } else {
            holder.tvIndex.setVisibility(View.GONE);
            holder.vRing.setBackgroundResource(R.drawable.circle_alarm_bg); // 未选中白圈
        }

        // 4. 点击选择逻辑
        holder.vRing.setOnClickListener(v -> {
            if (selectedList.contains(media)) {
                // 取消选择
                selectedList.remove(media);
            } else {
                // 尝试选择
                if (isSelectionValid(media)) {
                    selectedList.add(media);
                }
            }
            notifyDataSetChanged();
        });

        // 点击整个Item也可以触发选择
        holder.itemView.setOnClickListener(v -> holder.vRing.performClick());
    }

    // 校验选择是否合法
    private boolean isSelectionValid(PostMedia newMedia) {
        if (selectedList.isEmpty()) return true;

        PostMedia firstSelected = selectedList.get(0);

        // 规则1：视频和图片不能混选
        if (firstSelected.type != newMedia.type) {
            Toast.makeText(context, "不能同时选择照片和视频", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 规则2：如果是视频，只能选1个
        if (firstSelected.type == 2) {
            Toast.makeText(context, "最多选择1个视频", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 规则3：如果是图片，最多选10张
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
        ImageView ivImage, ivPlay;
        View vRing, vMask;
        TextView tvIndex;

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
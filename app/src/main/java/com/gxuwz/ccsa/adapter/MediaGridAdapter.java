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
        Glide.with(context).load(media.url).centerCrop().into(holder.ivImage);

        // 检查选中状态
        int index = selectedList.indexOf(media);
        if (index != -1) {
            holder.tvIndex.setVisibility(View.VISIBLE);
            holder.tvIndex.setText(String.valueOf(index + 1));
            holder.vRing.setBackgroundResource(R.drawable.red_circle); // 选中变色
        } else {
            holder.tvIndex.setVisibility(View.GONE);
            holder.vRing.setBackgroundResource(R.drawable.circle_alarm_bg); // 未选中白色圆环
        }

        holder.vRing.setOnClickListener(v -> {
            if (selectedList.contains(media)) {
                selectedList.remove(media);
            } else {
                if (selectedList.size() >= 10) {
                    Toast.makeText(context, "最多选择10张照片", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 视频互斥逻辑省略，此处简化为图片
                selectedList.add(media);
            }
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        View vRing; // 选中圈
        TextView tvIndex; // 选中序号

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            vRing = itemView.findViewById(R.id.view_ring);
            tvIndex = itemView.findViewById(R.id.tv_index);
        }
    }
}
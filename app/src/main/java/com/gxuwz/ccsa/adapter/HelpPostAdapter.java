package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.HelpPost;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.ui.resident.ChatActivity;
import com.gxuwz.ccsa.ui.resident.VideoFullScreenActivity;
import com.gxuwz.ccsa.util.DateUtils;
import java.util.List;

public class HelpPostAdapter extends RecyclerView.Adapter<HelpPostAdapter.ViewHolder> {

    private Context context;
    private List<HelpPost> list;
    private User currentUser;

    public HelpPostAdapter(Context context, List<HelpPost> list, User currentUser) {
        this.context = context;
        this.list = list;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_help_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HelpPost post = list.get(position);

        // 1. 设置基本信息
        holder.tvTitle.setText(post.title);
        holder.tvContent.setText(post.content);
        holder.tvAuthor.setText(post.userName);
        holder.tvTime.setText(DateUtils.formatTime(post.createTime)); // 假设你有这个工具类

        // 头像加载
        Glide.with(context)
                .load(post.userAvatar)
                .placeholder(R.drawable.ic_avatar) // 默认头像
                .circleCrop()
                .into(holder.ivAvatar);

        // 2. 处理媒体展示 (复用 PostAdapter 的逻辑，这里简化展示)
        if (post.type == 0 || post.mediaList == null || post.mediaList.isEmpty()) {
            holder.rvMedia.setVisibility(View.GONE);
        } else {
            holder.rvMedia.setVisibility(View.VISIBLE);
            // 这里建议复用 ImageGridAdapter 或 MediaGridAdapter
            // 简单起见，这里假设你有一个通用的 GridAdapter
            MediaGridAdapter mediaAdapter = new MediaGridAdapter(context, post.mediaList);
            holder.rvMedia.setLayoutManager(new GridLayoutManager(context, 3));
            holder.rvMedia.setAdapter(mediaAdapter);

            // 点击图片/视频的逻辑通常在 MediaGridAdapter 内部处理
        }

        // 3. 核心逻辑：联系按钮
        if (currentUser != null && post.userId == currentUser.getId()) {
            // 如果是自己发布的，隐藏联系按钮
            holder.llContact.setVisibility(View.GONE);
        } else {
            // 如果是别人发布的，显示联系按钮
            holder.llContact.setVisibility(View.VISIBLE);
            holder.llContact.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("targetUserId", post.userId);
                intent.putExtra("currentUser", currentUser);
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvAuthor, tvTime, tvTitle, tvContent;
        RecyclerView rvMedia;
        LinearLayout llContact; // 包含图标和文字的布局

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvAuthor = itemView.findViewById(R.id.tv_author_name);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
            rvMedia = itemView.findViewById(R.id.rv_media);
            llContact = itemView.findViewById(R.id.ll_contact); // 对应 item_help_post.xml 中的联系区域
        }
    }
}
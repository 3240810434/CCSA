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

        // 1. 设置文本内容
        holder.tvTitle.setText(post.title);
        holder.tvContent.setText(post.content);

        // 修复：确保显示正确的用户名，如果为空则显示默认
        holder.tvAuthor.setText(post.userName != null ? post.userName : "未知邻居");
        holder.tvTime.setText(DateUtils.formatTime(post.createTime));

        // 2. 加载头像 (核心修复：使用 lan 作为默认占位图)
        Glide.with(context)
                .load(post.userAvatar) // 如果 post.userAvatar 为 null，Glide 会自动加载 placeholder
                .placeholder(R.drawable.lan) // 加载中/为空时显示 lan.png
                .error(R.drawable.lan)       // 加载失败时显示 lan.png
                .circleCrop()
                .into(holder.ivAvatar);

        // 3. 处理媒体展示 (图片/视频)
        if (post.mediaList == null || post.mediaList.isEmpty()) {
            holder.rvMedia.setVisibility(View.GONE);
        } else {
            holder.rvMedia.setVisibility(View.VISIBLE);
            HelpPostMediaAdapter mediaAdapter = new HelpPostMediaAdapter(context, post.mediaList);
            holder.rvMedia.setLayoutManager(new GridLayoutManager(context, 3));
            holder.rvMedia.setAdapter(mediaAdapter);
        }

        // 4. 联系按钮逻辑
        if (currentUser != null && post.userId == currentUser.getId()) {
            // 自己发布的帖子，隐藏联系按钮
            holder.llContact.setVisibility(View.GONE);
        } else {
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
        LinearLayout llContact;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvAuthor = itemView.findViewById(R.id.tv_author_name);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
            rvMedia = itemView.findViewById(R.id.rv_media);
            llContact = itemView.findViewById(R.id.ll_contact);
        }
    }

    public void setList(List<HelpPost> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }
}
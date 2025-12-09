package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
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

        // 1. 设置用户信息
        holder.tvName.setText(post.userName);
        holder.tvTime.setText(DateUtils.getRelativeTime(post.createTime));
        Glide.with(context)
                .load(post.userAvatar)
                .placeholder(R.drawable.lan) // 默认头像 lan.png
                .error(R.drawable.lan)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(100)))
                .into(holder.ivAvatar);

        // 2. 设置内容
        holder.tvTitle.setText(post.title); // 粗体标题
        holder.tvContent.setText(post.content);

        // 3. 处理媒体显示 (简化逻辑，详细图片/视频显示代码请参考 PostAdapter 复用)
        holder.mediaContainer.removeAllViews();
        // TODO: 这里应直接复制 PostAdapter 中关于 mediaList (GridView/VideoView) 的展示逻辑
        // 为节省篇幅，此处省略具体的 addView 代码，请务必将 PostAdapter 的相关代码复制过来
        // 注意将 post.mediaList 类型改为 List<HelpPostMedia>

        // 4. 联系按钮逻辑
        boolean isMe = (currentUser != null && currentUser.getId() == post.userId);
        if (isMe) {
            // 本人发布的帖子，置灰不可点
            holder.layoutContact.setAlpha(0.5f);
            holder.layoutContact.setOnClickListener(null);
        } else {
            // 他人帖子，可点击
            holder.layoutContact.setAlpha(1.0f);
            holder.layoutContact.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("targetUserId", post.userId);
                intent.putExtra("currentUser", currentUser);
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvTime, tvTitle, tvContent;
        LinearLayout mediaContainer, layoutContact;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
            mediaContainer = itemView.findViewById(R.id.media_container);
            layoutContact = itemView.findViewById(R.id.layout_contact);
        }
    }
}
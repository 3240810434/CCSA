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

        holder.tvTitle.setText(post.title);
        holder.tvContent.setText(post.content);
        holder.tvAuthor.setText(post.userName != null ? post.userName : "未知邻居");
        holder.tvTime.setText(DateUtils.formatTime(post.createTime));

        Glide.with(context)
                .load(post.userAvatar)
                .placeholder(R.drawable.lan)
                .error(R.drawable.lan)
                .circleCrop()
                .into(holder.ivAvatar);

        // 处理媒体展示
        if (post.mediaList == null || post.mediaList.isEmpty()) {
            holder.rvMedia.setVisibility(View.GONE);
        } else {
            holder.rvMedia.setVisibility(View.VISIBLE);
            HelpPostMediaAdapter mediaAdapter = new HelpPostMediaAdapter(context, post.mediaList);

            // 优化：如果是视频 (type=2)，使用 1 列显示大图；如果是图片，使用 3 列网格
            // 假设 mediaList 中的元素类型一致
            int spanCount = 3;
            if (post.mediaList.get(0).type == 2) {
                spanCount = 1;
            }
            holder.rvMedia.setLayoutManager(new GridLayoutManager(context, spanCount));
            holder.rvMedia.setAdapter(mediaAdapter);
        }

        // 联系按钮
        if (currentUser != null && post.userId == currentUser.getId()) {
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
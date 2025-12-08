package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.Post;
import com.gxuwz.ccsa.ui.resident.PostDetailActivity;
import com.gxuwz.ccsa.util.DateUtils;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private Context context;
    private List<Post> list;

    public PostAdapter(Context context, List<Post> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post_card, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = list.get(position);
        holder.tvName.setText(post.userName);
        holder.tvTime.setText(DateUtils.getRelativeTime(post.createTime));
        holder.tvContent.setText(post.content);
        holder.tvCommentCount.setText(String.valueOf(post.commentCount));

        // 加载用户头像
        if (post.userAvatar != null && !post.userAvatar.isEmpty()) {
            Glide.with(context).load(post.userAvatar).into(holder.ivAvatar);
        } else {
            // 设置默认头像
            holder.ivAvatar.setImageResource(R.drawable.ic_avatar);
        }

        // 媒体处理
        holder.ivImage1.setVisibility(View.GONE);
        holder.ivImage2.setVisibility(View.GONE);
        holder.ivImage3.setVisibility(View.GONE);
        holder.videoView.setVisibility(View.GONE);
        holder.ivPlay.setVisibility(View.GONE);

        if (post.mediaList != null && !post.mediaList.isEmpty()) {
            if (post.type == 2) { // 视频
                holder.videoView.setVisibility(View.VISIBLE);
                holder.ivPlay.setVisibility(View.VISIBLE);
                holder.videoView.setVideoPath(post.mediaList.get(0).url);
                holder.ivPlay.setOnClickListener(v -> {
                    holder.videoView.start();
                    holder.ivPlay.setVisibility(View.GONE);
                });
            } else { // 图片
                int size = post.mediaList.size();
                if (size > 0) {
                    holder.ivImage1.setVisibility(View.VISIBLE);
                    Glide.with(context).load(post.mediaList.get(0).url).into(holder.ivImage1);
                }
                if (size > 1) {
                    holder.ivImage2.setVisibility(View.VISIBLE);
                    Glide.with(context).load(post.mediaList.get(1).url).into(holder.ivImage2);
                }
                if (size > 2) {
                    holder.ivImage3.setVisibility(View.VISIBLE);
                    Glide.with(context).load(post.mediaList.get(2).url).into(holder.ivImage3);
                }
            }
        }

        holder.btnComment.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("post", post);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime, tvContent, tvCommentCount;
        ImageView ivAvatar; // 新增头像控件
        ImageView ivImage1, ivImage2, ivImage3, ivPlay;
        VideoView videoView;
        View btnComment;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvCommentCount = itemView.findViewById(R.id.tv_comment_count);

            // 关键点：这里绑定的是 XML 中的 id/iv_avatar
            ivAvatar = itemView.findViewById(R.id.iv_avatar);

            ivImage1 = itemView.findViewById(R.id.iv_img_1);
            ivImage2 = itemView.findViewById(R.id.iv_img_2);
            ivImage3 = itemView.findViewById(R.id.iv_img_3);
            videoView = itemView.findViewById(R.id.video_view);
            ivPlay = itemView.findViewById(R.id.iv_play_icon);
            btnComment = itemView.findViewById(R.id.layout_comment);
        }
    }
}
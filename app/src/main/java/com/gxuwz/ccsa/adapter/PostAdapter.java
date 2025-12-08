package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.Post;
import com.gxuwz.ccsa.model.PostMedia;
import com.gxuwz.ccsa.ui.resident.PostDetailActivity;
import com.gxuwz.ccsa.util.DateUtils;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private Context context;
    private List<Post> list;
    private int screenWidth;

    public PostAdapter(Context context, List<Post> list) {
        this.context = context;
        this.list = list;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
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

        // 1. 基础信息绑定
        holder.tvName.setText(post.userName);
        holder.tvTime.setText(DateUtils.getRelativeTime(post.createTime));
        holder.tvCommentCount.setText(post.commentCount > 0 ? String.valueOf(post.commentCount) : "");

        Glide.with(context)
                .load(post.userAvatar)
                .placeholder(R.drawable.lan)
                .error(R.drawable.lan)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(100))) // 圆形头像
                .into(holder.ivAvatar);

        // 2. 文字处理：根据是否有媒体调整大小
        boolean hasMedia = post.mediaList != null && !post.mediaList.isEmpty();
        holder.tvContent.setText(post.content);
        if (!hasMedia) {
            // 纯文字帖：字体大一点
            holder.tvContent.setTextSize(18);
        } else {
            // 带媒体帖：标准字体
            holder.tvContent.setTextSize(15);
        }

        // 隐藏空文字
        holder.tvContent.setVisibility(post.content == null || post.content.isEmpty() ? View.GONE : View.VISIBLE);

        // 3. 媒体内容处理
        holder.mediaContainer.removeAllViews(); // 清除旧视图

        if (hasMedia) {
            if (post.type == 2) {
                // === 视频处理 ===
                View videoLayout = LayoutInflater.from(context).inflate(R.layout.item_media_grid, holder.mediaContainer, false);
                // 这里为了演示简单直接用了VideoView，实际列表中建议使用ImageView做封面，点击再播放
                VideoView videoView = new VideoView(context);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500); // 固定高度
                videoView.setLayoutParams(params);
                videoView.setVideoPath(post.mediaList.get(0).url);

                ImageView playIcon = new ImageView(context);
                playIcon.setImageResource(android.R.drawable.ic_media_play);
                FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(100, 100);
                iconParams.gravity = android.view.Gravity.CENTER;
                playIcon.setLayoutParams(iconParams);

                holder.mediaContainer.addView(videoView);
                holder.mediaContainer.addView(playIcon);

                // 列表内不建议自动播放，点击进入详情或播放
                View.OnClickListener listener = v -> {
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("post", post);
                    context.startActivity(intent);
                };
                playIcon.setOnClickListener(listener);

            } else {
                // === 图片处理 ===
                int imgCount = post.mediaList.size();

                if (imgCount == 1) {
                    // 2.1 单图：直接显示，自适应大小
                    ImageView imageView = new ImageView(context);
                    // 限制最大高度，避免图片过长
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.height = 600; // 最大高度限制
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(ImageView.ScaleType.FIT_START);
                    imageView.setAdjustViewBounds(true);

                    Glide.with(context)
                            .load(post.mediaList.get(0).url)
                            .transform(new CenterCrop(), new RoundedCorners(16))
                            .into(imageView);

                    holder.mediaContainer.addView(imageView);

                    // 点击放大/查看图片 (这里简化为进入详情页，或者可以跳转专门的图片查看Activity)
                    imageView.setOnClickListener(v -> {
                        // 这里通常跳转到一个全屏查看图片的Activity，根据需求描述，
                        // "点击图片可以放大查看"，建议实现一个 PhotoViewActivity
                        // 暂时为了简单，这里不写新的Activity，而是遵循"点击评论进详情"
                        // 如果需要单纯看图，可以扩展
                    });

                } else {
                    // 2.2 多图 (>=2)：网格显示
                    GridLayout gridLayout = new GridLayout(context);
                    gridLayout.setColumnCount(3);
                    gridLayout.setRowCount((imgCount + 2) / 3);

                    // 计算每个格子的宽度：(屏幕宽 - padding) / 3
                    int padding = 40; // 估算左右边距 dp转px
                    int itemSize = (screenWidth - dp2px(context, padding)) / 3;

                    for (int i = 0; i < imgCount; i++) {
                        String url = post.mediaList.get(i).url;
                        ImageView iv = new ImageView(context);
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.width = itemSize;
                        params.height = itemSize; // 正方形
                        params.setMargins(2, 2, 2, 2); // 间距
                        iv.setLayoutParams(params);
                        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

                        Glide.with(context)
                                .load(url)
                                .into(iv);

                        gridLayout.addView(iv);
                    }
                    holder.mediaContainer.addView(gridLayout);
                }
            }
        }

        // 4. 点击评论进入详情页
        holder.btnComment.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("post", post);
            context.startActivity(intent);
        });

        // 点击整个卡片也可以进详情
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("post", post);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime, tvContent, tvCommentCount;
        ImageView ivAvatar;
        FrameLayout mediaContainer;
        View btnComment;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvCommentCount = itemView.findViewById(R.id.tv_comment_count);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            mediaContainer = itemView.findViewById(R.id.media_container);
            btnComment = itemView.findViewById(R.id.layout_comment);
        }
    }
}
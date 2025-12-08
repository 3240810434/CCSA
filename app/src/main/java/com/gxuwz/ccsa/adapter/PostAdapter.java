package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.gxuwz.ccsa.ui.resident.ImagePreviewActivity;
import com.gxuwz.ccsa.ui.resident.PostDetailActivity;
import com.gxuwz.ccsa.util.DateUtils;
import java.util.ArrayList;
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

        // 1. 基础信息
        holder.tvName.setText(post.userName);
        holder.tvTime.setText(DateUtils.getRelativeTime(post.createTime));
        holder.tvCommentCount.setText(post.commentCount > 0 ? String.valueOf(post.commentCount) : "评论");

        Glide.with(context)
                .load(post.userAvatar)
                .placeholder(R.drawable.lan)
                .error(R.drawable.lan)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(100)))
                .into(holder.ivAvatar);

        // 2. 文字处理
        boolean hasMedia = post.mediaList != null && !post.mediaList.isEmpty();
        holder.tvContent.setText(post.content);
        // 有多媒体时字体稍小，纯文本时字体稍大
        holder.tvContent.setTextSize(hasMedia ? 15 : 17);
        holder.tvContent.setVisibility(post.content == null || post.content.isEmpty() ? View.GONE : View.VISIBLE);

        // 3. 媒体显示逻辑
        holder.mediaContainer.removeAllViews();

        if (hasMedia) {
            if (post.type == 2) {
                // ============ 2. 视频帖子优化 ============
                String videoUrl = post.mediaList.get(0).url;

                // 2.1 宽高设置：宽度占满卡片，高度按 16:9 比例增加
                // 注意：item_post_card 有 margin，所以 screenWidth 需要减去 margin 才是实际可用宽度
                int cardContentWidth = screenWidth - dp2px(context, 44); // 左右margin(10*2) + padding(12*2) = 44dp approx
                int videoHeight = (int) (cardContentWidth * 0.56f); // 16:9 比例 (9/16 ≈ 0.56)

                FrameLayout frameLayout = new FrameLayout(context);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, videoHeight);
                frameLayout.setLayoutParams(layoutParams);

                // VideoView
                VideoView videoView = new VideoView(context);
                FrameLayout.LayoutParams videoParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                videoParams.gravity = Gravity.CENTER;
                videoView.setLayoutParams(videoParams);
                videoView.setVideoPath(videoUrl);

                // 封面图（未播放时显示）
                ImageView coverImage = new ImageView(context);
                coverImage.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                coverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                // 使用 Glide 加载视频第一帧作为封面
                Glide.with(context)
                        .load(videoUrl)
                        .frame(1000000) // 取第1秒
                        .into(coverImage);

                // 播放图标 (居中)
                ImageView playIcon = new ImageView(context);
                playIcon.setImageResource(android.R.drawable.ic_media_play);
                FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(120, 120);
                iconParams.gravity = Gravity.CENTER;
                playIcon.setLayoutParams(iconParams);

                frameLayout.addView(videoView);
                frameLayout.addView(coverImage);
                frameLayout.addView(playIcon);
                holder.mediaContainer.addView(frameLayout);

                // 2.2 点击播放图标直接播放，不跳转
                playIcon.setOnClickListener(v -> {
                    coverImage.setVisibility(View.GONE);
                    playIcon.setVisibility(View.GONE);
                    videoView.start();
                });

                // 视频播放完成监听
                videoView.setOnCompletionListener(mp -> {
                    playIcon.setVisibility(View.VISIBLE);
                    // 也可以选择恢复封面显示
                });

            } else {
                // ============ 图片显示逻辑 (保持不变) ============
                int imgCount = post.mediaList.size();
                ArrayList<String> imgUrls = new ArrayList<>();
                for(int k=0; k<imgCount; k++) imgUrls.add(post.mediaList.get(k).url);

                if (imgCount == 1) {
                    ImageView imageView = new ImageView(context);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 600);
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(ImageView.ScaleType.FIT_START);
                    imageView.setAdjustViewBounds(true);

                    Glide.with(context)
                            .load(post.mediaList.get(0).url)
                            .transform(new CenterCrop(), new RoundedCorners(16))
                            .into(imageView);

                    holder.mediaContainer.addView(imageView);
                    imageView.setOnClickListener(v -> {
                        Intent intent = new Intent(context, ImagePreviewActivity.class);
                        intent.putStringArrayListExtra("images", imgUrls);
                        intent.putExtra("position", 0);
                        context.startActivity(intent);
                    });

                } else {
                    GridLayout gridLayout = new GridLayout(context);
                    gridLayout.setColumnCount(3);
                    gridLayout.setRowCount((imgCount + 2) / 3);

                    int padding = 40;
                    int itemSize = (screenWidth - dp2px(context, padding)) / 3;

                    for (int i = 0; i < imgCount; i++) {
                        String url = post.mediaList.get(i).url;
                        ImageView iv = new ImageView(context);
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.width = itemSize;
                        params.height = itemSize;
                        params.setMargins(4, 4, 4, 4);
                        iv.setLayoutParams(params);
                        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

                        Glide.with(context).load(url).into(iv);
                        final int pos = i;
                        iv.setOnClickListener(v -> {
                            Intent intent = new Intent(context, ImagePreviewActivity.class);
                            intent.putStringArrayListExtra("images", imgUrls);
                            intent.putExtra("position", pos);
                            context.startActivity(intent);
                        });
                        gridLayout.addView(iv);
                    }
                    holder.mediaContainer.addView(gridLayout);
                }
            }
        }

        // ============ 互动按钮逻辑 ============
        holder.ivLike.setImageResource(post.isLiked ? R.drawable.liked : R.drawable.like);
        holder.ivDislike.setImageResource(post.isDisliked ? R.drawable.disliked : R.drawable.dislike);

        holder.layoutLike.setOnClickListener(v -> {
            if (post.isDisliked) {
                post.isDisliked = false;
                holder.ivDislike.setImageResource(R.drawable.dislike);
            }
            post.isLiked = !post.isLiked;
            holder.ivLike.setImageResource(post.isLiked ? R.drawable.liked : R.drawable.like);
        });

        holder.layoutDislike.setOnClickListener(v -> {
            if (post.isLiked) {
                post.isLiked = false;
                holder.ivLike.setImageResource(R.drawable.like);
            }
            post.isDisliked = !post.isDisliked;
            holder.ivDislike.setImageResource(post.isDisliked ? R.drawable.disliked : R.drawable.dislike);
        });

        holder.layoutComment.setOnClickListener(v -> {
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
        View layoutLike, layoutDislike, layoutComment;
        ImageView ivLike, ivDislike;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvCommentCount = itemView.findViewById(R.id.tv_comment_count);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            mediaContainer = itemView.findViewById(R.id.media_container);
            layoutLike = itemView.findViewById(R.id.layout_like);
            layoutDislike = itemView.findViewById(R.id.layout_dislike);
            layoutComment = itemView.findViewById(R.id.layout_comment);
            ivLike = itemView.findViewById(R.id.iv_like);
            ivDislike = itemView.findViewById(R.id.iv_dislike);
        }
    }
}
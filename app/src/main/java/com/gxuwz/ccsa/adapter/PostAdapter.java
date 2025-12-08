package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.ui.resident.ImagePreviewActivity;
import com.gxuwz.ccsa.ui.resident.PostDetailActivity;
import com.gxuwz.ccsa.util.DateUtils;
import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private Context context;
    private List<Post> list;
    private User currentUser; // 当前登录用户
    private int screenWidth;

    public PostAdapter(Context context, List<Post> list, User currentUser) {
        this.context = context;
        this.list = list;
        this.currentUser = currentUser;
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
        holder.tvContent.setTextSize(hasMedia ? 15 : 17);
        holder.tvContent.setVisibility(post.content == null || post.content.isEmpty() ? View.GONE : View.VISIBLE);

        // 3. 媒体显示逻辑
        holder.mediaContainer.removeAllViews();
        // 默认恢复 padding (因为 View 是复用的)
        int defaultPadding = dp2px(context, 12);
        holder.mediaContainer.setPadding(defaultPadding, 0, defaultPadding, 0);

        if (hasMedia) {
            if (post.type == 2) {
                // ============ 视频帖子优化 ============

                // 3.1 去除 padding，使视频宽度撑满卡片
                holder.mediaContainer.setPadding(0, 0, 0, 0);

                String videoUrl = post.mediaList.get(0).url;

                // 3.2 宽度使用 MATCH_PARENT (XML中父布局去除了padding)，高度按比例计算
                // CardView 有 marginHorizontal 10dp * 2 = 20dp
                int cardWidth = screenWidth - dp2px(context, 20);
                int videoHeight = (int) (cardWidth * 0.56f); // 16:9 比例

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

                // 封面图
                ImageView coverImage = new ImageView(context);
                coverImage.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                coverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(context).load(videoUrl).frame(1000000).into(coverImage);

                // 播放图标
                ImageView playIcon = new ImageView(context);
                playIcon.setImageResource(android.R.drawable.ic_media_play);
                FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(120, 120);
                iconParams.gravity = Gravity.CENTER;
                playIcon.setLayoutParams(iconParams);

                frameLayout.addView(videoView);
                frameLayout.addView(coverImage);
                frameLayout.addView(playIcon);
                holder.mediaContainer.addView(frameLayout);

                // 3.3 点击直接播放，不跳转详情
                playIcon.setOnClickListener(v -> {
                    coverImage.setVisibility(View.GONE);
                    playIcon.setVisibility(View.GONE);
                    videoView.start();
                });

                videoView.setOnCompletionListener(mp -> playIcon.setVisibility(View.VISIBLE));

            } else {
                // ============ 图片显示逻辑 (保留 padding) ============
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
                    // 图片裁剪圆角
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

                    // 计算 Grid Item 宽度时要减去 padding
                    int padding = 12 * 2 + 20; // content padding + card margin
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

        // ============ 互动按钮 ============
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
            // 传递当前用户给详情页，用于发表评论
            if (currentUser != null) {
                intent.putExtra("user", currentUser);
            }
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
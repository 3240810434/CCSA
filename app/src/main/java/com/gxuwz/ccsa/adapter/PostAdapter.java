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
import android.widget.RelativeLayout;
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
    private User currentUser;
    private int screenWidth;
    private int screenHeight;
    // 预估的导航栏总高度 (顶部标题 + 状态栏 + 底部导航)，用于限制视频最大高度
    private static final int NAV_BARS_HEIGHT_DP = 160;

    public PostAdapter(Context context, List<Post> list, User currentUser) {
        this.context = context;
        this.list = list;
        this.currentUser = currentUser;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
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
        // 恢复 padding (因为 View 是复用的)
        int defaultPadding = dp2px(context, 12);
        holder.mediaContainer.setPadding(defaultPadding, 0, defaultPadding, 0);

        // 默认显示底部的互动栏
        holder.layoutBottomBar.setVisibility(View.VISIBLE);

        if (hasMedia) {
            if (post.type == 2) {
                // ============ 视频帖子优化 ============

                // 隐藏原有的底部互动栏
                holder.layoutBottomBar.setVisibility(View.GONE);

                // 去除 padding，使视频宽度撑满卡片
                holder.mediaContainer.setPadding(0, 0, 0, 0);

                String videoUrl = post.mediaList.get(0).url;
                int cardWidth = screenWidth - dp2px(context, 20); // CardView margin

                // 创建相对布局作为容器，用于放置视频和右侧按钮
                RelativeLayout relativeLayout = new RelativeLayout(context);
                relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                // VideoView
                VideoView videoView = new VideoView(context);
                RelativeLayout.LayoutParams videoParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                videoParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                videoView.setLayoutParams(videoParams);
                videoView.setVideoPath(videoUrl);

                // 封面图
                ImageView coverImage = new ImageView(context);
                coverImage.setLayoutParams(new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                coverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(context).load(videoUrl).frame(1000000).into(coverImage);

                // 播放图标
                ImageView playIcon = new ImageView(context);
                playIcon.setImageResource(android.R.drawable.ic_media_play);
                RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(120, 120);
                iconParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                playIcon.setLayoutParams(iconParams);

                // ============ 侧边栏按钮 (点赞/点踩/评论) ============
                LinearLayout sideBar = new LinearLayout(context);
                sideBar.setOrientation(LinearLayout.VERTICAL);
                sideBar.setGravity(Gravity.CENTER_HORIZONTAL);
                RelativeLayout.LayoutParams sideBarParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                sideBarParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                sideBarParams.addRule(RelativeLayout.CENTER_VERTICAL);
                sideBarParams.setMargins(0, 0, dp2px(context, 10), 0);
                sideBar.setLayoutParams(sideBarParams);

                // 创建侧边栏按钮的方法
                ImageView btnLike = createSideIcon(context, post.isLiked ? R.drawable.liked : R.drawable.like);
                ImageView btnDislike = createSideIcon(context, post.isDisliked ? R.drawable.disliked : R.drawable.dislike);
                ImageView btnComment = createSideIcon(context, R.drawable.ic_notice);

                sideBar.addView(btnLike);
                sideBar.addView(btnDislike);
                sideBar.addView(btnComment);

                // 添加到容器
                relativeLayout.addView(videoView);
                relativeLayout.addView(coverImage);
                relativeLayout.addView(playIcon);
                relativeLayout.addView(sideBar);
                holder.mediaContainer.addView(relativeLayout);

                // 视频尺寸动态计算与限制
                videoView.setOnPreparedListener(mp -> {
                    mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                    int videoW = mp.getVideoWidth();
                    int videoH = mp.getVideoHeight();

                    if (videoW > 0 && videoH > 0) {
                        // 1. 根据宽度计算等比高度
                        float ratio = (float) videoH / videoW;
                        int targetHeight = (int) (cardWidth * ratio);

                        // 2. 限制最大高度 (屏幕高度 - 导航栏预估高度)
                        int maxHeight = screenHeight - dp2px(context, NAV_BARS_HEIGHT_DP);
                        if (targetHeight > maxHeight) {
                            targetHeight = maxHeight;
                        }

                        // 3. 应用高度
                        ViewGroup.LayoutParams lp = relativeLayout.getLayoutParams();
                        lp.height = targetHeight;
                        relativeLayout.setLayoutParams(lp);
                    }
                });

                // 点击播放
                playIcon.setOnClickListener(v -> {
                    coverImage.setVisibility(View.GONE);
                    playIcon.setVisibility(View.GONE);
                    videoView.start();
                });
                videoView.setOnCompletionListener(mp -> playIcon.setVisibility(View.VISIBLE));

                // 侧边栏按钮点击事件
                btnLike.setOnClickListener(v -> {
                    if (post.isDisliked) {
                        post.isDisliked = false;
                        btnDislike.setImageResource(R.drawable.dislike);
                    }
                    post.isLiked = !post.isLiked;
                    btnLike.setImageResource(post.isLiked ? R.drawable.liked : R.drawable.like);
                });

                btnDislike.setOnClickListener(v -> {
                    if (post.isLiked) {
                        post.isLiked = false;
                        btnLike.setImageResource(R.drawable.like);
                    }
                    post.isDisliked = !post.isDisliked;
                    btnDislike.setImageResource(post.isDisliked ? R.drawable.disliked : R.drawable.dislike);
                });

                btnComment.setOnClickListener(v -> openDetail(post));

            } else {
                // ============ 图片显示逻辑 (保持原样) ============
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
                    int padding = 12 * 2 + 20;
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

                // 非视频帖子，绑定底部按钮事件
                bindBottomActions(holder, post);
            }
        } else {
            // 纯文本帖子，绑定底部按钮事件
            bindBottomActions(holder, post);
        }
    }

    // 绑定底部互动栏事件
    private void bindBottomActions(PostViewHolder holder, Post post) {
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

        holder.layoutComment.setOnClickListener(v -> openDetail(post));
    }

    private void openDetail(Post post) {
        Intent intent = new Intent(context, PostDetailActivity.class);
        intent.putExtra("post", post);
        if (currentUser != null) {
            intent.putExtra("user", currentUser);
        }
        context.startActivity(intent);
    }

    // 创建侧边栏图标的辅助方法
    private ImageView createSideIcon(Context context, int resId) {
        ImageView iv = new ImageView(context);
        iv.setImageResource(resId);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp2px(context, 32), dp2px(context, 32));
        params.setMargins(0, 0, 0, dp2px(context, 20)); // 垂直间距
        iv.setLayoutParams(params);
        // 添加阴影以确保在亮色视频背景上可见
        iv.setElevation(4f);
        return iv;
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
        LinearLayout layoutBottomBar; // 新增：底部整个互动栏
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
            layoutBottomBar = itemView.findViewById(R.id.layout_bottom_bar); // 获取底部栏引用
            ivLike = itemView.findViewById(R.id.iv_like);
            ivDislike = itemView.findViewById(R.id.iv_dislike);
        }
    }
}
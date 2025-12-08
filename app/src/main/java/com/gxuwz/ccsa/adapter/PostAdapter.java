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

    // 估算上下导航栏及状态栏的总高度 (单位 dp)
    // 顶部标题栏(约48) + 状态栏(约24) + 底部导航(约56) + 顶部Tab(约40) + 边距余量
    private static final int OCCUPIED_HEIGHT_DP = 180;

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

                // 计算固定高度：屏幕高度 - 上下导航栏占用的高度
                int fixedVideoHeight = screenHeight - dp2px(context, OCCUPIED_HEIGHT_DP);

                // 确保高度至少是宽度的一半，防止屏幕计算异常导致太矮
                if (fixedVideoHeight < screenWidth / 2) {
                    fixedVideoHeight = screenWidth; // 降级策略
                }

                // 创建相对布局作为容器
                RelativeLayout relativeLayout = new RelativeLayout(context);
                // 强制设置容器高度，确保不会是白色或0高度
                RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, fixedVideoHeight);
                relativeLayout.setLayoutParams(containerParams);
                relativeLayout.setBackgroundColor(0xFF000000); // 设置黑色背景，避免视频加载前显示白色

                // VideoView
                VideoView videoView = new VideoView(context);
                RelativeLayout.LayoutParams videoParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                videoParams.addRule(RelativeLayout.CENTER_IN_PARENT); // 视频居中
                videoView.setLayoutParams(videoParams);
                videoView.setVideoPath(videoUrl);

                // 封面图 (填满容器)
                ImageView coverImage = new ImageView(context);
                coverImage.setLayoutParams(new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                coverImage.setScaleType(ImageView.ScaleType.CENTER_CROP); // 裁剪填满
                Glide.with(context).load(videoUrl).frame(1000000).into(coverImage);

                // 播放图标
                ImageView playIcon = new ImageView(context);
                playIcon.setImageResource(android.R.drawable.ic_media_play);
                RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(150, 150); //稍微调大一点图标
                iconParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                playIcon.setLayoutParams(iconParams);
                playIcon.setElevation(10f); // 确保图标在最上层

                // ============ 侧边栏按钮 (点赞/点踩/评论) ============
                LinearLayout sideBar = new LinearLayout(context);
                sideBar.setOrientation(LinearLayout.VERTICAL);
                sideBar.setGravity(Gravity.CENTER_HORIZONTAL);
                RelativeLayout.LayoutParams sideBarParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                sideBarParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                sideBarParams.addRule(RelativeLayout.CENTER_VERTICAL); // 垂直居中
                sideBarParams.setMargins(0, 0, dp2px(context, 10), 0);
                sideBar.setLayoutParams(sideBarParams);
                sideBar.setElevation(10f); // 确保侧边栏在视频上方

                ImageView btnLike = createSideIcon(context, post.isLiked ? R.drawable.liked : R.drawable.like);
                ImageView btnDislike = createSideIcon(context, post.isDisliked ? R.drawable.disliked : R.drawable.dislike);
                ImageView btnComment = createSideIcon(context, R.drawable.ic_notice);

                sideBar.addView(btnLike);
                sideBar.addView(btnDislike);
                sideBar.addView(btnComment);

                // 添加到容器 (注意添加顺序，后添加的在上面)
                relativeLayout.addView(videoView);
                relativeLayout.addView(coverImage);
                relativeLayout.addView(playIcon);
                relativeLayout.addView(sideBar);
                holder.mediaContainer.addView(relativeLayout);

                // 视频准备监听 - 不再改变高度，只处理视频居中逻辑
                videoView.setOnPreparedListener(mp -> {
                    mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                    // 如果需要视频完全铺满不留黑边（可能会裁剪画面），可以使用：
                    // mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                });

                videoView.setOnErrorListener((mp, what, extra) -> {
                    // 视频加载错误处理，防止卡死
                    return true;
                });

                // 点击播放逻辑
                View.OnClickListener playAction = v -> {
                    coverImage.setVisibility(View.GONE);
                    playIcon.setVisibility(View.GONE);
                    if (!videoView.isPlaying()) {
                        videoView.start();
                    }
                };

                // 让播放图标和封面图都响应点击
                playIcon.setOnClickListener(playAction);
                coverImage.setOnClickListener(playAction);

                videoView.setOnCompletionListener(mp -> {
                    playIcon.setVisibility(View.VISIBLE);
                    // 播放完不显示封面图，或者你可以选择显示 coverImage.setVisibility(View.VISIBLE);
                });

                // 侧边栏交互
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

                bindBottomActions(holder, post);
            }
        } else {
            bindBottomActions(holder, post);
        }
    }

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

    private ImageView createSideIcon(Context context, int resId) {
        ImageView iv = new ImageView(context);
        iv.setImageResource(resId);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp2px(context, 35), dp2px(context, 35)); // 稍微调大按钮尺寸
        params.setMargins(0, 0, 0, dp2px(context, 25)); // 增加垂直间距
        iv.setLayoutParams(params);
        // 添加阴影
        iv.setElevation(6f);
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
        LinearLayout layoutBottomBar;
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
            layoutBottomBar = itemView.findViewById(R.id.layout_bottom_bar);
            ivLike = itemView.findViewById(R.id.iv_like);
            ivDislike = itemView.findViewById(R.id.iv_dislike);
        }
    }
}
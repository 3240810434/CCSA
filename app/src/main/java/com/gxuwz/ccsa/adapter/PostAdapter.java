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

        // 2. 文字大小逻辑
        boolean hasMedia = post.mediaList != null && !post.mediaList.isEmpty();
        holder.tvContent.setText(post.content);
        holder.tvContent.setTextSize(hasMedia ? 15 : 18); // 无媒体时文字变大
        holder.tvContent.setVisibility(post.content == null || post.content.isEmpty() ? View.GONE : View.VISIBLE);

        // 3. 媒体显示逻辑
        holder.mediaContainer.removeAllViews();

        if (hasMedia) {
            if (post.type == 2) {
                // ============ 1. 视频优化 ============
                // 视频卡片占满宽度，高度设为屏幕宽度的 9/16 (常用视频比例) 或固定值
                int videoHeight = (int) (screenWidth * 0.56f); // 16:9 比例

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
                videoView.setVideoPath(post.mediaList.get(0).url);

                // 播放图标 (居中)
                ImageView playIcon = new ImageView(context);
                playIcon.setImageResource(android.R.drawable.ic_media_play);
                FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(120, 120);
                iconParams.gravity = Gravity.CENTER;
                playIcon.setLayoutParams(iconParams);

                frameLayout.addView(videoView);
                frameLayout.addView(playIcon);
                holder.mediaContainer.addView(frameLayout);

                // 点击视频区域进入详情页播放（为了列表流畅性，不建议在列表中直接播放）
                View.OnClickListener listener = v -> {
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("post", post);
                    context.startActivity(intent);
                };
                playIcon.setOnClickListener(listener);
                // 如果需要点击任何地方都进详情
                videoView.setOnClickListener(listener);

            } else {
                // ============ 2. 图片优化 ============
                int imgCount = post.mediaList.size();
                // 准备图片URL列表供预览使用
                ArrayList<String> imgUrls = new ArrayList<>();
                for(int k=0; k<imgCount; k++) imgUrls.add(post.mediaList.get(k).url);

                if (imgCount == 1) {
                    // 单图
                    ImageView imageView = new ImageView(context);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 600); // 单图限制最大高度
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(ImageView.ScaleType.FIT_START); // 靠上对齐，宽度自适应
                    imageView.setAdjustViewBounds(true);

                    Glide.with(context)
                            .load(post.mediaList.get(0).url)
                            .transform(new CenterCrop(), new RoundedCorners(16))
                            .into(imageView);

                    holder.mediaContainer.addView(imageView);

                    // 点击单图 -> 放大查看
                    imageView.setOnClickListener(v -> {
                        Intent intent = new Intent(context, ImagePreviewActivity.class);
                        intent.putStringArrayListExtra("images", imgUrls);
                        intent.putExtra("position", 0);
                        context.startActivity(intent);
                    });

                } else {
                    // 多图 Grid
                    GridLayout gridLayout = new GridLayout(context);
                    gridLayout.setColumnCount(3);
                    gridLayout.setRowCount((imgCount + 2) / 3);

                    int padding = 40; // dp转px估算
                    int itemSize = (screenWidth - dp2px(context, padding)) / 3;

                    for (int i = 0; i < imgCount; i++) {
                        String url = post.mediaList.get(i).url;
                        ImageView iv = new ImageView(context);
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.width = itemSize;
                        params.height = itemSize;
                        params.setMargins(4, 4, 4, 4); // 增加一点间距
                        iv.setLayoutParams(params);
                        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

                        Glide.with(context).load(url).into(iv);

                        final int pos = i;
                        // 点击多图中的某张 -> 放大查看
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

        // ============ 2.1 互动优化 (点赞/点踩) ============

        // 设置初始状态图标
        holder.ivLike.setImageResource(post.isLiked ? R.drawable.liked : R.drawable.like);
        holder.ivDislike.setImageResource(post.isDisliked ? R.drawable.disliked : R.drawable.dislike);

        // 点赞点击事件
        holder.layoutLike.setOnClickListener(v -> {
            if (post.isDisliked) {
                // 如果已经点了踩，先取消踩
                post.isDisliked = false;
                holder.ivDislike.setImageResource(R.drawable.dislike);
            }
            // 切换点赞状态
            post.isLiked = !post.isLiked;
            holder.ivLike.setImageResource(post.isLiked ? R.drawable.liked : R.drawable.like);
            // 可以在这里添加动画效果
        });

        // 点踩点击事件
        holder.layoutDislike.setOnClickListener(v -> {
            if (post.isLiked) {
                // 如果已经点了赞，先取消赞
                post.isLiked = false;
                holder.ivLike.setImageResource(R.drawable.like);
            }
            // 切换点踩状态
            post.isDisliked = !post.isDisliked;
            holder.ivDislike.setImageResource(post.isDisliked ? R.drawable.disliked : R.drawable.dislike);
        });

        // 评论点击 -> 进入详情
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

        // 底部按钮区域
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
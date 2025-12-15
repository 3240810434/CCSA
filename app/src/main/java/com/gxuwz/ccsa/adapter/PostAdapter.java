package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

    private static final int OCCUPIED_HEIGHT_DP = 180;

    // 删除事件监听器
    public interface OnDeleteListener {
        void onDelete(Post post);
    }
    private OnDeleteListener deleteListener;

    public void setDeleteListener(OnDeleteListener listener) {
        this.deleteListener = listener;
    }

    public PostAdapter(Context context, List<Post> list, User currentUser) {
        this.context = context;
        this.list = list;
        this.currentUser = currentUser;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        notifyDataSetChanged();
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
        holder.tvCommentCount.setText(post.commentCount > 0 ? String.valueOf(post.commentCount) : "评论");

        Glide.with(context)
                .load(post.userAvatar)
                .placeholder(R.drawable.lan)
                .error(R.drawable.lan)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(100)))
                .into(holder.ivAvatar);

        boolean hasMedia = post.mediaList != null && !post.mediaList.isEmpty();
        holder.tvContent.setText(post.content);
        holder.tvContent.setTextSize(hasMedia ? 15 : 17);
        holder.tvContent.setVisibility(post.content == null || post.content.isEmpty() ? View.GONE : View.VISIBLE);

        holder.mediaContainer.removeAllViews();
        int defaultPadding = dp2px(context, 12);
        holder.mediaContainer.setPadding(defaultPadding, 0, defaultPadding, 0);

        holder.layoutBottomBar.setVisibility(View.VISIBLE);

        // --- 处理删除按钮逻辑 (非视频模式) ---
        // 查找是否已经添加过删除按钮，避免重复添加
        View existingDeleteBtn = holder.layoutBottomBar.findViewWithTag("DELETE_BTN");
        if (deleteListener != null && post.type != 2) {
            if (existingDeleteBtn == null) {
                TextView deleteBtn = new TextView(context);
                deleteBtn.setText("删除");
                deleteBtn.setTextColor(Color.RED);
                deleteBtn.setTag("DELETE_BTN");
                deleteBtn.setPadding(20, 0, 20, 0);
                deleteBtn.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                deleteBtn.setLayoutParams(params);
                holder.layoutBottomBar.addView(deleteBtn);
                existingDeleteBtn = deleteBtn;
            }
            existingDeleteBtn.setVisibility(View.VISIBLE);
            existingDeleteBtn.setOnClickListener(v -> deleteListener.onDelete(post));
        } else {
            if (existingDeleteBtn != null) {
                existingDeleteBtn.setVisibility(View.GONE);
            }
        }

        if (hasMedia) {
            if (post.type == 2) {
                // ============ 视频帖子 ============
                holder.layoutBottomBar.setVisibility(View.GONE);
                holder.mediaContainer.setPadding(0, 0, 0, 0);

                String videoUrl = post.mediaList.get(0).url;
                int fixedVideoHeight = screenHeight - dp2px(context, OCCUPIED_HEIGHT_DP);
                if (fixedVideoHeight < screenWidth / 2) {
                    fixedVideoHeight = screenWidth;
                }

                RelativeLayout relativeLayout = new RelativeLayout(context);
                RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, fixedVideoHeight);
                relativeLayout.setLayoutParams(containerParams);
                relativeLayout.setBackgroundColor(0xFF000000);

                VideoView videoView = new VideoView(context);
                RelativeLayout.LayoutParams videoParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                videoParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                videoView.setLayoutParams(videoParams);
                videoView.setVideoPath(videoUrl);

                ImageView coverImage = new ImageView(context);
                coverImage.setLayoutParams(new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                coverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(context).load(videoUrl).frame(1000000).into(coverImage);

                ImageView playIcon = new ImageView(context);
                playIcon.setImageResource(android.R.drawable.ic_media_play);
                RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(150, 150);
                iconParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                playIcon.setLayoutParams(iconParams);
                playIcon.setElevation(10f);

                // ============ 侧边栏按钮 ============
                LinearLayout sideBar = new LinearLayout(context);
                sideBar.setOrientation(LinearLayout.VERTICAL);
                sideBar.setGravity(Gravity.CENTER_HORIZONTAL);
                RelativeLayout.LayoutParams sideBarParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                sideBarParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                sideBarParams.addRule(RelativeLayout.CENTER_VERTICAL);
                sideBarParams.setMargins(0, 0, dp2px(context, 10), 0);
                sideBar.setLayoutParams(sideBarParams);
                sideBar.setElevation(10f);

                ImageView btnLike = createSideIcon(context, post.isLiked ? R.drawable.liked : R.drawable.like);
                ImageView btnFavorite = createSideIcon(context, post.isDisliked ? R.drawable.favorited : R.drawable.favorite);
                ImageView btnComment = createSideIcon(context, R.drawable.video_comments);

                sideBar.addView(btnLike);
                sideBar.addView(btnFavorite);
                sideBar.addView(btnComment);

                // --- 视频模式下的删除按钮 ---
                if (deleteListener != null) {
                    ImageView btnDelete = createSideIcon(context, android.R.drawable.ic_menu_delete);
                    // 这里使用了系统自带的删除图标，如果项目有 ic_delete.png 请替换为 R.drawable.ic_delete
                    btnDelete.setColorFilter(Color.WHITE);
                    btnDelete.setOnClickListener(v -> deleteListener.onDelete(post));
                    sideBar.addView(btnDelete);
                }

                relativeLayout.addView(videoView);
                relativeLayout.addView(coverImage);
                relativeLayout.addView(playIcon);
                relativeLayout.addView(sideBar);
                holder.mediaContainer.addView(relativeLayout);

                videoView.setOnPreparedListener(mp -> {
                    mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                });

                videoView.setOnErrorListener((mp, what, extra) -> true);

                View.OnClickListener playAction = v -> {
                    coverImage.setVisibility(View.GONE);
                    playIcon.setVisibility(View.GONE);
                    if (!videoView.isPlaying()) {
                        videoView.start();
                    }
                };

                playIcon.setOnClickListener(playAction);
                coverImage.setOnClickListener(playAction);

                videoView.setOnCompletionListener(mp -> playIcon.setVisibility(View.VISIBLE));

                btnLike.setOnClickListener(v -> {
                    if (post.isDisliked) {
                        post.isDisliked = false;
                        btnFavorite.setImageResource(R.drawable.favorite);
                    }
                    post.isLiked = !post.isLiked;
                    btnLike.setImageResource(post.isLiked ? R.drawable.liked : R.drawable.like);
                });

                btnFavorite.setOnClickListener(v -> {
                    if (post.isLiked) {
                        post.isLiked = false;
                        btnLike.setImageResource(R.drawable.like);
                    }
                    post.isDisliked = !post.isDisliked;
                    btnFavorite.setImageResource(post.isDisliked ? R.drawable.favorited : R.drawable.favorite);
                });

                btnComment.setOnClickListener(v -> openDetail(post));

            } else {
                // 图片处理逻辑
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
        holder.ivLike.setImageResource(post.isLiked ? R.drawable.liked2 : R.drawable.like2);
        holder.ivDislike.setImageResource(post.isDisliked ? R.drawable.favorited2 : R.drawable.favorite2);

        holder.layoutLike.setOnClickListener(v -> {
            if (post.isDisliked) {
                post.isDisliked = false;
                holder.ivDislike.setImageResource(R.drawable.favorite2);
            }
            post.isLiked = !post.isLiked;
            holder.ivLike.setImageResource(post.isLiked ? R.drawable.liked2 : R.drawable.like2);
        });

        holder.layoutDislike.setOnClickListener(v -> {
            if (post.isLiked) {
                post.isLiked = false;
                holder.ivLike.setImageResource(R.drawable.like2);
            }
            post.isDisliked = !post.isDisliked;
            holder.ivDislike.setImageResource(post.isDisliked ? R.drawable.favorited2 : R.drawable.favorite2);
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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp2px(context, 35), dp2px(context, 35));
        params.setMargins(0, 0, 0, dp2px(context, 25));
        iv.setLayoutParams(params);
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
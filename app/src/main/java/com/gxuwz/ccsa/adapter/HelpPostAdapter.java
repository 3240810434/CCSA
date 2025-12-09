package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
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
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.HelpPost;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.ui.resident.ChatActivity;
import com.gxuwz.ccsa.ui.resident.ImagePreviewActivity;
import com.gxuwz.ccsa.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class HelpPostAdapter extends RecyclerView.Adapter<HelpPostAdapter.ViewHolder> {

    private Context context;
    private List<HelpPost> list;
    private User currentUser;

    private int screenWidth;
    private int screenHeight;
    private static final int OCCUPIED_HEIGHT_DP = 220;

    public HelpPostAdapter(Context context, List<HelpPost> list, User currentUser) {
        this.context = context;
        this.list = list;
        this.currentUser = currentUser;

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
    }

    // 【关键修复】提供方法更新 currentUser，防止 Adapter 持有旧的用户对象
    public void setCurrentUser(User user) {
        this.currentUser = user;
        notifyDataSetChanged(); // 用户身份变化可能影响“联系”按钮的显示
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
        // 优先显示动态查询到的用户名，解决改名不同步问题
        holder.tvAuthor.setText(post.userName != null ? post.userName : "未知邻居");
        holder.tvTime.setText(DateUtils.formatTime(post.createTime));

        Glide.with(context)
                .load(post.userAvatar)
                .placeholder(R.drawable.lan)
                .error(R.drawable.lan)
                .circleCrop()
                .into(holder.ivAvatar);

        // 媒体内容处理 (保持原有逻辑)
        holder.mediaContainer.removeAllViews();
        if (post.mediaList != null && !post.mediaList.isEmpty()) {
            holder.mediaContainer.setVisibility(View.VISIBLE);
            int mediaType = post.mediaList.get(0).type;

            if (mediaType == 2) {
                // 视频处理逻辑
                String videoUrl = post.mediaList.get(0).url;
                int fixedVideoHeight = screenHeight - dp2px(context, OCCUPIED_HEIGHT_DP);
                if (fixedVideoHeight < screenWidth / 2) fixedVideoHeight = screenWidth;

                RelativeLayout relativeLayout = new RelativeLayout(context);
                relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, fixedVideoHeight));
                relativeLayout.setBackgroundColor(0xFF000000);

                VideoView videoView = new VideoView(context);
                RelativeLayout.LayoutParams videoParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                videoParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                videoView.setLayoutParams(videoParams);
                videoView.setVideoPath(videoUrl);

                ImageView coverImage = new ImageView(context);
                coverImage.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                coverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(context).load(videoUrl).frame(1000000).into(coverImage);

                ImageView playIcon = new ImageView(context);
                playIcon.setImageResource(android.R.drawable.ic_media_play);
                RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(150, 150);
                iconParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                playIcon.setLayoutParams(iconParams);

                relativeLayout.addView(videoView);
                relativeLayout.addView(coverImage);
                relativeLayout.addView(playIcon);
                holder.mediaContainer.addView(relativeLayout);

                videoView.setOnPreparedListener(mp -> mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT));
                videoView.setOnErrorListener((mp, what, extra) -> true);

                View.OnClickListener playAction = v -> {
                    coverImage.setVisibility(View.GONE);
                    playIcon.setVisibility(View.GONE);
                    if (!videoView.isPlaying()) videoView.start();
                };
                playIcon.setOnClickListener(playAction);
                coverImage.setOnClickListener(playAction);
                videoView.setOnCompletionListener(mp -> playIcon.setVisibility(View.VISIBLE));
            } else {
                // 图片处理逻辑
                int imgCount = post.mediaList.size();
                ArrayList<String> imgUrls = new ArrayList<>();
                for (int k = 0; k < imgCount; k++) imgUrls.add(post.mediaList.get(k).url);

                if (imgCount == 1) {
                    ImageView imageView = new ImageView(context);
                    imageView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600));
                    imageView.setScaleType(ImageView.ScaleType.FIT_START);
                    imageView.setAdjustViewBounds(true);
                    Glide.with(context).load(post.mediaList.get(0).url).transform(new CenterCrop(), new RoundedCorners(16)).into(imageView);
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
                    int padding = dp2px(context, 20);
                    int itemSize = (screenWidth - padding) / 3;
                    for (int i = 0; i < imgCount; i++) {
                        String url = post.mediaList.get(i).url;
                        ImageView iv = new ImageView(context);
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED));
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
        } else {
            holder.mediaContainer.setVisibility(View.GONE);
        }

        // 联系按钮逻辑
        if (currentUser != null && post.userId == currentUser.getId()) {
            holder.llContact.setVisibility(View.GONE); // 自己不能联系自己
        } else {
            holder.llContact.setVisibility(View.VISIBLE);
            holder.llContact.setOnClickListener(v -> {
                // 【关键】：这里传递最新的 currentUser，确保 ID 是正确的
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

    private int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvAuthor, tvTime, tvTitle, tvContent;
        FrameLayout mediaContainer;
        LinearLayout llContact;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvAuthor = itemView.findViewById(R.id.tv_author_name);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
            mediaContainer = itemView.findViewById(R.id.media_container);
            llContact = itemView.findViewById(R.id.ll_contact);
        }
    }
}
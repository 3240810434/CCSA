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
import android.widget.Toast;
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
// 【新增】引入详情页 Activity
import com.gxuwz.ccsa.ui.resident.HelpPostDetailActivity;
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

    // 新增删除监听器
    public interface OnDeleteListener {
        void onDelete(HelpPost post);
    }
    private OnDeleteListener deleteListener;

    public void setDeleteListener(OnDeleteListener listener) {
        this.deleteListener = listener;
    }

    public HelpPostAdapter(Context context, List<HelpPost> list, User currentUser) {
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

        // 【新增】设置整个 Item 点击跳转详情
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, HelpPostDetailActivity.class);
            intent.putExtra("helpPost", post);
            intent.putExtra("user", currentUser);
            context.startActivity(intent);
        });

        // 媒体内容处理
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

        // --- 联系/删除 按钮逻辑 ---
        if (deleteListener != null) {
            // "我的互助" 模式：显示删除按钮
            holder.llContact.setVisibility(View.VISIBLE);

            // 清除可能存在的旧视图
            holder.llContact.removeAllViews();

            TextView tvDelete = new TextView(context);
            tvDelete.setText("删除求助");
            tvDelete.setTextColor(Color.RED);
            tvDelete.setTextSize(14);
            tvDelete.setGravity(Gravity.CENTER);
            holder.llContact.addView(tvDelete);

            // 设置删除背景和点击事件
            holder.llContact.setBackgroundResource(R.drawable.button_border_gray);
            holder.llContact.setOnClickListener(v -> deleteListener.onDelete(post));

        } else {
            // 正常浏览模式
            // 简单重置逻辑：如果第一个子 View 是 Textview 且内容是 "删除求助"，则清空并加回图标
            boolean isModified = false;
            if(holder.llContact.getChildCount() > 0 && holder.llContact.getChildAt(0) instanceof TextView) {
                TextView tv = (TextView) holder.llContact.getChildAt(0);
                if("删除求助".equals(tv.getText().toString())) {
                    isModified = true;
                }
            }

            if (isModified) {
                holder.llContact.removeAllViews();
                // 重新添加 图标和文字 (硬编码恢复布局)
                ImageView iv = new ImageView(context);
                iv.setImageResource(R.drawable.ic_contact); // 假设有这个资源，或者用 R.drawable.contact
                LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(dp2px(context, 16), dp2px(context, 16));
                iv.setLayoutParams(p1);

                TextView tv = new TextView(context);
                tv.setText("联系Ta");
                tv.setTextSize(12);
                tv.setTextColor(Color.parseColor("#666666"));
                LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                p2.leftMargin = dp2px(context, 4);
                tv.setLayoutParams(p2);

                holder.llContact.addView(iv);
                holder.llContact.addView(tv);
                holder.llContact.setBackgroundResource(R.drawable.btn_rounded_light_blue);
            }

            if (currentUser != null && post.userId == currentUser.getId()) {
                holder.llContact.setVisibility(View.GONE);
            } else {
                holder.llContact.setVisibility(View.VISIBLE);
                holder.llContact.setOnClickListener(v -> {
                    if (currentUser == null) {
                        Toast.makeText(context, "用户信息获取失败，请刷新或重新登录", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("myId", currentUser.getId());
                    intent.putExtra("myRole", "RESIDENT");
                    intent.putExtra("targetId", post.userId);
                    intent.putExtra("targetRole", "RESIDENT");
                    intent.putExtra("targetName", post.userName);
                    intent.putExtra("targetAvatar", post.userAvatar);
                    context.startActivity(intent);
                });
            }
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
package com.gxuwz.ccsa.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Comment;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.util.DateUtils;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private Context context;
    private List<Comment> list;
    private int currentUserId = 1; // 假定当前用户ID，实际项目中应从SharedPref或Activity获取

    public CommentAdapter(Context context, List<Comment> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = list.get(position);

        holder.tvContent.setText(comment.content);
        holder.tvTime.setText(DateUtils.getRelativeTime(comment.createTime));

        // 默认先显示 Comment 对象中存储的快照信息（防止网络/数据库延迟时空白）
        holder.tvName.setText(comment.userName);
        Glide.with(context)
                .load(comment.userAvatar)
                .placeholder(R.drawable.ic_avatar)
                .error(R.drawable.ic_avatar)
                .into(holder.ivAvatar);

        // ============ 核心修改：实时同步用户信息 ============
        // 开启线程查询该评论发布者的最新信息（头像、昵称）
        new Thread(() -> {
            // 根据 comment.userId 查询最新的 User 数据
            User latestUser = AppDatabase.getInstance(context).userDao().getUserById(comment.userId);

            if (latestUser != null && context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> {
                    // 在主线程更新UI
                    holder.tvName.setText(latestUser.getName());

                    Glide.with(context)
                            .load(latestUser.getAvatar()) // 加载最新头像
                            .placeholder(R.drawable.ic_avatar)
                            .error(R.drawable.ic_avatar)
                            .into(holder.ivAvatar);
                });
            }
        }).start();
        // ===============================================

        // 长按删除自己的评论
        holder.itemView.setOnLongClickListener(v -> {
            if (comment.userId == currentUserId) {
                new AlertDialog.Builder(context)
                        .setMessage("删除这条评论?")
                        .setPositiveButton("删除", (d, w) -> {
                            new Thread(() -> {
                                AppDatabase.getInstance(context).postDao().deleteComment(comment);
                                // UI更新需在Activity中回调，此处简化直接移除List
                                if (context instanceof Activity) {
                                    ((Activity) context).runOnUiThread(() -> {
                                        list.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, list.size());
                                    });
                                }
                            }).start();
                        }).show();
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvContent, tvTime;
        CircleImageView ivAvatar; // 使用 CircleImageView 或 ImageView

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }
}
package com.gxuwz.ccsa.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Comment;
import com.gxuwz.ccsa.util.DateUtils;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private Context context;
    private List<Comment> list;
    private int currentUserId = 1; // 实际开发中应从SharedPreferences或Session获取

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
        holder.tvName.setText(comment.userName);
        holder.tvContent.setText(comment.content);
        holder.tvTime.setText(DateUtils.getRelativeTime(comment.createTime));

        // 1. 评论优化：显示真实的头像
        // 使用 Glide 加载头像，并设置为圆形，如果为空则显示默认图
        Glide.with(context)
                .load(comment.userAvatar)
                .placeholder(R.drawable.lan) // 使用你提供的默认头像 lan
                .error(R.drawable.lan)
                .apply(RequestOptions.circleCropTransform()) // 圆形剪裁
                .into(holder.ivAvatar);

        // 长按删除自己的评论
        holder.itemView.setOnLongClickListener(v -> {
            if (comment.userId == currentUserId) {
                new AlertDialog.Builder(context)
                        .setMessage("删除这条评论?")
                        .setPositiveButton("删除", (d, w) -> {
                            new Thread(() -> {
                                AppDatabase.getInstance(context).postDao().deleteComment(comment);
                                ((android.app.Activity)context).runOnUiThread(() -> {
                                    list.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, list.size());
                                });
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
        ImageView ivAvatar; // 新增头像控件

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
            // 确保 item_comment.xml 中有这个 id，如果没有请添加
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }
}
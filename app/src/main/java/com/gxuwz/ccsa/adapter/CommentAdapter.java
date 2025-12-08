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
    private int currentUserId = 1; // 假定当前用户ID

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
        holder.tvName.setText(comment.userName);

        // ============ 修改点 1：默认头像改为 lan (lan.jpg) ============
        // 解决评论显示“黑人头像”的问题，默认加载 lan.jpg
        Glide.with(context)
                .load(comment.userAvatar)
                .placeholder(R.drawable.lan) // 修改这里
                .error(R.drawable.lan)       // 修改这里
                .into(holder.ivAvatar);

        // 实时同步用户信息
        new Thread(() -> {
            User latestUser = AppDatabase.getInstance(context).userDao().getUserById(comment.userId);
            if (latestUser != null && context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> {
                    holder.tvName.setText(latestUser.getName());
                    // 同步加载最新头像，同样使用 lan 作为占位
                    Glide.with(context)
                            .load(latestUser.getAvatar())
                            .placeholder(R.drawable.lan) // 修改这里
                            .error(R.drawable.lan)       // 修改这里
                            .into(holder.ivAvatar);
                });
            }
        }).start();

        // 长按删除
        holder.itemView.setOnLongClickListener(v -> {
            if (comment.userId == currentUserId) {
                new AlertDialog.Builder(context)
                        .setMessage("删除这条评论?")
                        .setPositiveButton("删除", (d, w) -> {
                            new Thread(() -> {
                                AppDatabase.getInstance(context).postDao().deleteComment(comment);
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
        CircleImageView ivAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }
}
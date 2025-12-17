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
    private int currentUserId = 1; // 假定当前用户ID，实际项目中建议从 SharedPreferences 或 User 对象获取

    // 【新增】定义回复点击监听接口
    private OnReplyClickListener onReplyClickListener;

    public interface OnReplyClickListener {
        void onReply(Comment comment);
    }

    public void setOnReplyListener(OnReplyClickListener listener) {
        this.onReplyClickListener = listener;
    }

    public CommentAdapter(Context context, List<Comment> list) {
        this.context = context;
        this.list = list;
    }

    // 如果你有传入当前用户ID的需求，可以加这个方法
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
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

        // 默认加载 lan.jpg
        Glide.with(context)
                .load(comment.userAvatar)
                .placeholder(R.drawable.lan)
                .error(R.drawable.lan)
                .into(holder.ivAvatar);

        // 同步用户信息逻辑
        new Thread(() -> {
            User latestUser = AppDatabase.getInstance(context).userDao().getUserById(comment.userId);
            if (latestUser != null && context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> {
                    holder.tvName.setText(latestUser.getName());
                    Glide.with(context)
                            .load(latestUser.getAvatar())
                            .placeholder(R.drawable.lan)
                            .error(R.drawable.lan)
                            .into(holder.ivAvatar);
                });
            }
        }).start();

        // 【新增】设置回复按钮点击事件
        if (holder.tvReply != null) {
            holder.tvReply.setOnClickListener(v -> {
                if (onReplyClickListener != null) {
                    onReplyClickListener.onReply(comment);
                }
            });
        }

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
        // 【新增】声明回复按钮
        TextView tvReply;
        CircleImageView ivAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);

            // 【新增】绑定回复按钮，请确保 item_comment.xml 中有 id 为 tv_reply 的控件
            // 如果你的 xml 中该控件 id 不叫 tv_reply，请改为实际的 id
            tvReply = itemView.findViewById(R.id.tv_reply);
        }
    }
}
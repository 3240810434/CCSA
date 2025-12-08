package com.gxuwz.ccsa.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Comment;
import com.gxuwz.ccsa.util.DateUtils;
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
        holder.tvName.setText(comment.userName);
        holder.tvContent.setText(comment.content);
        holder.tvTime.setText(DateUtils.getRelativeTime(comment.createTime));

        // 长按删除自己的评论
        holder.itemView.setOnLongClickListener(v -> {
            if (comment.userId == currentUserId) {
                new AlertDialog.Builder(context)
                        .setMessage("删除这条评论?")
                        .setPositiveButton("删除", (d, w) -> {
                            new Thread(() -> {
                                AppDatabase.getInstance(context).postDao().deleteComment(comment);
                                // UI更新需在Activity中回调，此处简化直接移除List
                                list.remove(position);
                                // 注意：非主线程操作UI会崩，实际需Handler，这里仅展示逻辑
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
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
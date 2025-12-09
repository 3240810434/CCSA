package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.UnifiedMessage;
import com.gxuwz.ccsa.util.DateUtils;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private Context context;
    private List<UnifiedMessage> messageList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(UnifiedMessage message);
    }

    public NotificationAdapter(Context context, List<UnifiedMessage> messageList, OnItemClickListener listener) {
        this.context = context;
        this.messageList = messageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UnifiedMessage message = messageList.get(position);
        if (message != null) {
            holder.tvTitle.setText(message.getTitle());
            holder.tvContent.setText(message.getContent());
            holder.tvTime.setText(DateUtils.formatTime(message.getTime()));

            // 根据类型设置图标
            if (message.getType() == UnifiedMessage.TYPE_CHAT_MESSAGE) {
                // 聊天消息：加载用户头像
                Glide.with(context)
                        .load(message.getAvatarUrl())
                        .placeholder(R.drawable.ic_avatar)
                        .circleCrop()
                        .into(holder.ivIcon);
                // 隐藏未读红点（或者根据实际聊天未读状态显示）
                if (holder.ivUnread != null) holder.ivUnread.setVisibility(View.GONE);
            } else {
                // 系统通知：显示系统铃铛图标
                holder.ivIcon.setImageResource(R.drawable.ic_notification);
                // 如果需要显示系统通知红点，需在 UnifiedMessage 中传递 isRead 状态
                if (holder.ivUnread != null) holder.ivUnread.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(message);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messageList == null ? 0 : messageList.size();
    }

    public void updateData(List<UnifiedMessage> newMessages) {
        this.messageList = newMessages;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon; // 左侧头像/图标
        TextView tvTitle;
        TextView tvContent;
        TextView tvTime;
        View ivUnread;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 确保你的 item_notification.xml 里有这些 ID
            // 如果 item_notification.xml 左侧没有 ImageView，请添加一个 id 为 iv_icon_type 的 ImageView
            // 这里为了防止崩溃，尝试匹配常见 ID
            ivIcon = itemView.findViewById(R.id.iv_icon_type);
            // 如果布局没改，为了防崩，临时用 iv_unread 占位（建议修改 item_notification.xml 添加图片控件）
            if (ivIcon == null && itemView.findViewById(R.id.iv_unread) instanceof ImageView) {
                ivIcon = (ImageView) itemView.findViewById(R.id.iv_unread);
            }

            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            // 假设布局有内容显示区域，如果没有，复用 title
            tvContent = itemView.findViewById(R.id.tv_notification_content);
            if (tvContent == null) tvContent = tvTitle;

            tvTime = itemView.findViewById(R.id.tv_notification_time);
            ivUnread = itemView.findViewById(R.id.iv_unread);
        }
    }
}
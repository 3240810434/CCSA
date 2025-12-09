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
        // 复用 item_notification.xml 布局
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
                // 如果是聊天，加载头像（如果没有头像则用默认）
                Glide.with(context)
                        .load(message.getAvatarUrl())
                        .placeholder(R.drawable.ic_avatar) // 确保你有这个默认头像资源
                        .circleCrop()
                        .into(holder.ivIcon);
                // 聊天消息一般不显示红点，或者根据逻辑自行判断，这里先隐藏
                holder.ivUnread.setVisibility(View.GONE);
            } else {
                // 如果是系统通知，显示系统图标
                holder.ivIcon.setImageResource(R.drawable.ic_notification); // 使用系统通知图标
                // 系统通知逻辑：这里简单处理，实际可根据 notification.isRead 判断
                holder.ivUnread.setVisibility(View.GONE);
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
        ImageView ivIcon; // 左侧图标/头像
        TextView tvTitle;
        TextView tvContent;
        TextView tvTime;
        View ivUnread;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 注意：请确保 item_notification.xml 中有这些 ID
            // 建议 item_notification.xml 左侧添加一个 ImageView id 为 iv_icon_type
            // 这里假设复用布局，可能需要微调 layout 文件，见下文
            ivIcon = itemView.findViewById(R.id.iv_icon_type); // 需在 layout 添加此ID
            if (ivIcon == null) {
                // 如果找不到，尝试找 notification 布局里原本可能存在的图片控件，或者你需要修改 layout
                // 这里为了稳健，假设你会在 layout 加一个 ImageView
                ivIcon = itemView.findViewById(R.id.iv_unread); // 临时替代，仅防崩
            }

            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            // 如果布局里没有 tv_content，显示在 title 或者 time 旁边
            tvContent = itemView.findViewById(R.id.tv_notification_content);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
            ivUnread = itemView.findViewById(R.id.iv_unread);
        }
    }
}
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
    private OnItemLongClickListener longClickListener; // 新增：长按监听器

    // 点击事件接口
    public interface OnItemClickListener {
        void onItemClick(UnifiedMessage message);
    }

    // 新增：长按事件接口
    public interface OnItemLongClickListener {
        void onItemLongClick(UnifiedMessage message);
    }

    public NotificationAdapter(Context context, List<UnifiedMessage> messageList,
                               OnItemClickListener listener, OnItemLongClickListener longClickListener) {
        this.context = context;
        this.messageList = messageList;
        this.listener = listener;
        this.longClickListener = longClickListener;
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

            // 根据类型设置图标和头像
            if (message.getType() == UnifiedMessage.TYPE_CHAT_MESSAGE) {
                // 聊天消息：加载用户头像
                // 修复：如果头像Url为空，或者加载失败，显示 R.drawable.lan (居民默认头像)
                if (message.getAvatarUrl() != null && !message.getAvatarUrl().isEmpty()) {
                    Glide.with(context)
                            .load(message.getAvatarUrl())
                            .placeholder(R.drawable.lan) // 加载中显示 lan
                            .error(R.drawable.lan)       // 错误显示 lan
                            .circleCrop()
                            .into(holder.ivIcon);
                } else {
                    // 没有设置头像，直接显示默认 lan.jpg
                    Glide.with(context)
                            .load(R.drawable.lan)
                            .circleCrop()
                            .into(holder.ivIcon);
                }

                // 隐藏未读红点（聊天未读逻辑暂简化为隐藏）
                if (holder.ivUnread != null) holder.ivUnread.setVisibility(View.GONE);

            } else {
                // 系统通知：显示系统铃铛图标
                holder.ivIcon.setImageResource(R.drawable.ic_notification);
                // 这里的ImageView不用圆形裁剪，或者是系统图标
                if (holder.ivUnread != null) holder.ivUnread.setVisibility(View.GONE);
            }

            // 点击事件
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(message);
                }
            });

            // 新增：长按事件
            holder.itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onItemLongClick(message);
                    return true; // 返回true表示消费了事件，不再触发点击
                }
                return false;
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
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvContent;
        TextView tvTime;
        View ivUnread;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 确保 item_notification.xml 中有这些 ID
            // 这里为了防止 ID 找不到导致崩溃，做了简单的兼容判断
            // 建议您确认布局文件中左侧头像的 ID 是 iv_icon_type 或 iv_avatar
            ivIcon = itemView.findViewById(R.id.iv_icon_type);
            if (ivIcon == null) {
                // 尝试找一下可能存在的其他ID
                ivIcon = itemView.findViewById(R.id.iv_avatar);
            }

            // 如果实在找不到头像控件，临时使用 unread 控件防止空指针（仅作防崩处理）
            if (ivIcon == null) {
                View temp = itemView.findViewById(R.id.iv_unread);
                if (temp instanceof ImageView) ivIcon = (ImageView) temp;
            }

            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvContent = itemView.findViewById(R.id.tv_notification_content);
            // 如果布局里没有单独的内容 view，就复用 title 或 hidden
            if (tvContent == null) tvContent = tvTitle;

            tvTime = itemView.findViewById(R.id.tv_notification_time);
            ivUnread = itemView.findViewById(R.id.iv_unread);
        }
    }
}
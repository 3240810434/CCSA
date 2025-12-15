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
import com.gxuwz.ccsa.model.ChatMessage;
import com.gxuwz.ccsa.util.DateUtils;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_LEFT = 0;
    private static final int TYPE_RIGHT = 1;

    private Context context;
    private List<ChatMessage> messageList;

    // 当前登录用户的信息
    private int myId;
    private String myRole;
    private String myAvatar;

    // 聊天对象的信息
    private String targetAvatar;

    public ChatAdapter(Context context, List<ChatMessage> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    // 设置我的信息和对方的信息，用于区分左右和显示头像
    public void setUserInfo(int myId, String myRole, String myAvatar, String targetAvatar) {
        this.myId = myId;
        this.myRole = myRole;
        this.myAvatar = myAvatar;
        this.targetAvatar = targetAvatar;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messageList.get(position);
        // 如果发送者ID和角色都匹配，那就是我发的 -> 右边
        if (msg.senderId == myId && msg.senderRole != null && msg.senderRole.equals(myRole)) {
            return TYPE_RIGHT;
        }
        return TYPE_LEFT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_right, parent, false);
            return new RightViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_left, parent, false);
            return new LeftViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messageList.get(position);

        if (holder instanceof RightViewHolder) {
            // --- 右边：我发的消息 ---
            RightViewHolder rightHolder = (RightViewHolder) holder;
            rightHolder.tvContent.setText(msg.content);
            rightHolder.tvTime.setText(DateUtils.formatTime(msg.createTime));

            // 【修复点2】判断角色是否为 ADMIN
            if ("ADMIN".equals(myRole)) {
                // 管理员：加载本地 drawable 资源
                Glide.with(context)
                        .load(R.drawable.admin) // 确保你有 admin.jpg 或 admin.png
                        .placeholder(R.drawable.ic_avatar)
                        .circleCrop()
                        .into(rightHolder.ivAvatar);
            } else {
                // 普通用户/商家：加载网络头像或路径
                Glide.with(context)
                        .load(myAvatar)
                        .placeholder(R.drawable.ic_avatar)
                        .circleCrop()
                        .into(rightHolder.ivAvatar);
            }

        } else if (holder instanceof LeftViewHolder) {
            // --- 左边：对方发的消息 ---
            LeftViewHolder leftHolder = (LeftViewHolder) holder;
            leftHolder.tvContent.setText(msg.content);
            leftHolder.tvTime.setText(DateUtils.formatTime(msg.createTime));

            // 如果对方是管理员（虽然当前场景不太可能，但为了健壮性可以加判断）
            if (msg.senderRole != null && "ADMIN".equals(msg.senderRole)) {
                Glide.with(context)
                        .load(R.drawable.admin)
                        .placeholder(R.drawable.ic_avatar)
                        .circleCrop()
                        .into(leftHolder.ivAvatar);
            } else {
                // 普通用户/商家
                Glide.with(context)
                        .load(targetAvatar)
                        .placeholder(R.drawable.ic_avatar)
                        .circleCrop()
                        .into(leftHolder.ivAvatar);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class LeftViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvContent;
        ImageView ivAvatar;

        public LeftViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvContent = itemView.findViewById(R.id.tv_content);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }

    static class RightViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvContent;
        ImageView ivAvatar;

        public RightViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvContent = itemView.findViewById(R.id.tv_content);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }
}
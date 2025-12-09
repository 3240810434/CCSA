package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.ui.resident.ChatActivity;
import com.gxuwz.ccsa.util.DateUtils;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private Context context;
    private List<ChatMessage> conversationList;
    private User currentUser;

    public MessageListAdapter(Context context, List<ChatMessage> conversationList, User currentUser) {
        this.context = context;
        this.conversationList = conversationList;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 使用 item_message_list.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_message_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage msg = conversationList.get(position);

        // 显示对方的头像和名字（这些字段在 Activity 中通过 loadConversations 填充了）
        holder.tvName.setText(msg.targetName != null ? msg.targetName : "未知用户");
        holder.tvContent.setText(msg.content);
        holder.tvTime.setText(DateUtils.formatTime(msg.createTime));

        Glide.with(context)
                .load(msg.targetAvatar)
                .placeholder(R.drawable.ic_avatar)
                .circleCrop()
                .into(holder.ivAvatar);

        // 点击进入聊天页面
        holder.itemView.setOnClickListener(v -> {
            int targetId = (msg.senderId == currentUser.getId()) ? msg.receiverId : msg.senderId;
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("targetUserId", targetId);
            intent.putExtra("currentUser", currentUser);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvContent, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}

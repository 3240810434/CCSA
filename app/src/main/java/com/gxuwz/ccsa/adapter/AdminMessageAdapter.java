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
import com.gxuwz.ccsa.model.Admin;
import com.gxuwz.ccsa.model.ChatMessage;
import com.gxuwz.ccsa.ui.resident.ChatActivity;
import com.gxuwz.ccsa.util.DateUtils;

import java.util.List;

public class AdminMessageAdapter extends RecyclerView.Adapter<AdminMessageAdapter.ViewHolder> {

    private Context context;
    private List<ChatMessage> conversationList;
    private Admin currentAdmin;

    public AdminMessageAdapter(Context context, List<ChatMessage> conversationList, Admin currentAdmin) {
        this.context = context;
        this.conversationList = conversationList;
        this.currentAdmin = currentAdmin;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage msg = conversationList.get(position);

        holder.tvName.setText(msg.targetName != null ? msg.targetName : "未知用户");
        holder.tvContent.setText(msg.content);
        holder.tvTime.setText(DateUtils.formatTime(msg.createTime));

        Glide.with(context)
                .load(msg.targetAvatar)
                .placeholder(R.drawable.ic_avatar)
                .circleCrop()
                .into(holder.ivAvatar);

        holder.itemView.setOnClickListener(v -> {
            // 计算聊天对象的ID和角色
            int targetId;
            String targetRole;

            // 如果我是发送者(ADMIN)，那对方就是Receiver
            if (msg.senderId == currentAdmin.getId() && "ADMIN".equals(msg.senderRole)) {
                targetId = msg.receiverId;
                targetRole = msg.receiverRole;
            } else {
                targetId = msg.senderId;
                targetRole = msg.senderRole;
            }

            Intent intent = new Intent(context, ChatActivity.class);
            // 我是管理员
            intent.putExtra("myId", currentAdmin.getId());
            intent.putExtra("myRole", "ADMIN");

            // 对方信息
            intent.putExtra("targetId", targetId);
            intent.putExtra("targetRole", targetRole);
            intent.putExtra("targetName", msg.targetName);
            intent.putExtra("targetAvatar", msg.targetAvatar);

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

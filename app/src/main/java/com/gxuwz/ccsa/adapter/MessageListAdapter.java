package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_message_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage msg = conversationList.get(position);

        // --- 【修复步骤 1】先准确计算出对方的角色 (targetRole) ---
        // 逻辑与 onClick 中保持一致，确保显示和点击跳转的逻辑相同
        int targetId;
        String targetRole;

        if (msg.senderId == currentUser.getId() && "RESIDENT".equalsIgnoreCase(msg.senderRole)) {
            // 我是发送者，对方是接收者
            targetId = msg.receiverId;
            targetRole = msg.receiverRole;
        } else {
            // 我是接收者，对方是发送者
            targetId = msg.senderId;
            targetRole = msg.senderRole;
        }

        // --- 【修复步骤 2】基于 targetRole 判断是否为管理员 ---
        // 只要角色包含 ADMIN，或者名字已经被 Activity 强制修正为 "管理员"，都算作管理员
        boolean isAdmin = (targetRole != null && targetRole.toUpperCase().contains("ADMIN")) ||
                "管理员".equals(msg.targetName) ||
                "local_admin_resource".equals(msg.targetAvatar);

        // --- 【修复步骤 3】设置 UI ---
        holder.tvTime.setText(DateUtils.formatTime(msg.createTime));
        holder.tvContent.setText(msg.content);

        if (isAdmin) {
            // 强制显示管理员信息
            holder.tvName.setText("管理员");
            holder.ivAvatar.setImageResource(R.drawable.admin); // 确保你的 drawable 文件夹里有 admin.jpg (或 .png)
        } else {
            // 普通显示逻辑
            holder.tvName.setText(TextUtils.isEmpty(msg.targetName) ? "未知用户" : msg.targetName);

            Glide.with(context)
                    .load(msg.targetAvatar)
                    .placeholder(R.drawable.ic_avatar) // 默认头像
                    .error(R.drawable.ic_avatar)
                    .circleCrop()
                    .into(holder.ivAvatar);
        }

        // --- 点击事件 ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("myId", currentUser.getId());
            intent.putExtra("myRole", "RESIDENT");

            intent.putExtra("targetId", targetId);
            intent.putExtra("targetRole", targetRole);

            if (isAdmin) {
                intent.putExtra("targetName", "管理员");
                intent.putExtra("targetAvatar", "local_admin_resource");
            } else {
                intent.putExtra("targetName", msg.targetName);
                intent.putExtra("targetAvatar", msg.targetAvatar);
            }

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
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

        // 1. 先准确计算出“对方”的ID和角色
        int targetId;
        String targetRole;

        if (msg.senderId == currentUser.getId() && "RESIDENT".equalsIgnoreCase(msg.senderRole)) {
            // 我发给对方
            targetId = msg.receiverId;
            targetRole = msg.receiverRole;
        } else {
            // 对方发给我
            targetId = msg.senderId;
            targetRole = msg.senderRole;
        }

        // 2. 判断是否为管理员
        boolean isAdmin = false;

        // 判定条件 A: 对方角色字段包含 ADMIN
        if (targetRole != null && (targetRole.toUpperCase().contains("ADMIN") || targetRole.toUpperCase().contains("SYSTEM"))) {
            isAdmin = true;
        }
        // 判定条件 B: Activity 层面已经强制修正了名字或头像标记
        else if ("管理员".equals(msg.targetName) || "local_admin_resource".equals(msg.targetAvatar)) {
            isAdmin = true;
        }

        // 3. 设置 UI 显示
        holder.tvTime.setText(DateUtils.formatTime(msg.createTime));
        holder.tvContent.setText(msg.content);

        if (isAdmin) {
            holder.tvName.setText("管理员");
            holder.ivAvatar.setImageResource(R.drawable.admin);
        } else {
            String displayName = TextUtils.isEmpty(msg.targetName) ? "未知用户" : msg.targetName;
            holder.tvName.setText(displayName);

            Glide.with(context)
                    .load(msg.targetAvatar)
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .circleCrop()
                    .into(holder.ivAvatar);
        }

        // --- 【Bug修复核心】 ---
        // 为了满足 Lambda 表达式的 final 要求，创建 final 副本变量
        final boolean finalIsAdmin = isAdmin;
        final int finalTargetId = targetId;
        final String finalTargetRole = targetRole;

        // 4. 点击跳转
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("myId", currentUser.getId());
            intent.putExtra("myRole", "RESIDENT");

            // 使用 final 变量
            intent.putExtra("targetId", finalTargetId);
            intent.putExtra("targetRole", finalTargetRole);

            if (finalIsAdmin) {
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
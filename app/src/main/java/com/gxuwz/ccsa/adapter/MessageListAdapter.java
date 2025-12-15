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

        // 1. 计算对方的ID和角色
        int targetId;
        String targetRole;

        if (msg.senderId == currentUser.getId() && "RESIDENT".equalsIgnoreCase(msg.senderRole)) {
            targetId = msg.receiverId;
            targetRole = msg.receiverRole;
        } else {
            targetId = msg.senderId;
            targetRole = msg.senderRole;
        }

        // 2. 强力判断是否为管理员
        boolean isAdmin = false;

        // 条件A: Activity 已经处理过，标记为 "管理员" 或头像为 "local_admin_resource"
        if ("管理员".equals(msg.targetName) || "local_admin_resource".equals(msg.targetAvatar)) {
            isAdmin = true;
        }
        // 条件B: 检查 ID 是否命中管理员 ID 列表 (包含 1, 11, 111 等常见模式)
        else if (targetId == 1 || targetId == 11 || targetId == 111 ||
                targetId == 1111 || targetId == 11111 || targetId == 111111) {
            isAdmin = true;
        }
        // 条件C: 角色字段包含 ADMIN
        else if (targetRole != null && (targetRole.toUpperCase().contains("ADMIN") || targetRole.toUpperCase().contains("SYSTEM"))) {
            isAdmin = true;
        }

        // 3. 设置 UI
        holder.tvTime.setText(DateUtils.formatTime(msg.createTime));
        holder.tvContent.setText(msg.content);

        if (isAdmin) {
            holder.tvName.setText("管理员");
            // 确保 res/drawable 下有 admin.jpg
            holder.ivAvatar.setImageResource(R.drawable.admin);
        } else {
            // 如果不是管理员，正常显示
            String displayName = TextUtils.isEmpty(msg.targetName) ? "未知用户" : msg.targetName;
            holder.tvName.setText(displayName);

            Glide.with(context)
                    .load(msg.targetAvatar)
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .circleCrop()
                    .into(holder.ivAvatar);
        }

        // 4. 准备跳转参数
        final boolean finalIsAdmin = isAdmin;
        final int finalTargetId = targetId;
        final String finalTargetRole = targetRole;

        // 5. 点击跳转
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("myId", currentUser.getId());
            intent.putExtra("myRole", "RESIDENT");

            intent.putExtra("targetId", finalTargetId);
            // 如果判定是管理员，强制传 ADMIN
            intent.putExtra("targetRole", finalIsAdmin ? "ADMIN" : finalTargetRole);

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
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_message_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage msg = conversationList.get(position);

        // 显示名字
        holder.tvName.setText(msg.targetName != null ? msg.targetName : "未知用户");
        holder.tvContent.setText(msg.content);
        holder.tvTime.setText(DateUtils.formatTime(msg.createTime));

        // --- 【强制显示管理员头像】 ---
        // 只要名字是“管理员” 或者 头像标记是我们Activity里设定的那个特殊字符串
        // 或者是消息角色包含 ADMIN (作为双重保险)
        boolean isAdmin = "管理员".equals(msg.targetName) ||
                "local_admin_resource".equals(msg.targetAvatar) ||
                (msg.senderRole != null && msg.senderRole.toUpperCase().contains("ADMIN") && msg.senderId != currentUser.getId());

        if (isAdmin) {
            // 强制加载本地 drawable 资源
            holder.ivAvatar.setImageResource(R.drawable.admin);
        } else {
            // 普通用户或商家，使用 Glide 加载网络/本地路径图片
            Glide.with(context)
                    .load(msg.targetAvatar)
                    .placeholder(R.drawable.ic_avatar) // 默认头像
                    .error(R.drawable.ic_avatar)       // 加载失败显示默认头像
                    .circleCrop()
                    .into(holder.ivAvatar);
        }

        holder.itemView.setOnClickListener(v -> {
            // 点击跳转逻辑
            int targetId;
            String targetRole;

            if (msg.senderId == currentUser.getId() && "RESIDENT".equalsIgnoreCase(msg.senderRole)) {
                targetId = msg.receiverId;
                targetRole = msg.receiverRole;
            } else {
                targetId = msg.senderId;
                targetRole = msg.senderRole;
            }

            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("myId", currentUser.getId());
            intent.putExtra("myRole", "RESIDENT");

            intent.putExtra("targetId", targetId);
            intent.putExtra("targetRole", targetRole);

            // 将强制修正后的名字和头像传给聊天页面，确保聊天页面标题也显示“管理员”
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
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

        holder.tvName.setText(msg.targetName != null ? msg.targetName : "未知用户");
        holder.tvContent.setText(msg.content);
        holder.tvTime.setText(DateUtils.formatTime(msg.createTime));

        // 【修复部分】：判断是否为管理员，显示特定头像
        if ("管理员".equals(msg.targetName)) {
            // 直接加载资源文件中的 admin.jpg
            holder.ivAvatar.setImageResource(R.drawable.admin);
        } else {
            // 其他用户使用 Glide 加载 URL
            Glide.with(context)
                    .load(msg.targetAvatar)
                    .placeholder(R.drawable.ic_avatar)
                    .circleCrop()
                    .into(holder.ivAvatar);
        }

        holder.itemView.setOnClickListener(v -> {
            // 重新计算目标ID和角色
            int targetId;
            String targetRole;

            if (msg.senderId == currentUser.getId() && "RESIDENT".equals(msg.senderRole)) {
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
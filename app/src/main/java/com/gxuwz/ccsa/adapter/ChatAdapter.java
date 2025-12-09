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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.ChatMessage;
import com.gxuwz.ccsa.model.User;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<ChatMessage> list;
    private User currentUser;
    private User targetUser; // 聊天对象

    private static final int TYPE_SEND = 1;
    private static final int TYPE_RECEIVE = 2;

    public ChatAdapter(Context context, List<ChatMessage> list, User currentUser, User targetUser) {
        this.context = context;
        this.list = list;
        this.currentUser = currentUser;
        this.targetUser = targetUser;
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).senderId == currentUser.getId()) {
            return TYPE_SEND;
        } else {
            return TYPE_RECEIVE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_right, parent, false);
            return new SendHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_left, parent, false);
            return new ReceiveHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = list.get(position);
        if (holder instanceof SendHolder) {
            ((SendHolder) holder).tvContent.setText(msg.content);
            Glide.with(context).load(currentUser.getAvatar()).placeholder(R.drawable.lan)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(100)))
                    .into(((SendHolder) holder).ivAvatar);
        } else if (holder instanceof ReceiveHolder) {
            ((ReceiveHolder) holder).tvContent.setText(msg.content);
            Glide.with(context).load(targetUser.getAvatar()).placeholder(R.drawable.lan)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(100)))
                    .into(((ReceiveHolder) holder).ivAvatar);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class SendHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        ImageView ivAvatar;
        SendHolder(View view) {
            super(view);
            tvContent = view.findViewById(R.id.tv_content);
            ivAvatar = view.findViewById(R.id.iv_avatar);
        }
    }

    static class ReceiveHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        ImageView ivAvatar;
        ReceiveHolder(View view) {
            super(view);
            tvContent = view.findViewById(R.id.tv_content);
            ivAvatar = view.findViewById(R.id.iv_avatar);
        }
    }
}

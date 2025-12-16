package com.gxuwz.ccsa.ui.merchant;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.ChatMessage;
import com.gxuwz.ccsa.model.Merchant;
import com.gxuwz.ccsa.model.Notification;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.ui.resident.ChatActivity;
import com.gxuwz.ccsa.util.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MerchantMessageFragment extends Fragment {

    private RecyclerView recyclerView;
    private MerchantChatAdapter adapter;
    private List<ChatMessage> conversationList = new ArrayList<>();
    private AppDatabase db;
    private int merchantId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_merchant_message, container, false);

        db = AppDatabase.getInstance(getContext());

        // 获取当前商家ID
        SharedPreferences sp = getContext().getSharedPreferences("merchant_prefs", Context.MODE_PRIVATE);
        merchantId = sp.getInt("merchant_id", -1);

        recyclerView = view.findViewById(R.id.recycler_view);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new MerchantChatAdapter(getContext(), conversationList, merchantId);
            recyclerView.setAdapter(adapter);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadConversations();
    }

    private void loadConversations() {
        if (merchantId == -1) return;

        new Thread(() -> {
            // 1. 查询聊天消息
            List<ChatMessage> allMsgs = db.chatDao().getAllMyMessages(merchantId, "MERCHANT");
            Map<String, ChatMessage> latestMsgMap = new HashMap<>();

            for (ChatMessage msg : allMsgs) {
                int otherId;
                String otherRole;

                if (msg.senderId == merchantId && "MERCHANT".equals(msg.senderRole)) {
                    otherId = msg.receiverId;
                    otherRole = msg.receiverRole;
                } else {
                    otherId = msg.senderId;
                    otherRole = msg.senderRole;
                }

                String key = otherRole + "_" + otherId;

                if (!latestMsgMap.containsKey(key)) {
                    if ("RESIDENT".equals(otherRole)) {
                        User u = db.userDao().findById(otherId);
                        msg.targetName = (u != null) ? u.getName() : "居民";
                        msg.targetAvatar = (u != null) ? u.getAvatar() : "";
                    } else {
                        msg.targetName = "未知用户";
                    }
                    latestMsgMap.put(key, msg);
                }
            }

            List<ChatMessage> finalList = new ArrayList<>(latestMsgMap.values());

            // 2. 查询系统通知
            Merchant me = db.merchantDao().findById(merchantId);
            if (me != null) {
                // 确保 NotificationDao 中有 findByRecipientPhone 方法
                List<Notification> notices = db.notificationDao().findByRecipientPhone(me.getPhone());
                if (notices != null && !notices.isEmpty()) {
                    Notification latestNotice = notices.get(0);
                    ChatMessage sysMsg = new ChatMessage();
                    sysMsg.targetName = "系统通知";
                    sysMsg.content = latestNotice.getTitle();

                    // 【修复点 1】类型转换：Date -> long
                    if (latestNotice.getCreateTime() != null) {
                        sysMsg.createTime = latestNotice.getCreateTime().getTime();
                    } else {
                        sysMsg.createTime = System.currentTimeMillis();
                    }

                    sysMsg.targetAvatar = "";
                    sysMsg.senderRole = "SYSTEM";
                    finalList.add(sysMsg);
                }
            }

            // 【修复点 2】修复 long 类型比较报错
            // ChatMessage.createTime 是 long 类型，不能与 null 比较，也不能直接调用 compareTo
            Collections.sort(finalList, (o1, o2) -> Long.compare(o2.createTime, o1.createTime));

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    conversationList.clear();
                    conversationList.addAll(finalList);
                    if (adapter != null) adapter.notifyDataSetChanged();
                });
            }
        }).start();
    }

    public static class MerchantChatAdapter extends RecyclerView.Adapter<MerchantChatAdapter.ViewHolder> {
        private Context context;
        private List<ChatMessage> list;
        private int myMerchantId;

        public MerchantChatAdapter(Context context, List<ChatMessage> list, int myMerchantId) {
            this.context = context;
            this.list = list;
            this.myMerchantId = myMerchantId;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChatMessage msg = list.get(position);

            if (holder.tvName != null) holder.tvName.setText(msg.targetName);
            if (holder.tvContent != null) holder.tvContent.setText(msg.content);
            if (holder.tvTime != null) holder.tvTime.setText(DateUtils.formatTime(msg.createTime));

            if ("系统通知".equals(msg.targetName) || "SYSTEM".equals(msg.senderRole)) {
                holder.ivAvatar.setImageResource(R.drawable.ic_notification);
                holder.itemView.setOnClickListener(v -> {
                    new AlertDialog.Builder(context)
                            .setTitle("最新系统通知")
                            .setMessage(msg.content)
                            .setPositiveButton("知道了", null)
                            .show();
                });
            } else {
                Glide.with(context)
                        .load(msg.targetAvatar)
                        .placeholder(R.drawable.ic_avatar)
                        .circleCrop()
                        .into(holder.ivAvatar);

                holder.itemView.setOnClickListener(v -> {
                    int targetId;
                    String targetRole;
                    if (msg.senderId == myMerchantId && "MERCHANT".equals(msg.senderRole)) {
                        targetId = msg.receiverId;
                        targetRole = msg.receiverRole;
                    } else {
                        targetId = msg.senderId;
                        targetRole = msg.senderRole;
                    }
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("myId", myMerchantId);
                    intent.putExtra("myRole", "MERCHANT");
                    intent.putExtra("targetId", targetId);
                    intent.putExtra("targetRole", targetRole);
                    intent.putExtra("targetName", msg.targetName);
                    intent.putExtra("targetAvatar", msg.targetAvatar);
                    context.startActivity(intent);
                });
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivAvatar;
            TextView tvName, tvContent, tvTime;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivAvatar = itemView.findViewById(R.id.iv_avatar);
                tvName = itemView.findViewById(R.id.tv_name);
                tvContent = itemView.findViewById(R.id.tv_last_msg);
                tvTime = itemView.findViewById(R.id.tv_time);
            }
        }
    }
}
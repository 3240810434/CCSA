package com.gxuwz.ccsa.ui.merchant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.ChatMessage;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.ui.resident.ChatActivity;
import com.gxuwz.ccsa.util.DateUtils;

import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
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
        // 从 SharedPreferences 获取当前商家ID
        SharedPreferences sp = getContext().getSharedPreferences("merchant_prefs", Context.MODE_PRIVATE);
        merchantId = sp.getInt("merchant_id", -1);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new MerchantChatAdapter(getContext(), conversationList, merchantId);
        recyclerView.setAdapter(adapter);

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
            // 查询所有与我(MERCHANT)相关的消息
            List<ChatMessage> allMsgs = db.chatDao().getAllMyMessages(merchantId, "MERCHANT");
            Map<String, ChatMessage> latestMsgMap = new HashMap<>();

            for (ChatMessage msg : allMsgs) {
                int otherId;
                String otherRole;

                // 判断对方是谁
                if (msg.senderId == merchantId && "MERCHANT".equals(msg.senderRole)) {
                    otherId = msg.receiverId;
                    otherRole = msg.receiverRole;
                } else {
                    otherId = msg.senderId;
                    otherRole = msg.senderRole;
                }

                String key = otherRole + "_" + otherId;

                if (!latestMsgMap.containsKey(key)) {
                    // 查询居民信息
                    if ("RESIDENT".equals(otherRole)) {
                        User u = db.userDao().findById(otherId);
                        msg.targetName = (u != null) ? u.getName() : "居民";
                        msg.targetAvatar = (u != null) ? u.getAvatar() : "";
                    } else {
                        // 商家互聊等情况（如有）
                        msg.targetName = "未知用户";
                    }
                    latestMsgMap.put(key, msg);
                }
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    conversationList.clear();
                    conversationList.addAll(latestMsgMap.values());
                    adapter.notifyDataSetChanged();
                });
            }
        }).start();
    }

    // 内部类：专用 Adapter
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
            holder.tvName.setText(msg.targetName);
            holder.tvContent.setText(msg.content);
            holder.tvTime.setText(DateUtils.formatTime(msg.createTime));

            Glide.with(context)
                    .load(msg.targetAvatar)
                    .placeholder(R.drawable.ic_avatar)
                    .circleCrop()
                    .into(holder.ivAvatar);

            holder.itemView.setOnClickListener(v -> {
                // 计算目标ID（通常是居民）
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
                // 我是商家
                intent.putExtra("myId", myMerchantId);
                intent.putExtra("myRole", "MERCHANT");

                // 对方是居民
                intent.putExtra("targetId", targetId);
                intent.putExtra("targetRole", targetRole);

                intent.putExtra("targetName", msg.targetName);
                intent.putExtra("targetAvatar", msg.targetAvatar);

                context.startActivity(intent);
            });
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
                tvContent = itemView.findViewById(R.id.tv_content);
                tvTime = itemView.findViewById(R.id.tv_time);
            }
        }
    }
}
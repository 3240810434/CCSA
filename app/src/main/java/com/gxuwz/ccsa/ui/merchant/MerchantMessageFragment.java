package com.gxuwz.ccsa.ui.merchant;

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
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.ui.resident.ChatActivity;
import com.gxuwz.ccsa.util.DateUtils;

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

        // 获取当前商家ID
        SharedPreferences sp = getContext().getSharedPreferences("merchant_prefs", Context.MODE_PRIVATE);
        // 【注意】这里使用 getInt，必须配合 LoginActivity 中的 putInt
        merchantId = sp.getInt("merchant_id", -1);

        recyclerView = view.findViewById(R.id.recycler_view);
        // 确保你的 fragment_merchant_message.xml 中有一个 ID 为 recycler_view 的 RecyclerView
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
        // 每次页面显示时刷新数据
        loadConversations();
    }

    private void loadConversations() {
        if (merchantId == -1) return;

        new Thread(() -> {
            // 查询所有与我(MERCHANT)相关的消息
            // 确保 ChatDao 中有 getAllMyMessages(int id, String role) 方法
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

                // 组合Key，防止ID重复
                String key = otherRole + "_" + otherId;

                if (!latestMsgMap.containsKey(key)) {
                    // 如果对方是居民，查询居民信息
                    if ("RESIDENT".equals(otherRole)) {
                        User u = db.userDao().findById(otherId); // 确保 UserDao 有 findById
                        msg.targetName = (u != null) ? u.getName() : "居民";
                        msg.targetAvatar = (u != null) ? u.getAvatar() : "";
                    } else {
                        msg.targetName = "未知用户";
                    }
                    latestMsgMap.put(key, msg);
                }
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    conversationList.clear();
                    conversationList.addAll(latestMsgMap.values());
                    if (adapter != null) adapter.notifyDataSetChanged();
                });
            }
        }).start();
    }

    // 内部类 Adapter
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

            Glide.with(context).load(msg.targetAvatar).placeholder(R.drawable.ic_avatar).circleCrop().into(holder.ivAvatar);

            holder.itemView.setOnClickListener(v -> {
                int targetId;
                String targetRole;

                // 点击跳转到聊天页面
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
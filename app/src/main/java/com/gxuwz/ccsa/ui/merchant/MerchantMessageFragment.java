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
import android.widget.Toast;

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

        // 1. 获取当前商家ID
        SharedPreferences sp = getContext().getSharedPreferences("merchant_prefs", Context.MODE_PRIVATE);
        // 注意：这里使用 getInt，请确保你在 MerchantLoginActivity 中是使用 putInt 保存的 merchant_id
        merchantId = sp.getInt("merchant_id", -1);

        if (merchantId == -1) {
            Toast.makeText(getContext(), "商家登录状态异常", Toast.LENGTH_SHORT).show();
        }

        // 2. 初始化 RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new MerchantChatAdapter(getContext(), conversationList, merchantId);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次回到页面时刷新数据
        loadConversations();
    }

    private void loadConversations() {
        if (merchantId == -1) return;

        new Thread(() -> {
            // 3. 查询数据库：查找所有涉及该商家的消息（角色为 MERCHANT）
            // 确保 ChatDao 中有 getAllMyMessages(int id, String role) 方法
            List<ChatMessage> allMsgs = db.chatDao().getAllMyMessages(merchantId, "MERCHANT");

            // 使用 Map 去重，只保留与每个用户的最新一条消息
            Map<String, ChatMessage> latestMsgMap = new HashMap<>();

            for (ChatMessage msg : allMsgs) {
                int otherId;
                String otherRole;

                // 判断对方是谁
                // 如果我是发送者(且我是商家)，那对方就是接收者
                if (msg.senderId == merchantId && "MERCHANT".equals(msg.senderRole)) {
                    otherId = msg.receiverId;
                    otherRole = msg.receiverRole;
                } else {
                    // 如果我是接收者，对方就是发送者
                    otherId = msg.senderId;
                    otherRole = msg.senderRole;
                }

                // 组合Key (Role + ID) 防止不同角色的ID冲突
                String key = otherRole + "_" + otherId;

                // 因为查询结果是按时间倒序的，所以第一次遇到的 key 就是最新的消息
                if (!latestMsgMap.containsKey(key)) {
                    // 查询对方（通常是居民 User）的详细信息，用于显示头像和名字
                    if ("RESIDENT".equals(otherRole)) {
                        User u = db.userDao().findById(otherId); // 确保 UserDao 有 findById 方法
                        msg.targetName = (u != null) ? u.getName() : "居民";
                        msg.targetAvatar = (u != null) ? u.getAvatar() : "";
                    } else {
                        msg.targetName = "未知用户";
                    }
                    latestMsgMap.put(key, msg);
                }
            }

            // 4. 回到主线程更新 UI
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    conversationList.clear();
                    conversationList.addAll(latestMsgMap.values());
                    adapter.notifyDataSetChanged();
                });
            }
        }).start();
    }

    // --- 内部类：适配器 ---
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
            // 复用 item_message_list 布局
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChatMessage msg = list.get(position);
            holder.tvName.setText(msg.targetName);
            holder.tvContent.setText(msg.content);
            holder.tvTime.setText(DateUtils.formatTime(msg.createTime)); //

            Glide.with(context)
                    .load(msg.targetAvatar)
                    .placeholder(R.drawable.ic_avatar) // 确保有默认头像资源
                    .circleCrop()
                    .into(holder.ivAvatar);

            // 点击条目跳转到聊天详情页
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

                Intent intent = new Intent(context, ChatActivity.class); //
                intent.putExtra("myId", myMerchantId);
                intent.putExtra("myRole", "MERCHANT"); // 这里的身份是商家
                intent.putExtra("targetId", targetId);
                intent.putExtra("targetRole", targetRole); // 对方身份通常是 RESIDENT
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
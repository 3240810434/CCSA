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
import java.util.Comparator;
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
        // 每次页面显示时刷新数据
        loadConversations();
    }

    private void loadConversations() {
        if (merchantId == -1) return;

        new Thread(() -> {
            // 1. 查询所有与我(MERCHANT)相关的聊天消息
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
                        User u = db.userDao().findById(otherId);
                        msg.targetName = (u != null) ? u.getName() : "居民";
                        msg.targetAvatar = (u != null) ? u.getAvatar() : "";
                    } else {
                        msg.targetName = "未知用户";
                    }
                    latestMsgMap.put(key, msg);
                }
            }

            // 暂存聊天列表
            List<ChatMessage> finalList = new ArrayList<>(latestMsgMap.values());

            // 2. 【新增】查询系统通知
            // 首先获取商家的手机号，因为 Notification 表是用手机号关联的
            Merchant me = db.merchantDao().findById(merchantId);
            if (me != null) {
                // 使用在 NotificationDao 中新增的方法 findByRecipientPhone
                List<Notification> notices = db.notificationDao().findByRecipientPhone(me.getPhone());
                if (notices != null && !notices.isEmpty()) {
                    // 取最新的一条通知作为展示
                    Notification latestNotice = notices.get(0);
                    ChatMessage sysMsg = new ChatMessage();
                    sysMsg.targetName = "系统通知"; // 特殊标记名称
                    sysMsg.content = latestNotice.getTitle(); // 显示标题
                    sysMsg.createTime = latestNotice.getCreateTime();
                    // 可以设置一个系统头像资源ID，在 Adapter 里处理，或者这里给空字符串
                    sysMsg.targetAvatar = "";
                    sysMsg.senderRole = "SYSTEM"; // 标记为系统角色

                    // 将系统通知加入列表
                    finalList.add(sysMsg);
                }
            }

            // 3. 按时间倒序排序
            Collections.sort(finalList, (o1, o2) -> {
                if(o1.createTime == null || o2.createTime == null) return 0;
                return o2.createTime.compareTo(o1.createTime);
            });

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    conversationList.clear();
                    conversationList.addAll(finalList);
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

            // 设置昵称
            if (holder.tvName != null) {
                holder.tvName.setText(msg.targetName);
            }

            // 设置内容
            if (holder.tvContent != null) {
                holder.tvContent.setText(msg.content);
            }

            // 设置时间
            if (holder.tvTime != null) {
                holder.tvTime.setText(DateUtils.formatTime(msg.createTime));
            }

            // 【新增】处理系统通知的头像和点击事件
            if ("系统通知".equals(msg.targetName) || "SYSTEM".equals(msg.senderRole)) {
                // 设置系统通知默认头像 (请确保 res/drawable 下有 ic_notification 图片，或者使用其他现有图片)
                holder.ivAvatar.setImageResource(R.drawable.ic_notification);

                // 点击系统通知弹出详情或跳转列表 (这里简单实现为弹窗显示最新一条，或者跳转到通知列表页)
                holder.itemView.setOnClickListener(v -> {
                    // 如果有 NotificationListActivity 应该跳转过去，这里为了演示直接弹窗显示内容
                    new AlertDialog.Builder(context)
                            .setTitle("最新系统通知")
                            .setMessage(msg.content) // 这里只显示了标题，实际开发建议跳转专门的通知列表Activity
                            .setPositiveButton("知道了", null)
                            .show();
                });
            } else {
                // 普通聊天逻辑
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
                // 确保这里ID和xml一致
                tvContent = itemView.findViewById(R.id.tv_last_msg);
                tvTime = itemView.findViewById(R.id.tv_time);
            }
        }
    }
}
package com.gxuwz.ccsa.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.AdminMessageAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Admin;
import com.gxuwz.ccsa.model.ChatMessage;
import com.gxuwz.ccsa.model.Merchant;
import com.gxuwz.ccsa.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminMessageFragment extends Fragment {

    private static final String ARG_ADMIN_ACCOUNT = "adminAccount";
    private String adminAccount;
    private RecyclerView recyclerView;
    private AdminMessageAdapter adapter;
    private List<ChatMessage> conversationList = new ArrayList<>();
    private AppDatabase db;
    private Admin currentAdmin;

    public AdminMessageFragment() {
        // Required empty public constructor
    }

    public static AdminMessageFragment newInstance(String adminAccount) {
        AdminMessageFragment fragment = new AdminMessageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ADMIN_ACCOUNT, adminAccount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            adminAccount = getArguments().getString(ARG_ADMIN_ACCOUNT);
        }
        db = AppDatabase.getInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AdminMessageAdapter(getContext(), conversationList, null);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            // 1. 获取当前登录的管理员信息
            if (currentAdmin == null) {
                currentAdmin = db.adminDao().findByAccount(adminAccount);
            }

            if (currentAdmin == null) return;

            // 2. 查询所有和该管理员(ADMIN)有关的消息
            // 这里实现了隔离：因为 ChatDao 查的是 senderId/receiverId 为 currentAdmin.getId() 的数据
            // 而只有本小区的居民才会持有这个 Admin ID 并发消息过来
            List<ChatMessage> allMsgs = db.chatDao().getAllMyMessages(currentAdmin.getId(), "ADMIN");

            // 使用 Map 进行消息去重，只保留每个会话的最新一条
            Map<String, ChatMessage> latestMsgMap = new HashMap<>();

            for (ChatMessage msg : allMsgs) {
                // 确定对方是谁 (Who is the other party?)
                int otherId;
                String otherRole;

                if (msg.senderId == currentAdmin.getId() && "ADMIN".equals(msg.senderRole)) {
                    // 我(管理员)发的，对方是接收者
                    otherId = msg.receiverId;
                    otherRole = msg.receiverRole;
                } else {
                    // 别人发的，对方是发送者
                    otherId = msg.senderId;
                    otherRole = msg.senderRole;
                }

                // 组合 Key 避免不同 Role 的 ID 冲突
                String key = otherRole + "_" + otherId;

                if (!latestMsgMap.containsKey(key)) {
                    // 3. 查询对方详细信息 (头像、名称)
                    if ("RESIDENT".equals(otherRole)) {
                        User u = db.userDao().findById(otherId);
                        if (u != null) {
                            // 可以在名字前加楼号，方便管理员识别
                            // 格式：张三 (1栋101)
                            String displayName = u.getName();
                            if (u.getBuilding() != null && u.getRoom() != null) {
                                displayName += " (" + u.getBuilding() + "-" + u.getRoom() + ")";
                            }
                            msg.targetName = displayName;
                            msg.targetAvatar = u.getAvatar();

                            // 额外的安全校验（可选）：确保居民小区和管理员一致
                            // if (!currentAdmin.getCommunity().equals(u.getCommunity())) continue;
                        } else {
                            msg.targetName = "居民(已注销)";
                            msg.targetAvatar = "";
                        }
                    } else if ("MERCHANT".equals(otherRole)) {
                        Merchant m = db.merchantDao().findById(otherId);
                        msg.targetName = (m != null) ? m.getMerchantName() : "商家(已注销)";
                        msg.targetAvatar = (m != null) ? m.getAvatar() : "";
                    } else {
                        msg.targetName = "未知用户";
                    }
                    latestMsgMap.put(key, msg);
                }
            }

            getActivity().runOnUiThread(() -> {
                conversationList.clear();
                // 将 Map 的值转为 List
                conversationList.addAll(latestMsgMap.values());

                // 更新 Adapter 的 Admin 对象和数据
                adapter = new AdminMessageAdapter(getContext(), conversationList, currentAdmin);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }
}
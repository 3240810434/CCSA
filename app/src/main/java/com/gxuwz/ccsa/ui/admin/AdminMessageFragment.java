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

        // 初始时 Admin 可能还没加载完，先不设置 Adapter 或设置空列表
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
            // 1. 获取管理员信息
            if (currentAdmin == null) {
                currentAdmin = db.adminDao().findByAccount(adminAccount);
            }

            if (currentAdmin == null) return;

            // 2. 查询所有和管理员(ADMIN)有关的消息
            List<ChatMessage> allMsgs = db.chatDao().getAllMyMessages(currentAdmin.getId(), "ADMIN");
            Map<String, ChatMessage> latestMsgMap = new HashMap<>();

            for (ChatMessage msg : allMsgs) {
                // 确定对方是谁
                int otherId;
                String otherRole;

                if (msg.senderId == currentAdmin.getId() && "ADMIN".equals(msg.senderRole)) {
                    // 我发的，对方是 Receiver
                    otherId = msg.receiverId;
                    otherRole = msg.receiverRole;
                } else {
                    // 别人发的，对方是 Sender
                    otherId = msg.senderId;
                    otherRole = msg.senderRole;
                }

                // 组合 Key 避免不同 Role 的 ID 冲突 (例如 User id=1 和 Merchant id=1)
                String key = otherRole + "_" + otherId;

                if (!latestMsgMap.containsKey(key)) {
                    // 查询对方详细信息 (头像、名称)
                    if ("RESIDENT".equals(otherRole)) {
                        User u = db.userDao().findById(otherId);
                        msg.targetName = (u != null) ? u.getName() : "居民(已注销)";
                        msg.targetAvatar = (u != null) ? u.getAvatar() : "";
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
                conversationList.addAll(latestMsgMap.values());
                // 更新 Adapter 的 Admin 对象和数据
                adapter = new AdminMessageAdapter(getContext(), conversationList, currentAdmin);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }
}
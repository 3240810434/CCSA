package com.gxuwz.ccsa.ui.resident;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.User;
// 引入消息列表页面
import com.gxuwz.ccsa.ui.resident.MessageListActivity;

public class ServiceFragment extends Fragment {

    private User currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service, container, false);

        // 获取当前登录用户 (从 Activity 获取)
        if (getActivity() instanceof ResidentMainActivity) {
            currentUser = ((ResidentMainActivity) getActivity()).getUser();
        }

        // 初始化其他功能按钮 (这里省略了其他的 findViewById)
        // ...

        // --- 核心修复：消息图标点击事件 ---
        // 假设你的布局中消息图标的 ID 是 ll_message 或者 iv_message
        // 如果是 Grid 布局的图标，请找到对应的点击位置
        View btnMessage = view.findViewById(R.id.ll_message); // 请确认 xml 中消息图标外层布局 ID
        if (btnMessage != null) {
            btnMessage.setOnClickListener(v -> {
                if (currentUser == null) {
                    Toast.makeText(getContext(), "用户信息获取失败，请重新登录", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 跳转到聊天消息列表，而不是通知公告
                Intent intent = new Intent(getActivity(), MessageListActivity.class);
                intent.putExtra("user", currentUser); // 必须传递 User 对象
                startActivity(intent);
            });
        }

        // 注意：原有的通知公告入口可能需要保留在其他位置，或者“消息”图标仅用于聊天

        return view;
    }
}
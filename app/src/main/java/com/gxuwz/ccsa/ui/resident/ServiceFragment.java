package com.gxuwz.ccsa.ui.resident;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.User;
// 引入消息列表页面
import com.gxuwz.ccsa.ui.resident.MessageListActivity;

public class ServiceFragment extends Fragment {
    private static final String TAG = "ServiceFragment";
    private User currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service, container, false);

        // 修复：严格校验宿主Activity类型
        if (!(getActivity() instanceof ResidentMainActivity)) {
            Log.e(TAG, "宿主Activity不是ResidentMainActivity");
            Toast.makeText(getContext(), "页面加载异常", Toast.LENGTH_SHORT).show();
            return view;
        }

        ResidentMainActivity activity = (ResidentMainActivity) getActivity();
        currentUser = activity.getUser();

        if (currentUser == null) {
            Log.e(TAG, "用户信息为空，可能未登录");
            Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
            return view;
        }

        // 在线缴纳物业费
        view.findViewById(R.id.btn_pay_fee).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), PayPropertyFeeActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });

        // 查看缴费明细
        view.findViewById(R.id.btn_fee_detail).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), PaymentDetailActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });

        // 缴费异常申诉
        view.findViewById(R.id.btn_fee_appeal).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), PaymentAppealActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });

        // --- 核心修复：消息图标点击事件 ---
        // 绑定消息图标（布局中ID为ll_message）的点击事件
        View btnMessage = view.findViewById(R.id.ll_message);
        if (btnMessage != null) {
            btnMessage.setOnClickListener(v -> {
                if (currentUser == null) {
                    Toast.makeText(getContext(), "用户信息获取失败，请重新登录", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 跳转到聊天消息列表页面，传递当前用户信息
                Intent intent = new Intent(getActivity(), MessageListActivity.class);
                intent.putExtra("user", currentUser);
                startActivity(intent);
            });
        } else {
            Log.w(TAG, "未找到消息图标控件（ID：ll_message），请检查布局文件");
        }

        return view;
    }
}
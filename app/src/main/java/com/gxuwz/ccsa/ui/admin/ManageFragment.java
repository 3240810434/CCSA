// CCSA/app/src/main/java/com/gxuwz/ccsa/ui/admin/ManageFragment.java
package com.gxuwz.ccsa.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.gxuwz.ccsa.R;

public class ManageFragment extends Fragment {
    private static final String TAG = "ManageFragment";
    private String community;
    private Button btnResidentList;
    private Button btnInitiateVote;

    public ManageFragment() {}

    public static ManageFragment newInstance(String community) {
        ManageFragment fragment = new ManageFragment();
        Bundle args = new Bundle();
        args.putString("community", community);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            community = getArguments().getString("community");
            Log.d(TAG, "当前管理员所属小区: " + community);
        } else {
            Log.e(TAG, "未获取到小区信息！");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_manage, container, false);
        initViews(view);
        setupButtonListeners();
        return view;
    }

    private void initViews(View view) {
        // 确保按钮正确初始化
        btnResidentList = view.findViewById(R.id.btn_resident_list);
        if (btnResidentList == null) {
            Log.e(TAG, "布局中未找到btn_resident_list，请检查XML文件");
        }
        btnInitiateVote = view.findViewById(R.id.btn_initiate_vote);
    }

    private void setupButtonListeners() {
        // 确保按钮非空再设置点击事件
        if (btnResidentList != null) {
            btnResidentList.setOnClickListener(v -> {
                Log.d(TAG, "居民列表按钮点击，当前小区: " + community);

                if (getActivity() == null) {
                    Toast.makeText(getContext(), "页面状态异常", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (community == null || community.isEmpty()) {
                    Toast.makeText(getActivity(), "未获取到小区信息，请重新登录", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 跳转到管理员的居民列表页面
                Intent intent = new Intent(getActivity(), com.gxuwz.ccsa.ui.admin.ResidentListActivity.class);
                intent.putExtra("community", community);
                startActivity(intent);
            });
        }

        if (btnInitiateVote != null) {
            btnInitiateVote.setOnClickListener(v -> {
                Toast.makeText(getContext(), "发起投票功能", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
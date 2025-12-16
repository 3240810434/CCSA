package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.ui.admin.VoteListFragment; // 引用之前定义的 Fragment
import com.gxuwz.ccsa.util.SharedPreferencesUtil;

public class ResidentVoteActivity extends AppCompatActivity {

    private String community;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_vote);

        // 1. 获取用户信息 (优先从 SharedPreferences 获取，保证数据准确)
        community = SharedPreferencesUtil.getData(this, "community", "");
        userId = SharedPreferencesUtil.getData(this, "userId", "");

        // 兼容性处理：如果 Intent 传了值，也可以覆盖
        if (getIntent().hasExtra("community")) {
            community = getIntent().getStringExtra("community");
        }

        initView();
    }

    private void initView() {
        // 2. 设置顶部标题栏
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("小区投票");

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        // 3. 加载投票列表 Fragment
        // 参数说明: community=小区名, status=1(已发布), isAdmin=false(居民身份)
        VoteListFragment fragment = VoteListFragment.newInstance(community, 1, false);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}
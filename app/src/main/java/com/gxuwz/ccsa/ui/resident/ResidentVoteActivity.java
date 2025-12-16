package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.ui.admin.VoteListFragment;
import com.gxuwz.ccsa.util.SharedPreferencesUtil;

public class ResidentVoteActivity extends AppCompatActivity {

    private String community;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_vote);

        // 1. 获取小区信息 (优先 Intent，其次 SharedPreferences)
        if (getIntent().hasExtra("community")) {
            community = getIntent().getStringExtra("community");
        } else {
            community = SharedPreferencesUtil.getData(this, "community", "");
        }

        userId = SharedPreferencesUtil.getData(this, "userId", "");

        // 2. 调试/校验：如果没有小区信息，提示用户
        if (TextUtils.isEmpty(community)) {
            Toast.makeText(this, "无法获取小区信息，请重新登录", Toast.LENGTH_SHORT).show();
            // 此时加载列表可能为空，但不会崩溃
        }

        initView();
    }

    private void initView() {
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("小区投票");

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        // 3. 加载 Fragment，确保 status=1 (已发布)
        if (!TextUtils.isEmpty(community)) {
            VoteListFragment fragment = VoteListFragment.newInstance(community, 1, false);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
        }
    }
}
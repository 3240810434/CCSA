package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
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

        // 此时 SharedPreferencesUtil 已有 getData(Context, String, String) 方法
        community = SharedPreferencesUtil.getData(this, "community", "");
        userId = SharedPreferencesUtil.getData(this, "userId", "");

        if (getIntent().hasExtra("community")) {
            community = getIntent().getStringExtra("community");
        }

        initView();
    }

    private void initView() {
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("小区投票");

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        // 加载 VoteListFragment，参数：社区，状态1(已发布)，非管理员
        VoteListFragment fragment = VoteListFragment.newInstance(community, 1, false);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}
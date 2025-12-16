package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.ui.admin.VoteListFragment;
import com.gxuwz.ccsa.util.SharedPreferencesUtil;

public class ResidentVoteActivity extends AppCompatActivity {

    private String community;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_vote);

        // --- 修复代码开始 ---
        // 1. 获取用户信息与小区信息
        // 优先尝试从 Intent 获取传递过来的 User 对象
        User user = (User) getIntent().getSerializableExtra("user");

        // 如果 Intent 中没有，尝试从本地缓存中读取 User 对象 (作为兜底)
        if (user == null) {
            user = SharedPreferencesUtil.getUser(this);
        }

        // 如果获取到了 User 对象，从中提取信息
        if (user != null) {
            community = user.getCommunity();
            userId = String.valueOf(user.getId());
        } else {
            // 如果 User 对象完全获取失败，尝试读取旧逻辑中的字段 (兼容性保留)
            if (getIntent().hasExtra("community")) {
                community = getIntent().getStringExtra("community");
            } else {
                community = SharedPreferencesUtil.getData(this, "community", "");
            }
            userId = SharedPreferencesUtil.getData(this, "userId", "");
        }
        // --- 修复代码结束 ---

        // 2. 调试/校验：如果没有小区信息，提示用户
        if (TextUtils.isEmpty(community)) {
            Toast.makeText(this, "无法获取小区信息，请重新登录", Toast.LENGTH_SHORT).show();
            // 建议：如果获取失败，可以直接关闭页面，避免展示空白
            // finish();
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
            // 注意：VoteListFragment 实际上可能也需要 userId 来判断是否已投票
            // 如果 VoteListFragment 内部没有处理 userId，可能需要修改 Fragment 的 newInstance 方法传递 userId
            // 这里维持原样调用
            VoteListFragment fragment = VoteListFragment.newInstance(community, 1, false);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
        }
    }
}
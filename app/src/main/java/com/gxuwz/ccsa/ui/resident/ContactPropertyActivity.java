package com.gxuwz.ccsa.ui.resident;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Admin;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.util.SharedPreferencesUtil;

public class ContactPropertyActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private TextView tvPropertyName;
    private Button btnContactChat;
    private TextView tvStatusHint;

    private AppDatabase db;
    private User currentUser;
    private Admin propertyAdmin; // 查询到的该小区的管理员

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_property);

        // 设置标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("联系物业");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = AppDatabase.getInstance(this);
        // 获取当前登录的居民信息
        currentUser = SharedPreferencesUtil.getUser(this);

        initView();
        initData();
    }

    private void initView() {
        ivAvatar = findViewById(R.id.iv_property_avatar);
        tvPropertyName = findViewById(R.id.tv_property_name);
        btnContactChat = findViewById(R.id.btn_contact_chat);
        tvStatusHint = findViewById(R.id.tv_status_hint);

        // 默认显示管理员头像
        Glide.with(this)
                .load(R.drawable.admin)
                .apply(RequestOptions.circleCropTransform())
                .into(ivAvatar);

        // 点击事件：发起聊天
        btnContactChat.setOnClickListener(v -> {
            if (propertyAdmin != null) {
                // 跳转到聊天页面，传递精准的 Admin ID
                Intent intent = new Intent(ContactPropertyActivity.this, ChatActivity.class);
                intent.putExtra("myId", currentUser.getId());
                intent.putExtra("myRole", "RESIDENT");

                // 关键点：只能发给 propertyAdmin.getId()，实现了小区隔离
                intent.putExtra("targetId", propertyAdmin.getId());
                intent.putExtra("targetRole", "ADMIN"); // 明确目标角色是管理员

                // 优化标题显示
                String adminTitle = "物业管理员 (" + propertyAdmin.getCommunity() + ")";
                intent.putExtra("targetName", adminTitle);
                intent.putExtra("targetAvatar", "local_admin_resource"); // 标记使用本地资源

                startActivity(intent);
            } else {
                Toast.makeText(this, "当前小区暂无物业管理员入驻，请联系系统管理员", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initData() {
        if (currentUser == null) {
            Toast.makeText(this, "用户信息获取失败，请重新登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 获取居民所属小区
        String community = currentUser.getCommunity();

        new Thread(() -> {
            // 核心隔离逻辑：在数据库中查找负责该 community 的管理员
            // 假设 Admin 表有 community 字段，且 AdminDao 有 findByCommunity 方法
            propertyAdmin = db.adminDao().findByCommunity(community);

            runOnUiThread(() -> {
                if (propertyAdmin != null) {
                    // 找到管理员，UI 显示正常
                    // 动态设置标题：例如 "景悦小区 物业服务中心"
                    tvPropertyName.setText(community + " 物业服务中心");

                    btnContactChat.setEnabled(true);
                    btnContactChat.setAlpha(1.0f);
                    tvStatusHint.setText("如有紧急情况，也可直接拨打物业电话");
                } else {
                    // 未找到管理员
                    tvPropertyName.setText(community + "\n暂未配置物业管理员");
                    btnContactChat.setEnabled(false);
                    btnContactChat.setAlpha(0.5f); // 按钮变灰
                    btnContactChat.setText("暂不可用");
                    tvStatusHint.setText("该小区尚未入驻物业系统");
                }
            });
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
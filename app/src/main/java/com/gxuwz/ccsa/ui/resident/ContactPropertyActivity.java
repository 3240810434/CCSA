package com.gxuwz.ccsa.ui.resident;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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
    private TextView tvCommunityName;
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
        currentUser = SharedPreferencesUtil.getUser(this);

        initView();
        initData();
    }

    private void initView() {
        ivAvatar = findViewById(R.id.iv_property_avatar);
        tvPropertyName = findViewById(R.id.tv_property_name);
        tvCommunityName = findViewById(R.id.tv_community_name);
        btnContactChat = findViewById(R.id.btn_contact_chat);
        tvStatusHint = findViewById(R.id.tv_status_hint);

        // 圆形头像处理
        Glide.with(this)
                .load(R.drawable.admin) // 默认管理员头像
                .apply(RequestOptions.circleCropTransform())
                .into(ivAvatar);

        btnContactChat.setOnClickListener(v -> {
            if (propertyAdmin != null) {
                // 跳转到聊天页面
                Intent intent = new Intent(ContactPropertyActivity.this, ChatActivity.class);
                intent.putExtra("myId", currentUser.getId());
                intent.putExtra("myRole", "RESIDENT");
                intent.putExtra("targetId", propertyAdmin.getId());
                intent.putExtra("targetRole", "ADMIN");
                // 传递管理员的显示名称
                intent.putExtra("targetName", "物业管理员 (" + propertyAdmin.getCommunity() + ")");
                // 管理员使用本地特定资源作为头像标记，或者你可以传入具体的url
                intent.putExtra("targetAvatar", "local_admin_resource");
                startActivity(intent);
            } else {
                Toast.makeText(this, "当前小区暂无物业管理员入驻", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initData() {
        if (currentUser == null) {
            Toast.makeText(this, "用户信息获取失败，请重新登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String community = currentUser.getCommunity();
        tvCommunityName.setText(community);

        new Thread(() -> {
            // 根据居民所属的小区，查找对应的管理员
            propertyAdmin = db.adminDao().findByCommunity(community);

            runOnUiThread(() -> {
                if (propertyAdmin != null) {
                    tvPropertyName.setText(community + " 物业服务中心");
                    btnContactChat.setEnabled(true);
                    btnContactChat.setAlpha(1.0f);
                    tvStatusHint.setText("如遇紧急情况，请直接拨打物业电话");
                } else {
                    tvPropertyName.setText("暂未查询到物业信息");
                    btnContactChat.setEnabled(false);
                    btnContactChat.setAlpha(0.5f);
                    btnContactChat.setText("暂不可用");
                    tvStatusHint.setText("请联系系统管理员分配物业账号");
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
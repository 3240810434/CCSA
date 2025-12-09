package com.gxuwz.ccsa.ui.resident;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.HelpPostMediaAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.HelpPost;
import com.gxuwz.ccsa.model.HelpPostMedia;
import com.gxuwz.ccsa.model.PostMedia;
import com.gxuwz.ccsa.model.User;
import java.util.ArrayList;
import java.util.List;

public class HelpPostEditActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private ImageView ivPublishBtn, ivBack, ivAlbum;
    private RecyclerView rvPreview;
    private User currentUser;

    // 存储选中的媒体（转换为 HelpPostMedia 格式以便复用 Adapter）
    private List<HelpPostMedia> selectedMediaList = new ArrayList<>();
    private HelpPostMediaAdapter previewAdapter;

    // 注册 Activity Result 回调，用于接收 MediaSelectActivity 返回的数据
    private final ActivityResultLauncher<Intent> mediaPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    // 获取返回的 PostMedia 列表
                    List<PostMedia> list = (List<PostMedia>) result.getData().getSerializableExtra("selected_media");
                    if (list != null) {
                        updatePreview(list);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_post_edit);

        currentUser = (User) getIntent().getSerializableExtra("user");
        if (currentUser == null) {
            Toast.makeText(this, "用户信息错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        ivPublishBtn = findViewById(R.id.iv_publish_btn);
        ivBack = findViewById(R.id.iv_back);
        ivAlbum = findViewById(R.id.iv_album);
        rvPreview = findViewById(R.id.rv_media_preview);

        // 1. 修复返回按钮逻辑
        ivBack.setOnClickListener(v -> finish());

        // 2. 跳转选择图片/视频
        ivAlbum.setOnClickListener(v -> {
            Intent intent = new Intent(this, MediaSelectActivity.class);
            intent.putExtra("user", currentUser);
            intent.putExtra("is_help_post", true); // 标记：告诉 MediaSelectActivity 这是求助帖
            mediaPickerLauncher.launch(intent);
        });

        // 3. 发布按钮
        ivPublishBtn.setOnClickListener(v -> attemptPublish());

        // 初始化预览 Adapter
        rvPreview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        previewAdapter = new HelpPostMediaAdapter(this, selectedMediaList);
        // 注意：这里复用了 HelpPostMediaAdapter，如果它是 Grid 样式可能需要微调，或者在此处仅仅展示缩略图
        // 如果原 Adapter 强依赖 Grid，建议这里用简单的 ImageView 列表，但为了复用代码先这样写
        rvPreview.setAdapter(previewAdapter);
    }

    // 更新预览列表
    private void updatePreview(List<PostMedia> sourceList) {
        selectedMediaList.clear();
        for (PostMedia pm : sourceList) {
            HelpPostMedia hm = new HelpPostMedia();
            hm.url = pm.url;
            hm.type = pm.type; // 假设 PostMedia 和 HelpPostMedia 类型定义一致 (1:Image, 2:Video)
            selectedMediaList.add(hm);
        }

        if (selectedMediaList.isEmpty()) {
            rvPreview.setVisibility(View.GONE);
        } else {
            rvPreview.setVisibility(View.VISIBLE);
            previewAdapter.notifyDataSetChanged();
        }
    }

    private void attemptPublish() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        // 允许只发图不发文，或者只发文不发图，但不能全空
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(content) && selectedMediaList.isEmpty()) {
            Toast.makeText(this, "请输入内容或选择图片/视频", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("是否确认发布该求助帖子？")
                .setPositiveButton("确定", (dialog, which) -> saveToDb(title, content))
                .setNegativeButton("取消", null)
                .show();
    }

    private void saveToDb(String title, String content) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);

            // 修复 User ID 为 0 的问题
            if (currentUser.getId() == 0 && !TextUtils.isEmpty(currentUser.getPhone())) {
                User dbUser = db.userDao().findByPhone(currentUser.getPhone());
                if (dbUser != null) currentUser = dbUser;
            }

            if (currentUser.getId() == 0) {
                runOnUiThread(() -> Toast.makeText(this, "用户状态异常", Toast.LENGTH_SHORT).show());
                return;
            }

            HelpPost post = new HelpPost();
            post.userId = currentUser.getId();
            post.title = title;
            post.content = content;
            post.createTime = System.currentTimeMillis();

            // 设置帖子类型：0纯文，1图片，2视频
            if (selectedMediaList.isEmpty()) {
                post.type = 0;
            } else {
                post.type = selectedMediaList.get(0).type; // 取第一个媒体的类型
            }

            long postId = db.helpPostDao().insertPost(post);

            // 保存媒体列表
            if (!selectedMediaList.isEmpty()) {
                for (HelpPostMedia media : selectedMediaList) {
                    media.helpPostId = (int) postId;
                }
                db.helpPostDao().insertMediaList(selectedMediaList);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}
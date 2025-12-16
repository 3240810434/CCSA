package com.gxuwz.ccsa.ui.admin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Vote;
import com.gxuwz.ccsa.util.NotificationUtil;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateVoteActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private RadioGroup rgSelectionType;
    private LinearLayout layoutOptionsContainer;
    private TextView tvAttachmentName;
    private ImageView btnDeleteAttachment;

    private String community;
    private String adminAccount;
    private AppDatabase db;
    private String attachmentUriString = null;
    private long existingId = -1; // 用于编辑草稿

    private final ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    attachmentUriString = uri.toString();
                    tvAttachmentName.setText(uri.getPath());
                    btnDeleteAttachment.setVisibility(View.VISIBLE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_vote); // 对应下面的XML文件

        community = getIntent().getStringExtra("community");
        adminAccount = getIntent().getStringExtra("adminAccount");
        db = AppDatabase.getInstance(this);

        initViews();

        // 如果是编辑草稿，加载数据
        if (getIntent().hasExtra("vote_id")) {
            existingId = getIntent().getLongExtra("vote_id", -1);
            loadDraftData();
        } else {
            // 默认添加两个选项框
            addOptionView("");
            addOptionView("");
        }

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_vote_title);
        etContent = findViewById(R.id.et_vote_content);
        rgSelectionType = findViewById(R.id.rg_selection_type);
        layoutOptionsContainer = findViewById(R.id.layout_options_container);
        tvAttachmentName = findViewById(R.id.tv_attachment_name);
        btnDeleteAttachment = findViewById(R.id.iv_delete_attachment);

        findViewById(R.id.btn_add_option).setOnClickListener(v -> addOptionView(""));
        findViewById(R.id.layout_attachment).setOnClickListener(v -> pickFile());

        btnDeleteAttachment.setOnClickListener(v -> {
            attachmentUriString = null;
            tvAttachmentName.setText("添加附件/图片");
            btnDeleteAttachment.setVisibility(View.GONE);
        });

        findViewById(R.id.btn_save_draft).setOnClickListener(v -> saveVote(0));
        findViewById(R.id.btn_publish_now).setOnClickListener(v -> prePublishCheck());
    }

    private void addOptionView(String text) {
        // 对应下面的 item_create_vote_option.xml
        View view = LayoutInflater.from(this).inflate(R.layout.item_create_vote_option, layoutOptionsContainer, false);
        EditText etOption = view.findViewById(R.id.et_option_text);
        ImageView ivDelete = view.findViewById(R.id.iv_delete_option);

        etOption.setText(text);
        ivDelete.setOnClickListener(v -> layoutOptionsContainer.removeView(view));

        layoutOptionsContainer.addView(view);
    }

    private void pickFile() {
        // 简单权限检查，如果是在Android 10+通常不需要READ_EXTERNAL_STORAGE也能通过SAF获取图片，但为了稳妥保留检查
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        } else {
            filePickerLauncher.launch("image/*");
        }
    }

    private void loadDraftData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // 修正方法名
            Vote vote = db.voteDao().getVoteById(existingId);
            if (vote != null) {
                runOnUiThread(() -> {
                    etTitle.setText(vote.getTitle());
                    etContent.setText(vote.getContent());
                    // 修正 RadioButton 选中
                    if (vote.getSelectionType() == 1) {
                        ((RadioButton)findViewById(R.id.rb_multi)).setChecked(true);
                    } else {
                        ((RadioButton)findViewById(R.id.rb_single)).setChecked(true);
                    }

                    attachmentUriString = vote.getAttachmentPath();
                    if (attachmentUriString != null) {
                        tvAttachmentName.setText("已添加附件");
                        btnDeleteAttachment.setVisibility(View.VISIBLE);
                    }
                    List<String> options = vote.getOptionList();
                    layoutOptionsContainer.removeAllViews();
                    for (String opt : options) addOptionView(opt);
                });
            }
        });
    }

    private void prePublishCheck() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            int count = db.userDao().countResidents(community);
            runOnUiThread(() -> {
                new AlertDialog.Builder(this)
                        .setTitle("确认发布")
                        .setMessage("即将向本小区的 " + count + " 位居民发布投票，发布后无法修改。")
                        .setPositiveButton("确认发布", (d, w) -> saveVote(1))
                        .setNegativeButton("取消", null)
                        .show();
            });
        });
    }

    private void saveVote(int status) {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        int selectionType = rgSelectionType.getCheckedRadioButtonId() == R.id.rb_multi ? 1 : 0;

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "标题和内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder optionsBuilder = new StringBuilder();
        int validOptionCount = 0;
        for (int i = 0; i < layoutOptionsContainer.getChildCount(); i++) {
            View v = layoutOptionsContainer.getChildAt(i);
            EditText et = v.findViewById(R.id.et_option_text);
            String optText = et.getText().toString().trim();
            if (!TextUtils.isEmpty(optText)) {
                if (validOptionCount > 0) optionsBuilder.append("|#|");
                optionsBuilder.append(optText);
                validOptionCount++;
            }
        }

        if (validOptionCount < 2) {
            Toast.makeText(this, "请至少设置两个有效选项", Toast.LENGTH_SHORT).show();
            return;
        }

        Vote vote = new Vote(title, content, community, adminAccount, System.currentTimeMillis(),
                optionsBuilder.toString(), selectionType, status, attachmentUriString);
        if (existingId != -1) vote.setId(existingId);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            if (existingId != -1) db.voteDao().update(vote);
            else db.voteDao().insert(vote);

            if (status == 1) {
                runOnUiThread(() -> {
                    NotificationUtil.sendVoteNotification(this, "小区投票: " + title, content);
                    Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "已保存到草稿箱", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }
}
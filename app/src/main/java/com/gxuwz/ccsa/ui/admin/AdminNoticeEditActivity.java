package com.gxuwz.ccsa.ui.admin;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.AdminNotice;
import com.gxuwz.ccsa.model.Merchant;
import com.gxuwz.ccsa.model.Notification;
import com.gxuwz.ccsa.model.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

public class AdminNoticeEditActivity extends AppCompatActivity {
    private EditText etTitle, etContent;
    private CheckBox cbMerchant, cbResident;
    private TextView tvSelectedBuildings, tvAttachmentName;
    private View btnDeleteAttachment;

    private String attachmentUriString = null;
    private List<Integer> selectedBuildings = new ArrayList<>(); // 存储选择的楼栋号 1-10
    private long existingId = -1; // 如果是编辑草稿，这里会有ID

    private final String[] buildingItems = {"1栋", "2栋", "3栋", "4栋", "5栋", "6栋", "7栋", "8栋", "9栋", "10栋"};
    private boolean[] buildingCheckedItems = new boolean[10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notice_edit);

        initViews();
        checkIntentData();
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        cbMerchant = findViewById(R.id.cb_merchant);
        cbResident = findViewById(R.id.cb_resident);
        tvSelectedBuildings = findViewById(R.id.tv_selected_buildings);
        tvAttachmentName = findViewById(R.id.tv_attachment_name);
        btnDeleteAttachment = findViewById(R.id.iv_delete_attachment);

        // 附件点击
        findViewById(R.id.layout_attachment).setOnClickListener(v -> pickFile());
        btnDeleteAttachment.setOnClickListener(v -> {
            attachmentUriString = null;
            tvAttachmentName.setText("添加附件/图片");
            btnDeleteAttachment.setVisibility(View.GONE);
        });

        // 居民CheckBox点击逻辑：弹出楼栋选择
        cbResident.setOnClickListener(v -> {
            if (cbResident.isChecked()) {
                showBuildingSelectionDialog();
                tvSelectedBuildings.setVisibility(View.VISIBLE);
            } else {
                tvSelectedBuildings.setVisibility(View.GONE);
                selectedBuildings.clear();
            }
        });
        // 点击文字也可以触发重选
        tvSelectedBuildings.setOnClickListener(v -> {
            if(cbResident.isChecked()) showBuildingSelectionDialog();
        });

        findViewById(R.id.btn_save_draft).setOnClickListener(v -> saveNotice(0)); // 0 = Draft
        findViewById(R.id.btn_publish_now).setOnClickListener(v -> saveNotice(1)); // 1 = Published
    }

    private void checkIntentData() {
        existingId = getIntent().getLongExtra("notice_id", -1);
        if (existingId != -1) {
            // 加载草稿数据
            Executors.newSingleThreadExecutor().execute(() -> {
                AdminNotice notice = AppDatabase.getInstance(this).adminNoticeDao().getById(existingId);
                runOnUiThread(() -> {
                    etTitle.setText(notice.getTitle());
                    etContent.setText(notice.getContent());
                    attachmentUriString = notice.getAttachmentPath();
                    if (attachmentUriString != null) {
                        tvAttachmentName.setText("已添加附件");
                        btnDeleteAttachment.setVisibility(View.VISIBLE);
                    }

                    String type = notice.getTargetType();
                    if ("MERCHANT".equals(type) || "BOTH".equals(type)) cbMerchant.setChecked(true);
                    if ("RESIDENT".equals(type) || "BOTH".equals(type)) {
                        cbResident.setChecked(true);
                        tvSelectedBuildings.setVisibility(View.VISIBLE);
                        // 解析楼栋 "1,2,3" 或 "ALL"
                        restoreBuildings(notice.getTargetBuildings());
                    }
                });
            });
        }
    }

    private void restoreBuildings(String buildingsStr) {
        if ("ALL".equals(buildingsStr)) {
            for(int i=0; i<10; i++) { buildingCheckedItems[i] = true; selectedBuildings.add(i+1); }
            tvSelectedBuildings.setText("已选: 全部楼栋");
        } else if (!TextUtils.isEmpty(buildingsStr)) {
            String[] splits = buildingsStr.split(",");
            StringBuilder sb = new StringBuilder("已选: ");
            for (String s : splits) {
                try {
                    int b = Integer.parseInt(s.trim());
                    if(b>=1 && b<=10) {
                        buildingCheckedItems[b-1] = true;
                        selectedBuildings.add(b);
                        sb.append(b).append("栋,");
                    }
                } catch (Exception e){}
            }
            tvSelectedBuildings.setText(sb.toString());
        }
    }

    private void showBuildingSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择接收通知的楼栋");
        builder.setMultiChoiceItems(buildingItems, buildingCheckedItems, (dialog, which, isChecked) -> {
            buildingCheckedItems[which] = isChecked;
        });
        builder.setPositiveButton("确定", (dialog, which) -> {
            selectedBuildings.clear();
            StringBuilder sb = new StringBuilder("已选: ");
            boolean all = true;
            for (int i = 0; i < 10; i++) {
                if (buildingCheckedItems[i]) {
                    selectedBuildings.add(i + 1);
                    sb.append(i + 1).append("栋,");
                } else {
                    all = false;
                }
            }
            if (selectedBuildings.isEmpty()) {
                cbResident.setChecked(false);
                tvSelectedBuildings.setVisibility(View.GONE);
            } else {
                tvSelectedBuildings.setText(all ? "已选: 全部楼栋" : sb.toString());
            }
        });
        builder.setNeutralButton("全选", (dialog, which) -> {
            for(int i=0; i<10; i++) buildingCheckedItems[i] = true;
            selectedBuildings.clear();
            for(int i=1; i<=10; i++) selectedBuildings.add(i);
            tvSelectedBuildings.setText("已选: 全部楼栋");
        });
        builder.show();
    }

    // 文件选择器
    private final ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    attachmentUriString = uri.toString();
                    tvAttachmentName.setText(uri.getPath());
                    btnDeleteAttachment.setVisibility(View.VISIBLE);
                }
            });

    private void pickFile() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        } else {
            filePickerLauncher.launch("image/*");
        }
    }

    private void saveNotice(int status) {
        String title = etTitle.getText().toString();
        String content = etContent.getText().toString();
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "标题和内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!cbResident.isChecked() && !cbMerchant.isChecked()) {
            Toast.makeText(this, "请至少选择一种接收对象", Toast.LENGTH_SHORT).show();
            return;
        }

        String targetType = "NONE";
        if (cbResident.isChecked() && cbMerchant.isChecked()) targetType = "BOTH";
        else if (cbResident.isChecked()) targetType = "RESIDENT";
        else if (cbMerchant.isChecked()) targetType = "MERCHANT";

        // --- 修复: 变量在 Lambda 表达式中必须是 final 或 effective final ---
        String tempBuildingsStr = "";
        if (cbResident.isChecked()) {
            if (selectedBuildings.size() == 10) tempBuildingsStr = "ALL";
            else {
                StringBuilder sb = new StringBuilder();
                for (int b : selectedBuildings) sb.append(b).append(",");
                if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
                tempBuildingsStr = sb.toString();
            }
        }
        final String buildingsStrFinal = tempBuildingsStr; // 定义 final 变量供线程使用
        // -------------------------------------------------------------

        AdminNotice notice = new AdminNotice(title, content, targetType, buildingsStrFinal, attachmentUriString, status, new Date(), status == 1 ? new Date() : null);
        if (existingId != -1) notice.setId(existingId);

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            long noticeId;
            if (existingId != -1) {
                db.adminNoticeDao().update(notice);
                noticeId = existingId;
                if (status == 1) db.notificationDao().deleteByAdminNoticeId(noticeId);
            } else {
                noticeId = db.adminNoticeDao().insert(notice);
            }

            if (status == 1) {
                List<Notification> distributionList = new ArrayList<>();
                Date now = new Date();

                // 1. 发给商家
                if (cbMerchant.isChecked()) {
                    List<Merchant> merchants = db.merchantDao().getAllMerchants(); // 现在 MerchantDao 已有此方法
                    for (Merchant m : merchants) {
                        distributionList.add(new Notification(noticeId, "所有店铺", m.getPhone(), title, content, 2, attachmentUriString, "管理员", now, false));
                    }
                }

                // 2. 发给居民
                if (cbResident.isChecked()) {
                    List<User> residents;
                    // 使用 final 变量 buildingsStrFinal
                    if ("ALL".equals(buildingsStrFinal)) {
                        residents = db.userDao().getAllUsers(); // 现在 UserDao 已有此方法
                    } else {
                        residents = db.userDao().getUsersByBuildings(selectedBuildings);
                    }
                    for (User u : residents) {
                        distributionList.add(new Notification(noticeId, u.getCommunityName(), u.getPhone(), title, content, 2, attachmentUriString, "管理员", now, false));
                    }
                }

                if (!distributionList.isEmpty()) {
                    db.notificationDao().insertAll(distributionList);
                }
            }

            runOnUiThread(() -> {
                Toast.makeText(this, status == 1 ? "发布成功" : "草稿已保存", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
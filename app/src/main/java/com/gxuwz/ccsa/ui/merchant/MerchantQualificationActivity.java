package com.gxuwz.ccsa.ui.merchant;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList; // 新增导入
import android.graphics.Color; // 新增导入
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Merchant;

import java.util.concurrent.Executors;

public class MerchantQualificationActivity extends AppCompatActivity {

    private Merchant currentMerchant;
    private int merchantId; // 新增：只保存ID，用于查询最新数据
    private TextView tvStatusText;
    private ImageView ivStatusIcon;
    private ImageView ivIdFront, ivIdBack, ivLicense;
    private Button btnSubmit;
    private LinearLayout layoutOverlay;

    // 0=Front, 1=Back, 2=License
    private int currentUploadType = 0;
    private Uri uriIdFront, uriIdBack, uriLicense;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_qualification);
        setTitle("商家资质");

        // 从 Intent 中获取商家对象，但主要为了获取 ID
        Merchant tempMerchant = (Merchant) getIntent().getSerializableExtra("merchant");
        if (tempMerchant != null) {
            merchantId = tempMerchant.getId();
            currentMerchant = tempMerchant; // 暂时赋值，避免空指针，onResume 会覆盖它
        }

        initViews();
        setupImagePicker();
        // 注意：这里不再调用 refreshUI，改为在 onResume 中加载最新数据后刷新
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 关键修复：每次页面显示时，从数据库重新加载最新的商家信息
        loadMerchantData();
    }

    private void loadMerchantData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // 从数据库查询最新的 Merchant 对象
            Merchant freshMerchant = AppDatabase.getInstance(this).merchantDao().findById(merchantId);
            runOnUiThread(() -> {
                if (freshMerchant != null) {
                    currentMerchant = freshMerchant;
                    // 恢复图片 URI 变量，防止提交时为空
                    if (currentMerchant.getIdCardFrontUri() != null) uriIdFront = Uri.parse(currentMerchant.getIdCardFrontUri());
                    if (currentMerchant.getIdCardBackUri() != null) uriIdBack = Uri.parse(currentMerchant.getIdCardBackUri());
                    if (currentMerchant.getLicenseUri() != null) uriLicense = Uri.parse(currentMerchant.getLicenseUri());

                    refreshUI(); // 用最新数据刷新 UI
                } else {
                    Toast.makeText(this, "商家信息加载失败", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    private void initViews() {
        tvStatusText = findViewById(R.id.tv_status_text);
        ivStatusIcon = findViewById(R.id.iv_status_icon);
        ivIdFront = findViewById(R.id.iv_id_card_front);
        ivIdBack = findViewById(R.id.iv_id_card_back);
        ivLicense = findViewById(R.id.iv_license);
        btnSubmit = findViewById(R.id.btn_submit_qualification);
        layoutOverlay = findViewById(R.id.layout_audit_overlay);

        ivIdFront.setOnClickListener(v -> pickImage(0));
        ivIdBack.setOnClickListener(v -> pickImage(1));
        ivLicense.setOnClickListener(v -> pickImage(2));
        btnSubmit.setOnClickListener(v -> submitQualification());
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            try {
                                getContentResolver().takePersistableUriPermission(
                                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception e) { e.printStackTrace(); }

                            displayImage(uri, currentUploadType);
                        }
                    }
                }
        );
    }

    private void pickImage(int type) {
        // 双重保险：如果处于审核中(1)或已认证(2)，禁止点击
        if (currentMerchant != null &&
                (currentMerchant.getQualificationStatus() == 1 || currentMerchant.getQualificationStatus() == 2)) {
            return;
        }

        currentUploadType = type;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void displayImage(Uri uri, int type) {
        switch (type) {
            case 0:
                uriIdFront = uri;
                ivIdFront.setImageURI(uri);
                ivIdFront.setPadding(0,0,0,0);
                break;
            case 1:
                uriIdBack = uri;
                ivIdBack.setImageURI(uri);
                ivIdBack.setPadding(0,0,0,0);
                break;
            case 2:
                uriLicense = uri;
                ivLicense.setImageURI(uri);
                ivLicense.setPadding(0,0,0,0);
                break;
        }
    }

    private void refreshUI() {
        if (currentMerchant == null) return;

        int status = currentMerchant.getQualificationStatus();

        // 加载显示的图片
        if (currentMerchant.getIdCardFrontUri() != null) {
            ivIdFront.setImageURI(Uri.parse(currentMerchant.getIdCardFrontUri()));
            ivIdFront.setPadding(0,0,0,0);
        }
        if (currentMerchant.getIdCardBackUri() != null) {
            ivIdBack.setImageURI(Uri.parse(currentMerchant.getIdCardBackUri()));
            ivIdBack.setPadding(0,0,0,0);
        }
        if (currentMerchant.getLicenseUri() != null) {
            ivLicense.setImageURI(Uri.parse(currentMerchant.getLicenseUri()));
            ivLicense.setPadding(0,0,0,0);
        }

        switch (status) {
            case 0: // 未认证
                tvStatusText.setText("未认证");
                ivStatusIcon.setImageResource(R.drawable.warn);
                layoutOverlay.setVisibility(View.GONE);
                setInputsEnabled(true);
                break;
            case 1: // 审核中
                tvStatusText.setText("审核中");
                ivStatusIcon.setImageResource(R.drawable.hourglass);
                layoutOverlay.setVisibility(View.VISIBLE);
                setInputsEnabled(false);
                break;
            case 2: // 已认证
                tvStatusText.setText("已认证资质");
                ivStatusIcon.setImageResource(R.drawable.shield);
                layoutOverlay.setVisibility(View.GONE);
                setInputsEnabled(false);
                btnSubmit.setVisibility(View.GONE);
                break;
            case 3: // 未通过
                tvStatusText.setText("未通过审核");
                ivStatusIcon.setImageResource(R.drawable.warn);
                layoutOverlay.setVisibility(View.GONE);
                setInputsEnabled(true);
                break;
        }
    }

    private void setInputsEnabled(boolean enabled) {
        ivIdFront.setEnabled(enabled);
        ivIdBack.setEnabled(enabled);
        ivLicense.setEnabled(enabled);
        btnSubmit.setEnabled(enabled);

        // 无论启用还是禁用，首先重置背景资源以保留圆角形状
        btnSubmit.setBackgroundResource(R.drawable.button_blue);

        if (!enabled) {
            // 禁用状态：设置灰色滤镜 (保留圆角)
            btnSubmit.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            btnSubmit.setText("审核中 / 已锁定");
        } else {
            // 启用状态：设置绿色滤镜 #32CD32
            btnSubmit.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#32CD32")));
            btnSubmit.setText("提交审核");
        }
    }

    private void submitQualification() {
        if (uriIdFront == null || uriIdBack == null || uriLicense == null) {
            Toast.makeText(this, "请补全所有证件照片", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新对象属性
        currentMerchant.setIdCardFrontUri(uriIdFront.toString());
        currentMerchant.setIdCardBackUri(uriIdBack.toString());
        currentMerchant.setLicenseUri(uriLicense.toString());
        currentMerchant.setQualificationStatus(1); // 设置为审核中

        // 保存到数据库
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).merchantDao().update(currentMerchant);
            runOnUiThread(() -> {
                Toast.makeText(this, "提交成功，请等待审核", Toast.LENGTH_SHORT).show();
                // 提交后刷新本地数据和UI，确保状态同步
                loadMerchantData();
            });
        });
    }
}
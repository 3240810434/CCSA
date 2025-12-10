package com.gxuwz.ccsa.ui.merchant;

import android.app.Activity;
import android.content.Intent;
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

        // 获取传递过来的商家对象
        currentMerchant = (Merchant) getIntent().getSerializableExtra("merchant");

        initViews();
        setupImagePicker();
        refreshUI(); // 根据状态刷新UI
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
        // 如果处于锁定状态，禁止点击
        if (currentMerchant.getQualificationStatus() == 1) return;

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

        // 加载已存在的图片
        if (currentMerchant.getIdCardFrontUri() != null) displayImage(Uri.parse(currentMerchant.getIdCardFrontUri()), 0);
        if (currentMerchant.getIdCardBackUri() != null) displayImage(Uri.parse(currentMerchant.getIdCardBackUri()), 1);
        if (currentMerchant.getLicenseUri() != null) displayImage(Uri.parse(currentMerchant.getLicenseUri()), 2);

        switch (status) {
            case 0: // 未认证
                tvStatusText.setText("未认证");
                ivStatusIcon.setImageResource(R.drawable.warn);
                layoutOverlay.setVisibility(View.GONE);
                setInputsEnabled(true);
                break;
            case 1: // 审核中
                tvStatusText.setText("未认证"); // 保持顶部不变，但中间显示遮罩
                ivStatusIcon.setImageResource(R.drawable.warn);
                layoutOverlay.setVisibility(View.VISIBLE);
                setInputsEnabled(false);
                break;
            case 2: // 已认证
                tvStatusText.setText("已认证资质");
                ivStatusIcon.setImageResource(R.drawable.shield);
                layoutOverlay.setVisibility(View.GONE);
                setInputsEnabled(true); // 解锁，允许重新修改提交
                break;
            case 3: // 未通过
                tvStatusText.setText("未通过审核");
                ivStatusIcon.setImageResource(R.drawable.warn);
                layoutOverlay.setVisibility(View.GONE);
                setInputsEnabled(true); // 解锁，允许重新提交
                break;
        }
    }

    private void setInputsEnabled(boolean enabled) {
        ivIdFront.setEnabled(enabled);
        ivIdBack.setEnabled(enabled);
        ivLicense.setEnabled(enabled);
        btnSubmit.setEnabled(enabled);
        if (!enabled) {
            btnSubmit.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            btnSubmit.setBackgroundResource(R.drawable.button_blue);
        }
    }

    private void submitQualification() {
        if (uriIdFront == null || uriIdBack == null || uriLicense == null) {
            Toast.makeText(this, "请补全所有证件照片", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新对象
        currentMerchant.setIdCardFrontUri(uriIdFront.toString());
        currentMerchant.setIdCardBackUri(uriIdBack.toString());
        currentMerchant.setLicenseUri(uriLicense.toString());
        currentMerchant.setQualificationStatus(1); // 设置为审核中

        // 保存到数据库
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).merchantDao().update(currentMerchant);
            runOnUiThread(() -> {
                Toast.makeText(this, "提交成功，请等待审核", Toast.LENGTH_SHORT).show();
                refreshUI(); // 刷新界面状态，显示遮罩
            });
        });
    }
}
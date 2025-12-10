package com.gxuwz.ccsa.ui.admin;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Merchant;
import java.util.concurrent.Executors;

public class MerchantAuditDetailActivity extends AppCompatActivity {

    private int merchantId;
    private Merchant currentMerchant;

    private TextView tvName, tvInfo;
    private ImageView ivFront, ivBack, ivLicense;
    private Button btnReject, btnApprove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_audit_detail);
        setTitle("审核详情");

        merchantId = getIntent().getIntExtra("merchant_id", -1);

        initViews();
        loadData();
    }

    private void initViews() {
        tvName = findViewById(R.id.tv_detail_name);
        tvInfo = findViewById(R.id.tv_detail_info);
        ivFront = findViewById(R.id.iv_detail_front);
        ivBack = findViewById(R.id.iv_detail_back);
        ivLicense = findViewById(R.id.iv_detail_license);
        btnReject = findViewById(R.id.btn_reject);
        btnApprove = findViewById(R.id.btn_approve);

        btnReject.setOnClickListener(v -> processAudit(3)); // 3 = 驳回/未通过
        btnApprove.setOnClickListener(v -> processAudit(2)); // 2 = 通过/已认证
    }

    private void loadData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            currentMerchant = AppDatabase.getInstance(this).merchantDao().findById(merchantId);
            runOnUiThread(() -> {
                if (currentMerchant != null) {
                    tvName.setText(currentMerchant.getMerchantName());
                    tvInfo.setText("联系人：" + currentMerchant.getContactName() + "\n电话：" + currentMerchant.getPhone());

                    if (currentMerchant.getIdCardFrontUri() != null) ivFront.setImageURI(Uri.parse(currentMerchant.getIdCardFrontUri()));
                    if (currentMerchant.getIdCardBackUri() != null) ivBack.setImageURI(Uri.parse(currentMerchant.getIdCardBackUri()));
                    if (currentMerchant.getLicenseUri() != null) ivLicense.setImageURI(Uri.parse(currentMerchant.getLicenseUri()));
                }
            });
        });
    }

    private void processAudit(int newStatus) {
        if (currentMerchant == null) return;

        currentMerchant.setQualificationStatus(newStatus);

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).merchantDao().update(currentMerchant);
            runOnUiThread(() -> {
                String msg = (newStatus == 2) ? "已通过审核" : "已驳回申请";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                finish(); // 关闭页面返回列表，列表会自动刷新
            });
        });
    }
}

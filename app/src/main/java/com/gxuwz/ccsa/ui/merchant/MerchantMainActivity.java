// CCSA/app/src/main/java/com/gxuwz/ccsa/ui/merchant/MerchantMainActivity.java
package com.gxuwz.ccsa.ui.merchant;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.Merchant;

public class MerchantMainActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private TextView tvServiceArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_main);

        initViews();
        showMerchantInfo();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tv_welcome);
        tvServiceArea = findViewById(R.id.tv_service_area);
    }

    private void showMerchantInfo() {
        // 获取传递的商家信息
        Merchant merchant = (Merchant) getIntent().getSerializableExtra("merchant");
        if (merchant != null) {
            tvWelcome.setText("欢迎您，" + merchant.getMerchantName() + "！");
            tvServiceArea.setText("服务小区：" + merchant.getCommunity());
        }
    }
}
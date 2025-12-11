package com.gxuwz.ccsa.ui.merchant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Merchant;
import com.gxuwz.ccsa.model.Product;

import org.json.JSONArray;
import org.json.JSONObject;

public class MerchantProductDetailActivity extends AppCompatActivity {

    private int productId;
    // 移除 Banner，改用 ImageView 简单实现，如果需要轮播建议使用 ViewPager2 但代码量较大，这里先保证不报错
    private ImageView ivSingleImage;
    private TextView tvName, tvMerchantName, tvDesc;
    private ImageView ivMerchantAvatar, btnBack;
    private LinearLayout llPriceTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_product_detail);

        productId = getIntent().getIntExtra("product_id", -1);
        initView();
        loadData();
    }

    private void initView() {
        btnBack = findViewById(R.id.btn_close_detail);
        ivSingleImage = findViewById(R.id.iv_banner_single); // 确保 XML 里有这个 ID

        tvName = findViewById(R.id.tv_detail_name);
        llPriceTable = findViewById(R.id.ll_detail_price_table);
        ivMerchantAvatar = findViewById(R.id.iv_merchant_avatar);
        tvMerchantName = findViewById(R.id.tv_merchant_name);
        tvDesc = findViewById(R.id.tv_detail_desc);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadData() {
        new Thread(() -> {
            // ProductDao 已在第三步添加了 getProductById
            Product product = AppDatabase.getInstance(this).productDao().getProductById(productId);
            if (product != null) {
                // MerchantDao 中应该有 getMerchantById，如果没有请参考 ProductDao 补上
                // 这里假设 MerchantDao 已存在该方法，如果报错请仿照 ProductDao 添加
                Merchant merchant = AppDatabase.getInstance(this).merchantDao().getMerchantById(product.merchantId);
                runOnUiThread(() -> updateUI(product, merchant));
            }
        }).start();
    }

    private void updateUI(Product product, Merchant merchant) {
        tvName.setText(product.name);
        tvDesc.setText(product.description);

        String imgUrl = product.getFirstImage();
        if (imgUrl != null && !imgUrl.isEmpty()) {
            Glide.with(this).load(imgUrl).into(ivSingleImage);
        }

        llPriceTable.removeAllViews();
        try {
            JSONArray jsonArray = new JSONArray(product.priceTableJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                // item_price_table_row_view 已在第四步补充
                View row = LayoutInflater.from(this).inflate(R.layout.item_price_table_row_view, llPriceTable, false);
                TextView t1 = row.findViewById(R.id.tv_row_desc);
                TextView t2 = row.findViewById(R.id.tv_row_price);

                String desc = obj.optString("desc");
                String price = obj.optString("price");

                if (t1 != null) t1.setText(desc);
                if (t2 != null) t2.setText("¥" + price);

                llPriceTable.addView(row);
            }
        } catch (Exception e) {
            // 如果解析失败，显示旧价格
            TextView tv = new TextView(this);
            tv.setText("价格: ¥" + product.price);
            llPriceTable.addView(tv);
        }

        if (merchant != null) {
            tvMerchantName.setText(merchant.name);
        }
    }
}
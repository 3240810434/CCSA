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
        ivSingleImage = findViewById(R.id.iv_banner_single);

        tvName = findViewById(R.id.tv_detail_name);
        llPriceTable = findViewById(R.id.ll_detail_price_table);
        ivMerchantAvatar = findViewById(R.id.iv_merchant_avatar);
        tvMerchantName = findViewById(R.id.tv_merchant_name);
        tvDesc = findViewById(R.id.tv_detail_desc);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadData() {
        new Thread(() -> {
            // 获取商品信息
            Product product = AppDatabase.getInstance(this).productDao().getProductById(productId);
            if (product != null) {
                // 修正1: 使用 findById 替代 getMerchantById (MerchantDao 中定义的方法名是 findById)
                Merchant merchant = AppDatabase.getInstance(this).merchantDao().findById(product.merchantId);
                runOnUiThread(() -> updateUI(product, merchant));
            }
        }).start();
    }

    private void updateUI(Product product, Merchant merchant) {
        tvName.setText(product.name);
        tvDesc.setText(product.description);

        // 修正2: 现在 Product 类中已经有了 getFirstImage() 方法
        String imgUrl = product.getFirstImage();
        if (imgUrl != null && !imgUrl.isEmpty()) {
            Glide.with(this).load(imgUrl).into(ivSingleImage);
        } else {
            ivSingleImage.setImageResource(R.drawable.shopping); // 默认图
        }

        llPriceTable.removeAllViews();
        try {
            JSONArray jsonArray = new JSONArray(product.priceTableJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                View row = LayoutInflater.from(this).inflate(R.layout.item_price_table_row_view, llPriceTable, false);
                TextView t1 = row.findViewById(R.id.tv_row_desc);
                TextView t2 = row.findViewById(R.id.tv_row_price);

                String desc = obj.optString("desc");
                String priceStr = obj.optString("price");

                if (t1 != null) t1.setText(desc);
                if (t2 != null) t2.setText("¥" + priceStr);

                llPriceTable.addView(row);
            }
        } catch (Exception e) {
            // 修正3: 移除了对 product.price 的引用，因为 Product 类中没有这个字段了
            // 如果解析 JSON 失败，只显示简单的文本提示
            TextView tv = new TextView(this);
            tv.setText("价格信息解析失败");
            tv.setPadding(10, 10, 10, 10);
            llPriceTable.addView(tv);
        }

        if (merchant != null) {
            // 修正4: 使用 Getter 方法获取商家名称，而不是直接访问 private 字段
            tvMerchantName.setText(merchant.getMerchantName());

            // 如果有头像也可以加载
            // if (merchant.getAvatar() != null) Glide.with(this).load(merchant.getAvatar()).into(ivMerchantAvatar);
        } else {
            tvMerchantName.setText("未知商家");
        }
    }
}
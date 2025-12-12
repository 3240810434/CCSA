package com.gxuwz.ccsa.ui.merchant;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Product;
import com.gxuwz.ccsa.util.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PhysicalProductEditActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 101;
    private EditText etName, etDesc;
    private LinearLayout llImageContainer, llPriceTableContainer;
    private RadioGroup rgDelivery;
    private RadioGroup rgTag; // 标签RadioGroup
    private List<String> selectedImagePaths = new ArrayList<>();
    private ImageView ivAddImage;

    // 保存正在编辑的商品对象
    private Product mEditingProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physical_product_edit);

        initView();

        // 判断是新增还是编辑
        if (getIntent().hasExtra("product")) {
            mEditingProduct = (Product) getIntent().getSerializableExtra("product");
            initDataFromProduct();
        } else {
            // 新增模式，默认添加3行空价格表
            addPriceRow();
            addPriceRow();
            addPriceRow();
        }
    }

    private void initView() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        etName = findViewById(R.id.et_name);
        etDesc = findViewById(R.id.et_desc);
        llImageContainer = findViewById(R.id.ll_image_container);
        ivAddImage = findViewById(R.id.iv_add_image);
        llPriceTableContainer = findViewById(R.id.ll_price_table_container);
        rgDelivery = findViewById(R.id.rg_delivery);
        rgTag = findViewById(R.id.rg_tag); // 初始化标签RadioGroup
        Button btnPublish = findViewById(R.id.btn_publish);
        ImageView btnAddPriceRow = findViewById(R.id.btn_add_price_row);

        ivAddImage.setOnClickListener(v -> checkPermissionAndPickImage());
        btnAddPriceRow.setOnClickListener(v -> addPriceRow());
        btnPublish.setOnClickListener(v -> attemptPublish());
    }

    private void initDataFromProduct() {
        // 1. 回显文本信息
        etName.setText(mEditingProduct.name);
        etDesc.setText(mEditingProduct.description);

        // 2. 回显配送方式
        if (mEditingProduct.deliveryMethod == 0) {
            rgDelivery.check(R.id.rb_delivery);
        } else {
            rgDelivery.check(R.id.rb_pickup);
        }

        // 3. 回显图片
        if (mEditingProduct.imagePaths != null && !mEditingProduct.imagePaths.isEmpty()) {
            String[] paths = mEditingProduct.imagePaths.split(",");
            for (String path : paths) {
                if (!path.trim().isEmpty()) {
                    selectedImagePaths.add(path);
                }
            }
            renderImages();
        }

        // 4. 回显价格表
        llPriceTableContainer.removeAllViews();
        try {
            JSONArray jsonArray = new JSONArray(mEditingProduct.priceTableJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                addPriceRowWithData(obj.optString("desc"), obj.optString("price"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            addPriceRow();
        }

        // 5. 回显商品标签
        if (mEditingProduct.tag != null) {
            switch (mEditingProduct.tag) {
                case "生鲜食材":
                    rgTag.check(R.id.rb_tag_fresh);
                    break;
                case "日用百货":
                    rgTag.check(R.id.rb_tag_daily);
                    break;
                case "零食饮品":
                    rgTag.check(R.id.rb_tag_snack);
                    break;
                default:
                    rgTag.check(R.id.rb_tag_fresh);
                    break;
            }
        }
    }

    private void addPriceRow() {
        View rowView = LayoutInflater.from(this).inflate(R.layout.item_price_table_row_edit, llPriceTableContainer, false);
        llPriceTableContainer.addView(rowView);
    }

    private void addPriceRowWithData(String desc, String price) {
        View rowView = LayoutInflater.from(this).inflate(R.layout.item_price_table_row_edit, llPriceTableContainer, false);
        EditText etItem = rowView.findViewById(R.id.et_price_item);
        EditText etPrice = rowView.findViewById(R.id.et_price_value);

        if (etItem != null) etItem.setText(desc);
        if (etPrice != null) etPrice.setText(price);

        llPriceTableContainer.addView(rowView);
    }

    private void checkPermissionAndPickImage() {
        if (selectedImagePaths.size() >= 9) {
            Toast.makeText(this, "最多上传9张图片", Toast.LENGTH_SHORT).show();
            return;
        }
        openGallery();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    getContentResolver().takePersistableUriPermission(
                            imageUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }

                selectedImagePaths.add(imageUri.toString());
                renderImages();
            }
        }
    }

    private void renderImages() {
        llImageContainer.removeAllViews();
        llImageContainer.addView(ivAddImage);

        for (String path : selectedImagePaths) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_image_preview_small, llImageContainer, false);
            ImageView iv = itemView.findViewById(R.id.iv_image);
            ImageView btnDel = itemView.findViewById(R.id.btn_delete);

            Glide.with(this).load(path).into(iv);
            btnDel.setOnClickListener(v -> {
                selectedImagePaths.remove(path);
                renderImages();
            });

            llImageContainer.addView(itemView, llImageContainer.getChildCount() - 1);
        }
    }

    private void attemptPublish() {
        String name = etName.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "请输入商品名称", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONArray priceJson = new JSONArray();
        try {
            for (int i = 0; i < llPriceTableContainer.getChildCount(); i++) {
                View row = llPriceTableContainer.getChildAt(i);
                EditText etItem = row.findViewById(R.id.et_price_item);
                EditText etPrice = row.findViewById(R.id.et_price_value);

                if (etItem != null && etPrice != null) {
                    String itemText = etItem.getText().toString().trim();
                    String priceVal = etPrice.getText().toString().trim();

                    if (!itemText.isEmpty() && !priceVal.isEmpty()) {
                        JSONObject obj = new JSONObject();
                        obj.put("desc", itemText);
                        obj.put("price", priceVal);
                        priceJson.put(obj);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (priceJson.length() == 0) {
            Toast.makeText(this, "请至少输入一行完整的价格信息", Toast.LENGTH_SHORT).show();
            return;
        }

        int deliveryType = rgDelivery.getCheckedRadioButtonId() == R.id.rb_delivery ? 0 : 1;

        // 获取选中的标签
        String selectedTag = "生鲜食材";
        int checkedId = rgTag.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_tag_daily) {
            selectedTag = "日用百货";
        } else if (checkedId == R.id.rb_tag_snack) {
            selectedTag = "零食饮品";
        }

        // 调用优化后的预览弹窗
        showPreviewDialog(name, desc, priceJson, deliveryType, selectedTag);
    }

    // ==========================================
    // 重点修改区域：优化后的确认面板
    // ==========================================
    private void showPreviewDialog(String name, String desc, JSONArray priceJson, int deliveryType, String tag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 注意：请确保你的 dialog_product_preview.xml 布局文件中包含了以下 ID 的 TextView
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_product_preview, null);

        TextView tvName = view.findViewById(R.id.tv_preview_name);
        TextView tvDesc = view.findViewById(R.id.tv_preview_desc);        // 需要在XML中添加
        TextView tvPrice = view.findViewById(R.id.tv_preview_price);
        TextView tvDelivery = view.findViewById(R.id.tv_preview_delivery); // 需要在XML中添加
        TextView tvTag = view.findViewById(R.id.tv_preview_tag);           // 需要在XML中添加

        // 设置基本信息
        tvName.setText("名称：" + name);
        tvDesc.setText("详情：" + (desc.isEmpty() ? "暂无描述" : desc));
        tvTag.setText("标签：" + tag);
        tvDelivery.setText("配送：" + (deliveryType == 0 ? "商家配送" : "用户自提"));

        // 构建价格表显示字符串（显示所有行）
        StringBuilder priceSb = new StringBuilder();
        priceSb.append("价格表：\n");
        try {
            for (int i = 0; i < priceJson.length(); i++) {
                JSONObject obj = priceJson.getJSONObject(i);
                priceSb.append("  • ").append(obj.getString("desc"))
                        .append(": ¥").append(obj.getString("price")).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tvPrice.setText(priceSb.toString());

        builder.setView(view)
                .setTitle("确认发布信息") // 建议增加标题
                .setPositiveButton(mEditingProduct != null ? "确认修改" : "确认发布", (dialog, which) -> {
                    saveToDb(name, desc, priceJson.toString(), deliveryType, tag);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void saveToDb(String name, String desc, String jsonPrice, int deliveryType, String tag) {
        final boolean isUpdate = (mEditingProduct != null);

        new Thread(() -> {
            Product product;

            if (isUpdate) {
                product = mEditingProduct;
            } else {
                product = new Product();
                product.createTime = DateUtils.getCurrentDateTime();
                product.merchantId = 1; // 实际开发中应获取当前登录商家ID
                product.type = "GOODS";
            }

            product.name = name;
            product.description = desc;
            product.priceTableJson = jsonPrice;
            product.deliveryMethod = deliveryType;
            product.tag = tag; // 保存标签

            try {
                JSONArray ja = new JSONArray(jsonPrice);
                if (ja.length() > 0) product.price = ja.getJSONObject(0).getString("price");
            } catch (Exception e) {
                e.printStackTrace();
            }

            StringBuilder sb = new StringBuilder();
            for (String s : selectedImagePaths) {
                sb.append(s).append(",");
            }
            if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
            product.imagePaths = sb.toString();
            product.coverImage = product.getFirstImage();

            if (isUpdate) {
                AppDatabase.getInstance(this).productDao().update(product);
            } else {
                AppDatabase.getInstance(this).productDao().insert(product);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, isUpdate ? "修改成功" : "发布成功", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}
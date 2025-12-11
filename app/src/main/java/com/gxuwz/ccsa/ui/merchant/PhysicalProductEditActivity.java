package com.gxuwz.ccsa.ui.merchant;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    private List<String> selectedImagePaths = new ArrayList<>();
    private ImageView ivAddImage;

    // --- 新增：保存正在编辑的商品对象 ---
    private Product mEditingProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physical_product_edit);

        initView();

        // --- 修改：判断是新增还是编辑 ---
        if (getIntent().hasExtra("product")) {
            mEditingProduct = (Product) getIntent().getSerializableExtra("product");
            // 如果有数据，则填充 UI（进入编辑模式）
            initDataFromProduct();
        } else {
            // 如果没有数据，则是新增模式，执行默认逻辑（默认添加3行空价格表）
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
        Button btnPublish = findViewById(R.id.btn_publish);
        ImageView btnAddPriceRow = findViewById(R.id.btn_add_price_row);

        ivAddImage.setOnClickListener(v -> checkPermissionAndPickImage());
        btnAddPriceRow.setOnClickListener(v -> addPriceRow());
        btnPublish.setOnClickListener(v -> attemptPublish());
    }

    /**
     * 新增：从 Product 对象回显数据到界面
     */
    private void initDataFromProduct() {
        // 1. 回显文本信息
        etName.setText(mEditingProduct.name);
        etDesc.setText(mEditingProduct.description);

        // 2. 回显配送方式
        if (mEditingProduct.deliveryMethod == 0) {
            rgDelivery.check(R.id.rb_delivery);
        } else {
            rgDelivery.check(R.id.rb_self_pick);
        }

        // 3. 回显图片
        if (mEditingProduct.imagePaths != null && !mEditingProduct.imagePaths.isEmpty()) {
            String[] paths = mEditingProduct.imagePaths.split(",");
            for (String path : paths) {
                // 简单的防空判断
                if (!path.trim().isEmpty()) {
                    selectedImagePaths.add(path);
                }
            }
            renderImages(); // 复用已有的渲染方法
        }

        // 4. 回显价格表
        llPriceTableContainer.removeAllViews(); // 清除界面初始化时可能存在的默认行
        try {
            JSONArray jsonArray = new JSONArray(mEditingProduct.priceTableJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                addPriceRowWithData(obj.optString("desc"), obj.optString("price"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // 如果解析失败，至少显示一行空的
            addPriceRow();
        }
    }

    /**
     * 默认添加空行
     */
    private void addPriceRow() {
        View rowView = LayoutInflater.from(this).inflate(R.layout.item_price_table_row_edit, llPriceTableContainer, false);
        llPriceTableContainer.addView(rowView);
    }

    /**
     * 新增：添加带有数据的价格行（用于编辑回显）
     */
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            openGallery();
        }
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
        // 重新添加"添加按钮"
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

            // 将图片添加到"添加按钮"之前
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
        showPreviewDialog(name, desc, priceJson, deliveryType);
    }

    private void showPreviewDialog(String name, String desc, JSONArray priceJson, int deliveryType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_product_preview, null);

        TextView tvName = view.findViewById(R.id.tv_preview_name);
        TextView tvPrice = view.findViewById(R.id.tv_preview_price);

        tvName.setText(name);
        try {
            if (priceJson.length() > 0) {
                JSONObject first = priceJson.getJSONObject(0);
                tvPrice.setText(first.getString("desc") + " ¥" + first.getString("price"));
            }
        } catch (Exception e) {}

        builder.setView(view)
                .setPositiveButton(mEditingProduct != null ? "确认修改" : "确认发布", (dialog, which) -> {
                    saveToDb(name, desc, priceJson.toString(), deliveryType);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // --- 修改：区分新增和更新逻辑 ---
    private void saveToDb(String name, String desc, String jsonPrice, int deliveryType) {
        new Thread(() -> {
            Product product;
            boolean isUpdate = false;

            if (mEditingProduct != null) {
                // 编辑模式：复用原有对象（关键是保留 id），只更新字段
                product = mEditingProduct;
                isUpdate = true;
            } else {
                // 新增模式：创建新对象
                product = new Product();
                product.createTime = DateUtils.getCurrentDateTime();
                product.merchantId = 1; // 实际开发中应从 User Session 获取
                product.type = "GOODS";
                isUpdate = false;
            }

            // 更新/设置通用字段
            product.name = name;
            product.description = desc;
            product.priceTableJson = jsonPrice;
            product.deliveryMethod = deliveryType;

            // 兼容旧逻辑：设置 price 字段为第一行价格
            try {
                JSONArray ja = new JSONArray(jsonPrice);
                if (ja.length() > 0) product.price = ja.getJSONObject(0).getString("price");
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 处理图片路径
            StringBuilder sb = new StringBuilder();
            for (String s : selectedImagePaths) {
                sb.append(s).append(",");
            }
            if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
            product.imagePaths = sb.toString();

            // 兼容封面图
            product.coverImage = product.getFirstImage();

            // --- 核心修改：区分 Update 和 Insert ---
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
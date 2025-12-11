package com.gxuwz.ccsa.ui.merchant;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup; // 解决 Cannot resolve symbol 'ViewGroup'
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 使用 activity_physical_product_edit 布局
        setContentView(R.layout.activity_physical_product_edit);

        initView();
        addPriceRow();
        addPriceRow();
        addPriceRow();
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

    private void addPriceRow() {
        // item_price_table_row_edit 必须存在于 layout 文件夹
        View rowView = LayoutInflater.from(this).inflate(R.layout.item_price_table_row_edit, llPriceTableContainer, false);
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
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
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
            // item_image_preview_small 已经在第四步补充
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_image_preview_small, llImageContainer, false);
            ImageView iv = itemView.findViewById(R.id.iv_image);
            ImageView btnDel = itemView.findViewById(R.id.btn_delete); // 解决 Cannot resolve symbol 'btn_delete'

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
                String itemText = etItem.getText().toString().trim();
                String priceVal = etPrice.getText().toString().trim();

                if (!itemText.isEmpty() && !priceVal.isEmpty()) {
                    JSONObject obj = new JSONObject();
                    obj.put("desc", itemText);
                    obj.put("price", priceVal);
                    priceJson.put(obj);
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
        // dialog_product_preview 已经在第四步补充
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
                .setPositiveButton("确认发布", (dialog, which) -> {
                    saveToDb(name, desc, priceJson.toString(), deliveryType);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void saveToDb(String name, String desc, String jsonPrice, int deliveryType) {
        new Thread(() -> {
            Product product = new Product();
            product.name = name;
            product.description = desc;
            product.priceTableJson = jsonPrice;
            // 兼容字段，保存第一行价格到 old price 字段
            try {
                JSONArray ja = new JSONArray(jsonPrice);
                if (ja.length()>0) product.price = ja.getJSONObject(0).getString("price");
            } catch(Exception e){}

            product.deliveryMethod = deliveryType;
            product.type = "GOODS";
            product.merchantId = 1;
            // DateUtils.getCurrentDateTime() 已经在第一步补充
            product.createTime = DateUtils.getCurrentDateTime();

            StringBuilder sb = new StringBuilder();
            for (String s : selectedImagePaths) sb.append(s).append(",");
            if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
            product.imagePaths = sb.toString();
            // 兼容字段
            product.coverImage = product.getFirstImage();

            AppDatabase.getInstance(this).productDao().insert(product);

            runOnUiThread(() -> {
                Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}
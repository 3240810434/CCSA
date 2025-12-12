package com.gxuwz.ccsa.ui.merchant;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
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

public class ServiceEditActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 102;

    private EditText etName, etDesc, etPrice;
    // 删除了 etExtraFeeNote
    private LinearLayout llImageContainer;
    private ImageView ivAddImage;
    private Spinner spinnerUnit;
    private RadioGroup rgServiceType, rgServiceTag;

    private List<String> selectedImagePaths = new ArrayList<>();

    // 新增：保存正在编辑的对象
    private Product mEditingProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_edit);
        initView();

        // 修复：添加数据回显逻辑
        if (getIntent().hasExtra("product")) {
            mEditingProduct = (Product) getIntent().getSerializableExtra("product");
            initDataFromProduct();
        }
    }

    private void initView() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        etName = findViewById(R.id.et_name);
        etDesc = findViewById(R.id.et_desc);
        etPrice = findViewById(R.id.et_price);
        // 删除 etExtraFeeNote 查找

        llImageContainer = findViewById(R.id.ll_image_container);
        ivAddImage = findViewById(R.id.iv_add_image);

        spinnerUnit = findViewById(R.id.spinner_unit);
        rgServiceType = findViewById(R.id.rg_service_type);
        rgServiceTag = findViewById(R.id.rg_service_tag);

        Button btnPublish = findViewById(R.id.btn_publish);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"次", "小时", "天"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(adapter);

        ivAddImage.setOnClickListener(v -> checkPermissionAndPickImage());
        btnPublish.setOnClickListener(v -> attemptPublish());
    }

    // 修复：实现数据回显
    private void initDataFromProduct() {
        etName.setText(mEditingProduct.name);
        etDesc.setText(mEditingProduct.description);
        etPrice.setText(mEditingProduct.price);

        // 回显图片
        if (mEditingProduct.imagePaths != null && !mEditingProduct.imagePaths.isEmpty()) {
            String[] paths = mEditingProduct.imagePaths.split(",");
            for (String path : paths) {
                if (!path.trim().isEmpty()) {
                    selectedImagePaths.add(path);
                }
            }
            renderImages();
        }

        // 回显单位
        String unit = mEditingProduct.unit;
        if (unit != null) {
            ArrayAdapter adapter = (ArrayAdapter) spinnerUnit.getAdapter();
            int pos = adapter.getPosition(unit);
            if (pos >= 0) spinnerUnit.setSelection(pos);
        }

        // 回显标签
        if (mEditingProduct.tag != null) {
            String tag = mEditingProduct.tag;
            if (tag.equals("保洁服务")) rgServiceTag.check(R.id.rb_tag_clean);
            else if (tag.equals("维修服务")) rgServiceTag.check(R.id.rb_tag_repair);
            else if (tag.equals("家政帮手")) rgServiceTag.check(R.id.rb_tag_housework);
            else if (tag.equals("便民代办")) rgServiceTag.check(R.id.rb_tag_errand);
        }

        // 回显类型 (需要解析 priceTableJson 中的 mode 字段)
        try {
            JSONArray ja = new JSONArray(mEditingProduct.priceTableJson);
            if (ja.length() > 0) {
                JSONObject obj = ja.getJSONObject(0);
                String mode = obj.optString("mode");
                if ("上门服务".equals(mode)) rgServiceType.check(R.id.rb_type_door);
                else if ("到店服务".equals(mode)) rgServiceType.check(R.id.rb_type_shop);
                else if ("线上咨询".equals(mode)) rgServiceType.check(R.id.rb_type_online);
            }
        } catch (Exception e) {}
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
        String priceStr = etPrice.getText().toString().trim();
        String unit = spinnerUnit.getSelectedItem().toString();

        if (name.isEmpty() || desc.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "请完善服务名称、描述及价格", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImagePaths.isEmpty()) {
            Toast.makeText(this, "请至少上传一张服务展示图", Toast.LENGTH_SHORT).show();
            return;
        }

        int typeId = rgServiceType.getCheckedRadioButtonId();
        String serviceMode = "上门服务";
        if (typeId != -1) {
            RadioButton rbMode = findViewById(typeId);
            if (rbMode != null) serviceMode = rbMode.getText().toString();
        }

        int tagId = rgServiceTag.getCheckedRadioButtonId();
        String serviceTag = "便民服务";
        if (tagId != -1) {
            RadioButton rbTag = findViewById(tagId);
            if (rbTag != null) {
                serviceTag = rbTag.getText().toString();
            }
        }

        JSONArray priceJson = new JSONArray();
        JSONObject obj = new JSONObject();
        try {
            obj.put("desc", "基础服务费");
            obj.put("price", priceStr);
            obj.put("unit", unit);
            obj.put("mode", serviceMode);
            obj.put("tag", serviceTag);
            priceJson.put(obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 移除了 extraFee 参数
        showPreviewDialog(name, desc, priceJson, serviceMode, serviceTag, priceStr, unit);
    }

    private void showPreviewDialog(String name, String desc, JSONArray priceJson,
                                   String mode, String tag, String price, String unit) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_service_preview, null);

        TextView tvName = view.findViewById(R.id.tv_preview_name);
        TextView tvDesc = view.findViewById(R.id.tv_preview_desc);
        TextView tvTag = view.findViewById(R.id.tv_preview_tag);
        TextView tvMode = view.findViewById(R.id.tv_preview_mode);
        TextView tvPrice = view.findViewById(R.id.tv_preview_price);

        tvName.setText("名称：" + name);
        tvDesc.setText("详情：" + desc);
        tvTag.setText("标签：" + tag);
        tvMode.setText("方式：" + mode);
        tvPrice.setText("价格：¥" + price + " / " + unit);

        builder.setView(view)
                .setPositiveButton(mEditingProduct != null ? "确认修改" : "确认发布", (dialog, which) -> {
                    saveToDb(name, desc, priceJson.toString(), price, tag, unit);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // 增加 unit 参数
    private void saveToDb(String name, String desc, String jsonPrice, String priceVal, String tag, String unit) {
        final boolean isUpdate = (mEditingProduct != null);
        new Thread(() -> {
            Product product;
            if (isUpdate) {
                product = mEditingProduct;
            } else {
                product = new Product();
                product.createTime = DateUtils.getCurrentDateTime();
                product.merchantId = 1;
                product.type = "SERVICE";
            }

            product.name = name;
            product.description = desc;
            product.priceTableJson = jsonPrice;
            product.price = priceVal;
            product.deliveryMethod = 0;
            product.tag = tag;
            product.unit = unit; // 保存单位

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
package com.gxuwz.ccsa.ui.merchant;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
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

public class ServiceEditActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 102; // 唯一的请求码

    private EditText etName, etDesc, etPrice, etExtraFeeNote;
    private LinearLayout llImageContainer;
    private ImageView ivAddImage;
    private Spinner spinnerUnit;
    private RadioGroup rgServiceType, rgServiceTag;

    // 存储选中的图片路径
    private List<String> selectedImagePaths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_edit);

        initView();
    }

    private void initView() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        etName = findViewById(R.id.et_name);
        etDesc = findViewById(R.id.et_desc);
        etPrice = findViewById(R.id.et_price);
        etExtraFeeNote = findViewById(R.id.et_extra_fee_note);

        llImageContainer = findViewById(R.id.ll_image_container);
        ivAddImage = findViewById(R.id.iv_add_image);

        spinnerUnit = findViewById(R.id.spinner_unit);
        rgServiceType = findViewById(R.id.rg_service_type);
        rgServiceTag = findViewById(R.id.rg_service_tag);

        Button btnPublish = findViewById(R.id.btn_publish);

        // 初始化计价单位下拉框
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"次", "小时", "天"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(adapter);

        ivAddImage.setOnClickListener(v -> checkPermissionAndPickImage());
        btnPublish.setOnClickListener(v -> attemptPublish());
    }

    // --- 图片选择逻辑 (复用自 PhysicalProductEditActivity) ---

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
                    // 获取持久化权限，防止重启后图片无法加载
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
        // 始终保留添加按钮
        llImageContainer.addView(ivAddImage);

        for (String path : selectedImagePaths) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_image_preview_small, llImageContainer, false);
            ImageView iv = itemView.findViewById(R.id.iv_image);
            ImageView btnDel = itemView.findViewById(R.id.btn_delete);

            Glide.with(this).load(path).into(iv);
            btnDel.setOnClickListener(v -> {
                selectedImagePaths.remove(path);
                renderImages(); // 重新渲染
            });

            // 将图片插在添加按钮之前
            llImageContainer.addView(itemView, llImageContainer.getChildCount() - 1);
        }
    }

    // --- 发布逻辑 ---

    private void attemptPublish() {
        String name = etName.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String extraFee = etExtraFeeNote.getText().toString().trim();
        String unit = spinnerUnit.getSelectedItem().toString();

        if (name.isEmpty() || desc.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "请完善服务名称、描述及价格", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImagePaths.isEmpty()) {
            Toast.makeText(this, "请至少上传一张服务展示图", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取服务类型
        int typeId = rgServiceType.getCheckedRadioButtonId();
        String serviceMode = "上门服务";
        if (typeId == R.id.rb_type_shop) serviceMode = "到店服务";
        else if (typeId == R.id.rb_type_online) serviceMode = "线上咨询";

        // 获取服务标签
        int tagId = rgServiceTag.getCheckedRadioButtonId();
        String serviceTag = "便民服务";
        RadioButton rbTag = findViewById(tagId);
        if (rbTag != null) {
            serviceTag = rbTag.getText().toString();
        }

        // 构建 JSON 数据
        // 为了兼容 ProductManagementActivity 的列表显示，我们构造一个类似的结构
        // [{"desc":"基础服务费", "price":"50", "unit":"次", "mode":"上门", "tag":"保洁", "note":"..."}]
        JSONArray priceJson = new JSONArray();
        JSONObject obj = new JSONObject();
        try {
            obj.put("desc", "基础服务费"); // 固定描述，用于列表显示兼容
            obj.put("price", priceStr);
            obj.put("unit", unit);
            obj.put("note", extraFee);
            obj.put("mode", serviceMode);
            obj.put("tag", serviceTag);
            priceJson.put(obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        showPreviewDialog(name, desc, priceJson, serviceMode, serviceTag, priceStr, unit, extraFee);
    }

    private void showPreviewDialog(String name, String desc, JSONArray priceJson,
                                   String mode, String tag, String price, String unit, String note) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_service_preview, null);

        TextView tvName = view.findViewById(R.id.tv_preview_name);
        TextView tvTag = view.findViewById(R.id.tv_preview_tag);
        TextView tvMode = view.findViewById(R.id.tv_preview_mode);
        TextView tvPrice = view.findViewById(R.id.tv_preview_price);
        TextView tvNote = view.findViewById(R.id.tv_preview_note);

        tvName.setText(name);
        tvTag.setText(tag);
        tvMode.setText("服务方式：" + mode);
        tvPrice.setText("¥" + price + " / " + unit);

        if (!note.isEmpty()) {
            tvNote.setText("备注：" + note);
            tvNote.setVisibility(View.VISIBLE);
        } else {
            tvNote.setVisibility(View.GONE);
        }

        builder.setView(view)
                .setPositiveButton("确认发布", (dialog, which) -> {
                    saveToDb(name, desc, priceJson.toString(), price);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void saveToDb(String name, String desc, String jsonPrice, String priceVal) {
        new Thread(() -> {
            Product product = new Product();
            product.createTime = DateUtils.getCurrentDateTime();
            product.merchantId = 1; // 默认商家ID
            product.type = "SERVICE"; // 标记为服务类型

            product.name = name;
            product.description = desc;
            product.priceTableJson = jsonPrice;
            product.price = priceVal; // 存储基础价格供排序或简单显示
            product.deliveryMethod = 0; // 服务类此字段可默认填0，具体逻辑由JSON中的mode决定

            // 处理图片路径
            StringBuilder sb = new StringBuilder();
            for (String s : selectedImagePaths) {
                sb.append(s).append(",");
            }
            if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
            product.imagePaths = sb.toString();
            product.coverImage = product.getFirstImage();

            AppDatabase.getInstance(this).productDao().insert(product);

            runOnUiThread(() -> {
                Toast.makeText(this, "服务发布成功", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}
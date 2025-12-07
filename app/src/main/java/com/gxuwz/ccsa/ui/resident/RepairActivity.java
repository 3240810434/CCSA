package com.gxuwz.ccsa.ui.resident;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.ImageGridAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Repair;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.util.ImageUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RepairActivity extends AppCompatActivity implements ImageGridAdapter.OnItemClickListener {
    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;
    private static final int PERMISSION_REQUEST_CODE = 200;

    private User currentUser;
    private EditText etTitle, etDescription;
    private TextView tvName, tvPhone, tvEditContact;
    private RecyclerView rvImages;
    private Button btnSubmit;
    private ImageGridAdapter imageAdapter;

    private String tempContactName;
    private String tempContactPhone;
    private AlertDialog loadingDialog; // 加载对话框

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repair);

        // 获取当前用户
        currentUser = (User) getIntent().getSerializableExtra("user");
        if (currentUser == null) {
            Toast.makeText(this, "用户信息获取失败，请重新登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化临时联系信息
        tempContactName = currentUser.getName();
        tempContactPhone = currentUser.getPhone();

        initViews();
        setupListeners();
        checkPermissions();
    }

    private void initViews() {
        // 顶部导航
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("报修");

        findViewById(R.id.btn_my_repairs).setOnClickListener(v -> {
            Intent intent = new Intent(this, MyRepairsActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });

        // 表单内容
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);

        // 联系人信息
        tvName = findViewById(R.id.tv_name);
        tvPhone = findViewById(R.id.tv_phone);
        tvEditContact = findViewById(R.id.tv_edit_contact);

        tvName.setText("姓名：" + tempContactName);
        tvPhone.setText("电话：" + tempContactPhone);

        // 图片上传区域
        rvImages = findViewById(R.id.rv_images);
        rvImages.setLayoutManager(new GridLayoutManager(this, 3));
        imageAdapter = new ImageGridAdapter(this, this);
        rvImages.setAdapter(imageAdapter);

        // 提交按钮
        btnSubmit = findViewById(R.id.btn_submit);
        btnSubmit.setEnabled(false); // 初始禁用
    }

    private void setupListeners() {
        // 编辑联系人信息
        tvEditContact.setOnClickListener(v -> showEditContactDialog());

        // 监听输入变化，启用/禁用提交按钮
        etTitle.setOnFocusChangeListener((v, hasFocus) -> checkSubmitEnabled());
        etDescription.setOnFocusChangeListener((v, hasFocus) -> checkSubmitEnabled());
        etTitle.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkSubmitEnabled();
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        etDescription.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkSubmitEnabled();
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // 提交按钮点击事件
        btnSubmit.setOnClickListener(v -> submitRepair());
    }

    // 检查提交按钮是否可点击
    private void checkSubmitEnabled() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        btnSubmit.setEnabled(!title.isEmpty() && !description.isEmpty());
        // 动态改变按钮颜色
        btnSubmit.setBackgroundTintList(ContextCompat.getColorStateList(this,
                btnSubmit.isEnabled() ? R.color.blue : R.color.gray));
    }

    // 显示编辑联系人对话框
    private void showEditContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_contact, null);
        builder.setView(view);

        EditText etName = view.findViewById(R.id.et_name);
        EditText etPhone = view.findViewById(R.id.et_phone);

        etName.setText(tempContactName);
        etPhone.setText(tempContactPhone);

        builder.setTitle("编辑联系人信息")
                .setPositiveButton("确定", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();

                    if (!name.isEmpty() && !phone.isEmpty()) {
                        tempContactName = name;
                        tempContactPhone = phone;
                        tvName.setText("姓名：" + tempContactName);
                        tvPhone.setText("电话：" + tempContactPhone);
                    } else {
                        Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null);

        builder.show();
    }

    // 检查权限
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    // 权限请求结果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已授予
            } else {
                Toast.makeText(this, "需要相机和存储权限才能上传图片", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 图片添加点击事件
    @Override
    public void onAddClick() {
        showImageSourceDialog();
    }

    // 图片删除点击事件
    @Override
    public void onDeleteClick(int position) {
        imageAdapter.removeImage(position);
    }

    // 显示图片来源选择对话框
    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择图片来源")
                .setItems(new String[]{"拍照", "从相册选择"}, (dialog, which) -> {
                    if (which == 0) {
                        // 拍照
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, REQUEST_CAMERA);
                    } else {
                        // 从相册选择
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, REQUEST_GALLERY);
                    }
                });
        builder.show();
    }

    // 修复方法名后的onActivityResult方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA && data != null) {
                // 处理拍照结果
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null) {
                        // 保存图片并添加到适配器（修正方法名）
                        String imagePath = ImageUtils.saveImageToExternalStorage(imageBitmap, this);
                        if (imagePath != null) {
                            imageAdapter.addImage(imagePath);
                        } else {
                            Toast.makeText(this, "图片保存失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } else if (requestCode == REQUEST_GALLERY && data != null) {
                // 处理相册选择结果
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    try {
                        // 改用原有的getBitmapFromUri方法，避免兼容性问题
                        Bitmap bitmap = ImageUtils.getBitmapFromUri(selectedImage, this);
                        // 修正方法名
                        String imagePath = ImageUtils.saveImageToExternalStorage(bitmap, this);
                        if (imagePath != null) {
                            imageAdapter.addImage(imagePath);
                        } else {
                            Toast.makeText(this, "图片处理失败", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "图片获取失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    // 提交报修信息
    private void submitRepair() {
        // 显示加载对话框
        showLoadingDialog();

        // 获取表单数据
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // 确保从当前用户获取小区信息
        String community = currentUser.getCommunity(); // 关键修改：添加小区信息
        String building = currentUser.getBuilding();   // 获取楼栋信息
        String room = currentUser.getRoom();           // 获取房间号

        // 生成报修单号
        String repairNo = generateRepairNo();

        // 处理图片
        List<String> imagePaths = imageAdapter.getImagePaths();
        String imageUrls = imagePaths.isEmpty() ? "" : String.join(",", imagePaths);

        // 创建报修对象
        Repair repair = new Repair();
        repair.setRepairNo(repairNo);
        repair.setTitle(title);
        repair.setDescription(description);
        repair.setUserName(tempContactName);
        repair.setUserPhone(tempContactPhone);
        repair.setUserId(currentUser.getPhone());
        repair.setImageUrls(imageUrls);
        repair.setSubmitTime(new Date().getTime());
        repair.setStatus(0); // 0-待处理，1-已完成
        repair.setCommunity(community); // 关键修改：设置小区信息
        repair.setBuilding(building);   // 设置楼栋信息
        repair.setRoom(room);           // 设置房间信息

        // 保存到数据库
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                AppDatabase.getInstance(this).repairDao().insert(repair);

                runOnUiThread(() -> {
                    dismissLoadingDialog();
                    Toast.makeText(this, "报修提交成功", Toast.LENGTH_SHORT).show();
                    finish(); // 返回上一页
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    dismissLoadingDialog();
                    Toast.makeText(this, "提交失败，请重试", Toast.LENGTH_SHORT).show();
                });
            } finally {
                executor.shutdown(); // 关闭线程池，避免内存泄漏
            }
        });
    }

    // 生成报修单号
    private String generateRepairNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String date = sdf.format(new Date());
        // 可以添加随机数或自增ID使单号唯一
        return "BX" + date + System.currentTimeMillis() % 1000;
    }

    // 显示加载对话框
    private void showLoadingDialog() {
        if (loadingDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(R.layout.dialog_loading);
            builder.setCancelable(false);
            loadingDialog = builder.create();
        }
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    // 关闭加载对话框
    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    // 防止内存泄漏，页面销毁时清理对话框
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissLoadingDialog();
        loadingDialog = null;
    }
}
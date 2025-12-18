package com.gxuwz.ccsa.ui.resident;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.ProductReview;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class PublishReviewActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SELECT_IMAGE = 101;
    private static final int PERMISSION_REQUEST_CODE = 102;
    private static final int MAX_IMAGE_COUNT = 6; // 修改要求：最多6张图片

    private RatingBar ratingBar;
    private TextView tvScoreValue;
    private EditText etContent;
    private RecyclerView recyclerImages;
    private Button btnSubmit;
    private ImageView btnBack;

    private ImageAdapter imageAdapter;
    private List<Uri> selectedImages = new ArrayList<>();
    private int productId;
    private long orderId;
    private int score = 0; // 0-10

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_review);

        productId = getIntent().getIntExtra("product_id", -1);
        orderId = getIntent().getLongExtra("order_id", -1);

        if (productId == -1) {
            Toast.makeText(this, "商品信息错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initView();
        setupListeners();
    }

    private void initView() {
        btnBack = findViewById(R.id.btn_back);
        ratingBar = findViewById(R.id.rating_bar);
        tvScoreValue = findViewById(R.id.tv_score_value);
        etContent = findViewById(R.id.et_content);
        recyclerImages = findViewById(R.id.recycler_images);
        btnSubmit = findViewById(R.id.btn_submit);

        // 设置图片网格布局，发布页面每行显示4张
        recyclerImages.setLayoutManager(new GridLayoutManager(this, 4));
        imageAdapter = new ImageAdapter();
        recyclerImages.setAdapter(imageAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // 评分监听：1颗星=2分
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            score = (int) (rating * 2);
            tvScoreValue.setText(score + "分");
        });

        // 提交监听
        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        if (score == 0) {
            Toast.makeText(this, "请点亮星星进行评分", Toast.LENGTH_SHORT).show();
            return;
        }
        String content = etContent.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "请输入评价内容", Toast.LENGTH_SHORT).show();
            return;
        }

        // 异步提交到数据库
        new Thread(() -> {
            int userId = SharedPreferencesUtil.getUserId(this);
            AppDatabase db = AppDatabase.getInstance(this);
            User user = db.userDao().getUserById(userId);

            // 处理图片路径
            StringBuilder sb = new StringBuilder();
            for (Uri uri : selectedImages) {
                sb.append(uri.toString()).append(",");
            }
            String imagePathStr = sb.toString();
            if (imagePathStr.endsWith(",")) {
                imagePathStr = imagePathStr.substring(0, imagePathStr.length() - 1);
            }

            ProductReview review = new ProductReview(
                    productId,
                    userId,
                    user != null ? user.getUsername() : "匿名用户",
                    user != null ? user.getAvatar() : "",
                    score,
                    content,
                    imagePathStr,
                    System.currentTimeMillis()
            );

            // 1. 插入评价
            db.productReviewDao().insert(review);

            // 2. 更新订单状态为已评价 (状态 1)
            if (orderId != -1) {
                db.orderDao().updateReviewStatus(orderId, 1);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "评价发表成功！", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    // 图片选择适配器
    private class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_ADD = 0;
        private static final int TYPE_ITEM = 1;

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_preview_small, parent, false);
            return new ImageHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ImageHolder imageHolder = (ImageHolder) holder;
            if (getItemViewType(position) == TYPE_ADD) {
                // 设置为相册图标
                imageHolder.img.setImageResource(R.drawable.photo_album);
                imageHolder.itemView.setOnClickListener(v -> checkPermissionAndSelectImage());
                // 添加按钮不显示删除键
                if (imageHolder.btnDelete != null) imageHolder.btnDelete.setVisibility(View.GONE);
            } else {
                Uri uri = selectedImages.get(position);
                Glide.with(PublishReviewActivity.this).load(uri).into(imageHolder.img);

                // 显示删除按钮并设置点击事件
                if (imageHolder.btnDelete != null) {
                    imageHolder.btnDelete.setVisibility(View.VISIBLE);
                    imageHolder.btnDelete.setOnClickListener(v -> {
                        int pos = imageHolder.getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            selectedImages.remove(pos);
                            notifyItemRemoved(pos);
                            notifyItemRangeChanged(pos, getItemCount());
                        }
                    });
                }
                imageHolder.itemView.setOnClickListener(null);
            }
        }

        @Override
        public int getItemCount() {
            // 限制最多显示 MAX_IMAGE_COUNT 张图片
            return selectedImages.size() < MAX_IMAGE_COUNT ? selectedImages.size() + 1 : MAX_IMAGE_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            // 如果满了 MAX_IMAGE_COUNT 张，就不显示加号了，全是 ITEM
            if (selectedImages.size() >= MAX_IMAGE_COUNT) return TYPE_ITEM;
            if (position == selectedImages.size()) return TYPE_ADD;
            return TYPE_ITEM;
        }

        class ImageHolder extends RecyclerView.ViewHolder {
            ImageView img;
            ImageView btnDelete;

            ImageHolder(View itemView) {
                super(itemView);
                img = itemView.findViewById(R.id.iv_image);
                btnDelete = itemView.findViewById(R.id.btn_delete);
            }
        }
    }

    private void checkPermissionAndSelectImage() {
        String permission = Build.VERSION.SDK_INT >= 33 ?
                Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            Toast.makeText(this, "需要相册权限才能选择图片", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            try {
                // 【修复】直接使用明确的常量 Intent.FLAG_GRANT_READ_URI_PERMISSION
                // 解决 "Must be one or more of..." 报错，并且只申请读权限，防止因申请写权限导致的崩溃
                int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;

                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        if (selectedImages.size() < MAX_IMAGE_COUNT) {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            // 安全检查：确认返回的 Intent 确实包含读权限 flag
                            if ((data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0) {
                                getContentResolver().takePersistableUriPermission(uri, takeFlags);
                            }
                            selectedImages.add(uri);
                        }
                    }
                } else if (data.getData() != null) {
                    if (selectedImages.size() < MAX_IMAGE_COUNT) {
                        Uri uri = data.getData();
                        if ((data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0) {
                            getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        }
                        selectedImages.add(uri);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 异常处理：即使 takePersistableUriPermission 失败，也尝试将 URI 加入列表，Glide 可能仍能短期加载
            }

            imageAdapter.notifyDataSetChanged();
        }
    }
}
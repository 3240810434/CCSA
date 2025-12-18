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

    private RatingBar ratingBar;
    private TextView tvScoreValue;
    private EditText etContent;
    private RecyclerView recyclerImages;
    private Button btnSubmit;
    private ImageView btnBack;

    private ImageAdapter imageAdapter;
    private List<Uri> selectedImages = new ArrayList<>();
    private int productId;
    private int score = 0; // 0-10

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_review);

        productId = getIntent().getIntExtra("product_id", -1);
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

        // 设置图片网格布局
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

            // 处理图片路径，将Uri转换为String拼接
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

            db.productReviewDao().insert(review);

            runOnUiThread(() -> {
                Toast.makeText(this, "评价发表成功！", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    // 图片选择适配器（含添加按钮）
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
                imageHolder.img.setImageResource(R.drawable.ic_add_photo); // 请确保有此图标
                imageHolder.itemView.setOnClickListener(v -> checkPermissionAndSelectImage());
            } else {
                Uri uri = selectedImages.get(position);
                Glide.with(PublishReviewActivity.this).load(uri).into(imageHolder.img);
                imageHolder.itemView.setOnClickListener(null);
            }
        }

        @Override
        public int getItemCount() {
            return selectedImages.size() < 9 ? selectedImages.size() + 1 : 9;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == selectedImages.size()) return TYPE_ADD;
            return TYPE_ITEM;
        }

        class ImageHolder extends RecyclerView.ViewHolder {
            ImageView img;
            ImageHolder(View itemView) {
                super(itemView);
                img = itemView.findViewById(R.id.iv_image); // 对应 item_image_preview_small.xml 中的 ID
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
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    selectedImages.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                selectedImages.add(data.getData());
            }
            imageAdapter.notifyDataSetChanged();
        }
    }
}
package com.gxuwz.ccsa.ui.merchant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Merchant;

import java.util.concurrent.Executors;

// 假设 MerchantQualificationActivity 在同一个包或已正确导入
// 如果不在同一个包，请根据实际路径 import
// import com.gxuwz.ccsa.ui.merchant.MerchantQualificationActivity;

public class MerchantProfileFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvMerchantName;
    private Merchant currentMerchant;

    // 图片选择启动器
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    // 临时保存用户选择的图片URI（在点击保存前）
    private Uri tempSelectedImageUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化图片选择回调
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            // 1. 持久化权限（防止重启后无法读取）
                            try {
                                requireContext().getContentResolver().takePersistableUriPermission(
                                        imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            // 2. 更新UI预览
                            updateAvatarUI(imageUri.toString());
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_merchant_profile, container, false);

        // 获取当前商家用户
        if (getActivity() instanceof MerchantMainActivity) {
            currentMerchant = ((MerchantMainActivity) getActivity()).getCurrentMerchant();
        }

        initViews(view);
        setupListeners(view);
        loadMerchantData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 页面恢复可见时（例如从资质页面返回），刷新数据
        if (currentMerchant != null) {
            Executors.newSingleThreadExecutor().execute(() -> {
                // 从数据库重新获取最新的 Merchant 信息
                // 注意：这里假设 Merchant 类有 getId() 方法
                Merchant updated = AppDatabase.getInstance(getContext())
                        .merchantDao()
                        .findById(currentMerchant.getId());

                if (updated != null) {
                    currentMerchant = updated;
                    // 如果需要在主线程更新UI（例如状态变更显示），请在此处 runOnUiThread
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            loadMerchantData(); // 重新加载数据显示
                        });
                    }
                }
            });
        }
    }

    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvMerchantName = view.findViewById(R.id.tv_merchant_name);
    }

    private void setupListeners(View view) {
        // 编辑主页点击事件
        View.OnClickListener editProfileListener = v -> showEditProfileDialog();
        view.findViewById(R.id.btn_edit_homepage).setOnClickListener(editProfileListener);
        // 点击头像也可以编辑
        view.findViewById(R.id.cv_avatar).setOnClickListener(editProfileListener);

        // --- 新增：点击商家资质按钮跳转逻辑 ---
        view.findViewById(R.id.btn_qualification).setOnClickListener(v -> {
            if (currentMerchant != null) {
                // 跳转到资质认证页面，传递 currentMerchant 对象
                Intent intent = new Intent(getContext(), MerchantQualificationActivity.class);
                intent.putExtra("merchant", currentMerchant);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "无法获取商家信息", Toast.LENGTH_SHORT).show();
            }
        });

        // 修改密码按钮（功能暂未开放）
        view.findViewById(R.id.btn_change_password).setOnClickListener(v ->
                Toast.makeText(getContext(), "功能暂未开放", Toast.LENGTH_SHORT).show());
    }

    private void loadMerchantData() {
        if (currentMerchant != null) {
            tvMerchantName.setText(currentMerchant.getMerchantName());

            // 加载头像逻辑 (保留源码原有逻辑)
            try {
                // 如果 Merchant 类中有 getAvatar() 方法，请取消注释并使用
                // String avatarUri = currentMerchant.getAvatar();
                // if (avatarUri != null && !avatarUri.isEmpty()) {
                //    ivAvatar.setImageURI(Uri.parse(avatarUri));
                // } else {
                ivAvatar.setImageResource(R.drawable.merchant_picture);
                // }
            } catch (Exception e) {
                ivAvatar.setImageResource(R.drawable.merchant_picture);
            }
        }
    }

    /**
     * 显示编辑资料对话框
     */
    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("编辑商家主页");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        // 修改头像链接
        TextView btnChangeAvatar = new TextView(getContext());
        btnChangeAvatar.setText("点击更换头像");
        btnChangeAvatar.setTextSize(16);
        btnChangeAvatar.setPadding(0, 0, 0, 30);
        btnChangeAvatar.setTextColor(getResources().getColor(R.color.teal_200));
        btnChangeAvatar.setOnClickListener(v -> openGallery());
        layout.addView(btnChangeAvatar);

        // 修改名称输入框
        final EditText etName = new EditText(getContext());
        etName.setHint("请输入新的商家名称");
        if (currentMerchant != null) {
            etName.setText(currentMerchant.getMerchantName());
        }
        layout.addView(etName);

        builder.setView(layout);

        builder.setPositiveButton("保存", (dialog, which) -> {
            String newName = etName.getText().toString().trim();

            if (currentMerchant != null) {
                boolean isChanged = false;

                if (!TextUtils.isEmpty(newName) && !newName.equals(currentMerchant.getMerchantName())) {
                    currentMerchant.setMerchantName(newName);
                    tvMerchantName.setText(newName);
                    isChanged = true;
                }

                if (tempSelectedImageUri != null) {
                    // 如需保存头像，请在 Merchant.java 添加 avatar 字段并在此处赋值
                    // currentMerchant.setAvatar(tempSelectedImageUri.toString());
                    // ivAvatar.setImageURI(tempSelectedImageUri);
                    isChanged = true;
                    tempSelectedImageUri = null;
                }

                if (isChanged) {
                    saveMerchantToDb();
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void updateAvatarUI(String uriString) {
        tempSelectedImageUri = Uri.parse(uriString);
        ivAvatar.setImageURI(tempSelectedImageUri);
    }

    private void saveMerchantToDb() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(getContext()).merchantDao().update(currentMerchant);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "保存成功", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}
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

public class MerchantProfileFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvMerchantName;
    private Merchant currentMerchant;

    // 图片选择启动器
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    // 临时保存用户选择的图片URI
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
                            try {
                                requireContext().getContentResolver().takePersistableUriPermission(
                                        imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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
        if (currentMerchant != null) {
            Executors.newSingleThreadExecutor().execute(() -> {
                Merchant updated = AppDatabase.getInstance(getContext())
                        .merchantDao()
                        .findById(currentMerchant.getId());

                if (updated != null) {
                    currentMerchant = updated;
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(this::loadMerchantData);
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
        View.OnClickListener editProfileListener = v -> showEditProfileDialog();
        view.findViewById(R.id.btn_edit_homepage).setOnClickListener(editProfileListener);
        view.findViewById(R.id.cv_avatar).setOnClickListener(editProfileListener);

        view.findViewById(R.id.btn_qualification).setOnClickListener(v -> {
            if (currentMerchant != null) {
                Intent intent = new Intent(getContext(), MerchantQualificationActivity.class);
                intent.putExtra("merchant", currentMerchant);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "无法获取商家信息", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.btn_change_password).setOnClickListener(v ->
                Toast.makeText(getContext(), "功能暂未开放", Toast.LENGTH_SHORT).show());
    }

    private void loadMerchantData() {
        if (currentMerchant != null) {
            tvMerchantName.setText(currentMerchant.getMerchantName());

            // --- 修复点 1：正确加载商家头像 ---
            try {
                String avatarUri = currentMerchant.getAvatar();
                if (avatarUri != null && !avatarUri.isEmpty()) {
                    ivAvatar.setImageURI(Uri.parse(avatarUri));
                } else {
                    ivAvatar.setImageResource(R.drawable.merchant_picture);
                }
            } catch (Exception e) {
                ivAvatar.setImageResource(R.drawable.merchant_picture);
            }
        }
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("编辑商家主页");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        TextView btnChangeAvatar = new TextView(getContext());
        btnChangeAvatar.setText("点击更换头像");
        btnChangeAvatar.setTextSize(16);
        btnChangeAvatar.setPadding(0, 0, 0, 30);
        btnChangeAvatar.setTextColor(getResources().getColor(R.color.teal_200));
        btnChangeAvatar.setOnClickListener(v -> openGallery());
        layout.addView(btnChangeAvatar);

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

                // --- 修复点 2：保存头像 URI 到对象 ---
                if (tempSelectedImageUri != null) {
                    currentMerchant.setAvatar(tempSelectedImageUri.toString());
                    // 立即更新头像显示
                    ivAvatar.setImageURI(tempSelectedImageUri);
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
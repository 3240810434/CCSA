package com.gxuwz.ccsa.ui.merchant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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

        // 尝试初次获取
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

        // 1. 尝试从 Activity 获取
        if (currentMerchant == null && getActivity() instanceof MerchantMainActivity) {
            currentMerchant = ((MerchantMainActivity) getActivity()).getCurrentMerchant();
        }

        // 2. 如果还是为空，尝试从 SharedPreferences 获取 ID 并查询数据库 (兜底方案)
        if (currentMerchant == null && getContext() != null) {
            long savedId = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    .getLong("merchant_id", -1);
            if (savedId != -1) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    Merchant m = AppDatabase.getInstance(getContext()).merchantDao().findById((int) savedId);
                    if (m != null) {
                        currentMerchant = m;
                        // 更新 Activity 中的引用
                        if (getActivity() instanceof MerchantMainActivity) {
                            ((MerchantMainActivity) getActivity()).setCurrentMerchant(m);
                        }
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(this::loadMerchantData);
                        }
                    }
                });
            }
        }

        // 3. 如果已有数据，刷新最新状态
        if (currentMerchant != null) {
            loadMerchantData(); // 立即刷新UI
            Executors.newSingleThreadExecutor().execute(() -> {
                Merchant updated = AppDatabase.getInstance(getContext())
                        .merchantDao()
                        .findById(currentMerchant.getId());

                if (updated != null) {
                    currentMerchant = updated;
                    // 同步更新 Activity 中的全局对象
                    if (getActivity() instanceof MerchantMainActivity) {
                        ((MerchantMainActivity) getActivity()).setCurrentMerchant(updated);
                    }
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
                Toast.makeText(getContext(), "数据加载中或获取失败，请稍后再试", Toast.LENGTH_SHORT).show();
                // 触发一次重新加载
                onResume();
            }
        });

        view.findViewById(R.id.btn_change_password).setOnClickListener(v ->
                Toast.makeText(getContext(), "功能暂未开放", Toast.LENGTH_SHORT).show());

        // 【新增】我的营收点击事件
        View btnRevenue = view.findViewById(R.id.btn_my_revenue);
        if (btnRevenue != null) {
            btnRevenue.setOnClickListener(v -> {
                // 如果当前已有商家信息，直接跳转；或者如果SharedPreferences里有ID也可以跳转
                long savedId = -1;
                if (getContext() != null) {
                    savedId = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                            .getLong("merchant_id", -1);
                }

                if (currentMerchant != null || savedId != -1) {
                    Intent intent = new Intent(getContext(), MerchantRevenueActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "请先登录或等待数据加载", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadMerchantData() {
        if (currentMerchant != null) {
            tvMerchantName.setText(currentMerchant.getMerchantName());

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

                if (tempSelectedImageUri != null) {
                    currentMerchant.setAvatar(tempSelectedImageUri.toString());
                    ivAvatar.setImageURI(tempSelectedImageUri);
                    isChanged = true;
                    tempSelectedImageUri = null;
                }

                if (isChanged) {
                    if (getActivity() instanceof MerchantMainActivity) {
                        ((MerchantMainActivity) getActivity()).setCurrentMerchant(currentMerchant);
                    }
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
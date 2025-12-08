package com.gxuwz.ccsa.ui.resident;

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

import com.bumptech.glide.Glide; // 如果项目中没有Glide，可以使用setImageURI，建议添加依赖
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.login.LoginActivity;
import com.gxuwz.ccsa.model.User;

import java.util.concurrent.Executors;

public class MineFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvUsername;
    private TextView tvAddress;
    private User currentUser;

    // 图片选择启动器
    private ActivityResultLauncher<Intent> imagePickerLauncher;

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

                            // 2. 保存到数据库并更新UI
                            updateAvatar(imageUri.toString());
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);

        // 获取当前用户
        if (getActivity() instanceof ResidentMainActivity) {
            currentUser = ((ResidentMainActivity) getActivity()).getUser();
        }

        initViews(view);
        setupListeners(view);
        loadUserData();

        return view;
    }

    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvUsername = view.findViewById(R.id.tv_username);
        tvAddress = view.findViewById(R.id.tv_address);
    }

    private void setupListeners(View view) {
        // 点击头像或编辑主页 -> 编辑资料
        View.OnClickListener editProfileListener = v -> showEditProfileDialog();
        view.findViewById(R.id.cv_avatar).setOnClickListener(editProfileListener);
        view.findViewById(R.id.btn_edit_profile).setOnClickListener(editProfileListener);

        // 未实现功能提示
        View.OnClickListener notImplListener = v ->
                Toast.makeText(getContext(), "功能暂未开放", Toast.LENGTH_SHORT).show();
        view.findViewById(R.id.btn_my_orders).setOnClickListener(notImplListener);
        view.findViewById(R.id.btn_watch_history).setOnClickListener(notImplListener);
        view.findViewById(R.id.btn_change_password).setOnClickListener(notImplListener);

        // 既有功能
        view.findViewById(R.id.btn_my_repairs).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MyRepairsActivity.class);
            intent.putExtra("user", currentUser); // 传递用户对象
            startActivity(intent);
        });

        view.findViewById(R.id.btn_contact_property).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ContactPropertyActivity.class)));

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> showLogoutDialog());
    }

    private void loadUserData() {
        if (currentUser != null) {
            tvUsername.setText(currentUser.getUsername());
            String address = currentUser.getCommunity() + "-" + currentUser.getBuilding() + "-" + currentUser.getRoom();
            tvAddress.setText(address);

            // 加载头像
            if (currentUser.getAvatar() != null && !currentUser.getAvatar().isEmpty()) {
                // 使用 Glide 加载图片 (推荐) 或者原生 setImageURI
                // Glide.with(this).load(currentUser.getAvatar()).into(ivAvatar);
                ivAvatar.setImageURI(Uri.parse(currentUser.getAvatar()));
            } else {
                ivAvatar.setImageResource(R.drawable.ic_avatar); // 默认头像
            }
        }
    }

    /**
     * 显示编辑资料对话框
     */
    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("编辑个人资料");

        // 自定义布局：包含修改头像按钮和修改名称输入框
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        // 修改头像按钮
        TextView btnChangeAvatar = new TextView(getContext());
        btnChangeAvatar.setText("点击更换头像");
        btnChangeAvatar.setTextSize(16);
        btnChangeAvatar.setPadding(0, 0, 0, 30);
        btnChangeAvatar.setTextColor(getResources().getColor(R.color.teal_200));
        btnChangeAvatar.setOnClickListener(v -> openGallery());
        layout.addView(btnChangeAvatar);

        // 修改用户名输入框
        final EditText etUsername = new EditText(getContext());
        etUsername.setHint("请输入新的用户名");
        etUsername.setText(currentUser.getUsername());
        layout.addView(etUsername);

        builder.setView(layout);

        builder.setPositiveButton("保存", (dialog, which) -> {
            String newName = etUsername.getText().toString().trim();
            if (!TextUtils.isEmpty(newName)) {
                updateUsername(newName);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    // 打开相册
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    // 更新数据库中的头像
    private void updateAvatar(String uriString) {
        currentUser.setAvatar(uriString);
        ivAvatar.setImageURI(Uri.parse(uriString)); // 立即更新UI
        saveUserToDb();
    }

    // 更新数据库中的用户名
    private void updateUsername(String newName) {
        currentUser.setUsername(newName);
        tvUsername.setText(newName); // 立即更新UI
        saveUserToDb();
    }

    // 异步保存到数据库
    private void saveUserToDb() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(getContext()).userDao().update(currentUser);
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), "保存成功", Toast.LENGTH_SHORT).show()
            );
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("提示")
                .setMessage("确定要退出登录吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
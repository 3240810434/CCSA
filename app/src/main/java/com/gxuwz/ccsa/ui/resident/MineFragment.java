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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.User;

import java.util.concurrent.Executors;

public class MineFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvUsername;
    private TextView tvAddress;
    private User currentUser;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                            updateAvatar(imageUri.toString());
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);

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
        View.OnClickListener editProfileListener = v -> showEditProfileDialog();
        view.findViewById(R.id.cv_avatar).setOnClickListener(editProfileListener);
        view.findViewById(R.id.btn_edit_profile).setOnClickListener(editProfileListener);

        // --- 修改部分：跳转到我的订单页面 ---
        view.findViewById(R.id.btn_my_orders).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ResidentOrdersActivity.class);
            startActivity(intent);
        });
        // ------------------------------------

        View.OnClickListener notImplListener = v ->
                Toast.makeText(getContext(), "功能暂未开放", Toast.LENGTH_SHORT).show();
        view.findViewById(R.id.btn_watch_history).setOnClickListener(notImplListener);
        view.findViewById(R.id.btn_change_password).setOnClickListener(notImplListener);
    }

    private void loadUserData() {
        if (currentUser != null) {
            tvUsername.setText(currentUser.getName());
            String address = currentUser.getCommunity() + "-" + currentUser.getBuilding() + "-" + currentUser.getRoom();
            tvAddress.setText(address);

            if (currentUser.getAvatar() != null && !currentUser.getAvatar().isEmpty()) {
                ivAvatar.setImageURI(Uri.parse(currentUser.getAvatar()));
            } else {
                ivAvatar.setImageResource(R.drawable.lan);
            }
        }
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("编辑个人资料");

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

        final EditText etUsername = new EditText(getContext());
        etUsername.setHint("请输入新的用户名");
        if (currentUser != null) {
            etUsername.setText(currentUser.getName());
        }
        layout.addView(etUsername);

        builder.setView(layout);

        builder.setPositiveButton("保存", (dialog, which) -> {
            String newName = etUsername.getText().toString().trim();
            if (!TextUtils.isEmpty(newName) && currentUser != null) {
                updateUsername(newName);
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

    private void updateAvatar(String uriString) {
        if (currentUser == null) return;
        currentUser.setAvatar(uriString);
        ivAvatar.setImageURI(Uri.parse(uriString));
        saveUserToDb();
    }

    private void updateUsername(String newName) {
        if (currentUser == null) return;
        currentUser.setName(newName);
        tvUsername.setText(newName);
        saveUserToDb();
    }

    private void saveUserToDb() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(getContext()).userDao().update(currentUser);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "保存成功", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}
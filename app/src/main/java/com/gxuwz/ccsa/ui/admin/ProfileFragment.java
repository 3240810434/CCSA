package com.gxuwz.ccsa.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.gxuwz.ccsa.R;

public class ProfileFragment extends Fragment {

    private TextView tvAdminAccount;
    private String adminAccount;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            adminAccount = getArguments().getString("adminAccount");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_profile, container, false);
        tvAdminAccount = view.findViewById(R.id.tv_admin_account);
        if (adminAccount != null) {
            tvAdminAccount.setText(adminAccount);
        }
        return view;
    }
}

package com.gxuwz.ccsa.ui.merchant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.Merchant;

public class MerchantMineFragment extends Fragment {

    private TextView tvMerchantName;
    private TextView tvCommunity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_merchant_mine, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        tvMerchantName = view.findViewById(R.id.tv_merchant_name_display);
        tvCommunity = view.findViewById(R.id.tv_community_display);

        // 获取 Activity 传递过来的商家信息（如果有）
        if (getActivity() instanceof MerchantMainActivity) {
            Merchant merchant = ((MerchantMainActivity) getActivity()).getCurrentMerchant();
            if (merchant != null) {
                tvMerchantName.setText(merchant.getMerchantName());
                tvCommunity.setText(merchant.getCommunity());
            }
        }
    }
}
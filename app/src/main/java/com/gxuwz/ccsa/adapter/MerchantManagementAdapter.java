package com.gxuwz.ccsa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.Merchant;
import java.util.List;

public class MerchantManagementAdapter extends RecyclerView.Adapter<MerchantManagementAdapter.ViewHolder> {

    private List<Merchant> mMerchantList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onDeleteClick(Merchant merchant);
    }

    public MerchantManagementAdapter(List<Merchant> merchantList, OnItemClickListener listener) {
        mMerchantList = merchantList;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 使用新建的布局文件 item_merchant_management
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_merchant_management, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Merchant merchant = mMerchantList.get(position);

        // 绑定商家数据
        holder.tvMerchantName.setText(merchant.getMerchantName());
        holder.tvContactName.setText("联系人：" + merchant.getContactName());
        holder.tvGender.setText("性别：" + merchant.getGender());
        holder.tvPhone.setText("电话：" + merchant.getPhone());
        holder.tvCommunity.setText("服务小区：" + merchant.getCommunity());

        // 绑定删除按钮事件
        holder.btnDelete.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onDeleteClick(merchant);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMerchantList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMerchantName, tvContactName, tvGender, tvPhone, tvCommunity;
        TextView btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvMerchantName = itemView.findViewById(R.id.tv_merchant_name);
            tvContactName = itemView.findViewById(R.id.tv_contact_name);
            tvGender = itemView.findViewById(R.id.tv_gender);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvCommunity = itemView.findViewById(R.id.tv_community);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
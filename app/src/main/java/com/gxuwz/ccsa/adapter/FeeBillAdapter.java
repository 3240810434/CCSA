package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.PropertyFeeBill;
import java.util.ArrayList;
import java.util.List;

public class FeeBillAdapter extends RecyclerView.Adapter<FeeBillAdapter.ViewHolder> {
    private Context mContext;
    private List<PropertyFeeBill> mBillList;
    private List<Boolean> mCheckedList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemCheck(int position, boolean isChecked);
        void onDetailClick(int position);
    }

    public FeeBillAdapter(Context context, OnItemClickListener listener) {
        mContext = context;
        mListener = listener;
        mBillList = new ArrayList<>();
        mCheckedList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_fee_bill, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 索引有效性检查
        if (position < 0 || position >= mBillList.size()) {
            return;
        }

        PropertyFeeBill bill = mBillList.get(position);
        holder.tvPeriod.setText(String.format("%s 至 %s", bill.getPeriodStart(), bill.getPeriodEnd()));
        holder.tvAmount.setText(String.format("%.2f元", bill.getTotalAmount()));

        // 检查选中状态列表索引有效性
        if (position < mCheckedList.size()) {
            holder.cbSelect.setChecked(mCheckedList.get(position));
        }

        // 选择框点击事件
        holder.cbSelect.setOnClickListener(v -> {
            boolean isChecked = holder.cbSelect.isChecked();
            if (position < mCheckedList.size()) {
                mCheckedList.set(position, isChecked);
                if (mListener != null) {
                    mListener.onItemCheck(position, isChecked);
                }
            }
        });

        // 详情按钮点击事件
        holder.btnDetail.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onDetailClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBillList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSelect;
        TextView tvPeriod;
        TextView tvAmount;
        TextView btnDetail;

        public ViewHolder(View view) {
            super(view);
            cbSelect = view.findViewById(R.id.cb_select);
            tvPeriod = view.findViewById(R.id.tv_period);
            tvAmount = view.findViewById(R.id.tv_amount);
            btnDetail = view.findViewById(R.id.btn_detail);
        }
    }

    // 统一更新数据的方法，确保数据源与选中状态同步
    public void updateData(List<PropertyFeeBill> newBills) {
        mBillList.clear();
        if (newBills != null) {
            mBillList.addAll(newBills);
        }
        // 同步更新选中状态列表
        mCheckedList.clear();
        for (int i = 0; i < mBillList.size(); i++) {
            mCheckedList.add(false);
        }
        notifyDataSetChanged();
    }

    // 设置全选
    public void setAllChecked(boolean isChecked) {
        for (int i = 0; i < mCheckedList.size(); i++) {
            mCheckedList.set(i, isChecked);
        }
        notifyDataSetChanged();
    }

    // 判断是否全选
    public boolean isAllChecked() {
        for (boolean checked : mCheckedList) {
            if (!checked) {
                return false;
            }
        }
        return !mCheckedList.isEmpty();
    }

    // 获取选中的账单
    public List<PropertyFeeBill> getCheckedBills() {
        List<PropertyFeeBill> checkedBills = new ArrayList<>();
        for (int i = 0; i < mCheckedList.size(); i++) {
            if (i < mBillList.size() && mCheckedList.get(i)) {
                checkedBills.add(mBillList.get(i));
            }
        }
        return checkedBills;
    }

    // 新增：获取指定位置的账单
    public PropertyFeeBill getBill(int position) {
        if (position >= 0 && position < mBillList.size()) {
            return mBillList.get(position);
        }
        return null;
    }
}
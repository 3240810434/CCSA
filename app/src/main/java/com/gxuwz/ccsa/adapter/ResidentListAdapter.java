package com.gxuwz.ccsa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.User;
import java.util.List;

public class ResidentListAdapter extends RecyclerView.Adapter<ResidentListAdapter.ViewHolder> {

    private List<User> mResidentList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onDeleteClick(User user);
        // 已删除 onChatClick 方法
    }

    public ResidentListAdapter(List<User> residentList, OnItemClickListener listener) {
        mResidentList = residentList;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resident, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = mResidentList.get(position);
        holder.tvName.setText(user.getName());
        holder.tvGender.setText(user.getGender());
        holder.tvPhone.setText(user.getPhone());
        holder.tvBuilding.setText(user.getBuilding());
        holder.tvRoom.setText(user.getRoom());

        // 根据是否有监听器来决定是否显示操作按钮
        if (mListener == null) {
            // 居民端查看：隐藏注销按钮
            holder.btnDelete.setVisibility(View.GONE);
        } else {
            // 管理员端查看：显示按钮并绑定事件
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> mListener.onDeleteClick(user));
        }
        // 已删除 btnChat 的相关设置逻辑
    }

    @Override
    public int getItemCount() {
        return mResidentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvGender, tvPhone, tvBuilding, tvRoom;
        TextView btnDelete;
        // 已删除 btnChat 引用

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvGender = itemView.findViewById(R.id.tv_gender);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvBuilding = itemView.findViewById(R.id.tv_building);
            tvRoom = itemView.findViewById(R.id.tv_room);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            // 已删除 btnChat 的 findViewById
        }
    }
}
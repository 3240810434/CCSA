// app/src/main/java/com/gxuwz/ccsa/adapter/ResidentListAdapter.java
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

    // 定义点击事件接口
    public interface OnItemClickListener {
        void onDeleteClick(User user);
        void onChatClick(User user);
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

        // 绑定注销按钮事件
        holder.btnDelete.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onDeleteClick(user);
            }
        });

        // 绑定发消息按钮事件
        holder.btnChat.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onChatClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mResidentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvGender, tvPhone, tvBuilding, tvRoom;
        TextView btnDelete, btnChat;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvGender = itemView.findViewById(R.id.tv_gender);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvBuilding = itemView.findViewById(R.id.tv_building);
            tvRoom = itemView.findViewById(R.id.tv_room);
            // 绑定新按钮
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnChat = itemView.findViewById(R.id.btn_chat);
        }
    }
}
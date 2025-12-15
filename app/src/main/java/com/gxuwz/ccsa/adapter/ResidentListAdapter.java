// CCSA/app/src/main/java/com/gxuwz/ccsa/adapter/ResidentListAdapter.java
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
        void onChatClick(User user);
    }

    // 构造函数保持不变
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

        // --- 关键修改：根据是否有监听器来决定是否显示操作按钮 ---
        if (mListener == null) {
            // 居民端查看：隐藏操作按钮 (假设 btnDelete 和 btnChat 在布局中是可见的)
            // 注意：如果在 item_resident.xml 中这些按钮是在一个父容器里，最好隐藏父容器
            // 这里假设直接隐藏按钮，或者您可以检查布局里是否有一个LinearLayout包裹了它们
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnChat.setVisibility(View.GONE);
            // 如果有父容器包裹按钮，建议 holder.layoutActions.setVisibility(View.GONE);
        } else {
            // 管理员端查看：显示按钮并绑定事件
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnChat.setVisibility(View.VISIBLE);

            holder.btnDelete.setOnClickListener(v -> mListener.onDeleteClick(user));
            holder.btnChat.setOnClickListener(v -> mListener.onChatClick(user));
        }
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
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnChat = itemView.findViewById(R.id.btn_chat);
        }
    }
}
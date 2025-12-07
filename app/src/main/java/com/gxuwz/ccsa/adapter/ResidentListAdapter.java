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

    public ResidentListAdapter(List<User> residentList) {
        mResidentList = residentList;
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
    }

    @Override
    public int getItemCount() {
        return mResidentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvGender, tvPhone, tvBuilding, tvRoom;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvGender = itemView.findViewById(R.id.tv_gender);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvBuilding = itemView.findViewById(R.id.tv_building);
            tvRoom = itemView.findViewById(R.id.tv_room);
        }
    }
}

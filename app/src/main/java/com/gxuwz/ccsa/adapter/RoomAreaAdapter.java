package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.RoomArea;
import java.util.List;

public class RoomAreaAdapter extends RecyclerView.Adapter<RoomAreaAdapter.ViewHolder> {
    private Context mContext;
    private List<RoomArea> mRoomAreaList;
    private String mCommunity;

    public RoomAreaAdapter(Context context, List<RoomArea> roomAreaList, String community) {
        mContext = context;
        mRoomAreaList = roomAreaList;
        mCommunity = community;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_room_area, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RoomArea roomArea = mRoomAreaList.get(position);
        holder.tvCommunity.setText(mCommunity);
        holder.tvBuilding.setText(roomArea.getBuilding());
        holder.tvRoomNumber.setText(roomArea.getRoomNumber());
        holder.etArea.setText(String.valueOf(roomArea.getArea()));

        // 监听面积输入变化
        holder.etArea.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                updateRoomArea(holder, roomArea);
            }
        });
    }

    // 提取更新房间面积的逻辑为单独方法
    private void updateRoomArea(ViewHolder holder, RoomArea roomArea) {
        try {
            double area = Double.parseDouble(holder.etArea.getText().toString());
            roomArea.setArea(area);
        } catch (NumberFormatException e) {
            // 输入无效时保持原有值
            holder.etArea.setText(String.valueOf(roomArea.getArea()));
        }
    }

    // 新增：主动更新所有输入框的值到数据模型
    public void updateAllRoomAreas() {
        for (int i = 0; i < getItemCount(); i++) {
            ViewHolder holder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            if (holder != null) {
                RoomArea roomArea = mRoomAreaList.get(i);
                updateRoomArea(holder, roomArea);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mRoomAreaList.size();
    }

    public void setRoomAreas(List<RoomArea> roomAreas) {
        mRoomAreaList = roomAreas;
        notifyDataSetChanged();
    }

    public List<RoomArea> getRoomAreas() {
        return mRoomAreaList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommunity;
        TextView tvBuilding;
        TextView tvRoomNumber;
        EditText etArea;

        public ViewHolder(View itemView) {
            super(itemView);
            tvCommunity = itemView.findViewById(R.id.tv_community);
            tvBuilding = itemView.findViewById(R.id.tv_building);
            tvRoomNumber = itemView.findViewById(R.id.tv_room_number);
            etArea = itemView.findViewById(R.id.et_area);
        }
    }

    // 新增：保存RecyclerView引用
    private RecyclerView recyclerView;

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }
}
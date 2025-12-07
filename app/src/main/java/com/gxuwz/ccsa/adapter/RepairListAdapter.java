package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.Repair;
import com.gxuwz.ccsa.util.DateUtils;
import java.util.List;

public class RepairListAdapter extends RecyclerView.Adapter<RepairListAdapter.ViewHolder> {
    private Context context;
    private List<Repair> repairList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Repair repair);
        void onDetailClick(Repair repair);
    }

    public RepairListAdapter(Context context, List<Repair> repairList, OnItemClickListener listener) {
        this.context = context;
        this.repairList = repairList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_repair, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Repair repair = repairList.get(position);
        if (repair == null) return;

        holder.tvRepairNo.setText(repair.getRepairNo());
        holder.tvDate.setText(DateUtils.formatTime(repair.getSubmitTime()));
        holder.tvTitle.setText(repair.getTitle());
        holder.tvLocation.setText(repair.getBuilding() + repair.getRoom());

        // 设置状态标签
        if (repair.getStatus() == 0) {
            holder.tvStatus.setText("处理中");
            holder.tvStatus.setBackgroundResource(R.drawable.status_processing);
        } else {
            holder.tvStatus.setText("已完成");
            holder.tvStatus.setBackgroundResource(R.drawable.status_completed);
        }

        // 点击事件
        holder.itemView.setOnClickListener(v -> listener.onItemClick(repair));
        holder.tvDetail.setOnClickListener(v -> listener.onDetailClick(repair));
    }

    @Override
    public int getItemCount() {
        return repairList == null ? 0 : repairList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRepairNo, tvDate, tvStatus, tvTitle, tvLocation, tvDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRepairNo = itemView.findViewById(R.id.tv_repair_no);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvDetail = itemView.findViewById(R.id.tv_detail);
        }
    }

    public void updateData(List<Repair> list) {
        this.repairList = list;
        notifyDataSetChanged();
    }
}

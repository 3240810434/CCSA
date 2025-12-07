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

public class AdminRepairListAdapter extends RecyclerView.Adapter<AdminRepairListAdapter.ViewHolder> {
    private Context context;
    private List<Repair> repairList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onViewClick(Repair repair);
    }

    public AdminRepairListAdapter(Context context, List<Repair> repairList, OnItemClickListener listener) {
        this.context = context;
        this.repairList = repairList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_repair, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Repair repair = repairList.get(position);
        if (repair == null) return;

        holder.tvTitle.setText(repair.getTitle());
        holder.tvRepairNo.setText("单号: " + repair.getRepairNo());
        holder.tvLocation.setText("地点: " + repair.getBuilding() + repair.getRoom());
        holder.tvSubmitter.setText("提交人: " + repair.getUserName());
        holder.tvSubmitTime.setText("时间: " + DateUtils.formatTime(repair.getSubmitTime()));

        // 设置状态
        if (repair.getStatus() == 0) {
            holder.tvStatus.setText("待受理");
        } else {
            holder.tvStatus.setText("已维修");
        }

        // 查看按钮点击事件
        holder.tvView.setOnClickListener(v -> listener.onViewClick(repair));
    }

    @Override
    public int getItemCount() {
        return repairList == null ? 0 : repairList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvRepairNo, tvLocation, tvSubmitter, tvSubmitTime, tvStatus, tvView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvRepairNo = itemView.findViewById(R.id.tv_repair_no);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvSubmitter = itemView.findViewById(R.id.tv_submitter);
            tvSubmitTime = itemView.findViewById(R.id.tv_submit_time);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvView = itemView.findViewById(R.id.tv_view);
        }
    }

    public void updateData(List<Repair> list) {
        this.repairList = list;
        notifyDataSetChanged();
    }
}

package com.gxuwz.ccsa.ui.admin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.AdminNotice;
import com.gxuwz.ccsa.util.DateUtils;
import java.util.ArrayList;
import java.util.List;

public class AdminNoticeAdapter extends RecyclerView.Adapter<AdminNoticeAdapter.ViewHolder> {
    private List<AdminNotice> list = new ArrayList<>();
    private Context context;
    private boolean isDraft;

    public AdminNoticeAdapter(Context context, boolean isDraft) {
        this.context = context;
        this.isDraft = isDraft;
    }

    public void setData(List<AdminNotice> data) {
        this.list = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_notice, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminNotice notice = list.get(position);
        holder.tvTitle.setText(notice.getTitle());
        holder.tvContent.setText(notice.getContent());
        // 如果是草稿，显示创建时间；如果是已发布，显示发布时间
        String timeStr = DateUtils.dateToString(isDraft ? notice.getCreateTime() : notice.getPublishTime());
        holder.tvTime.setText(timeStr);

        holder.itemView.setOnClickListener(v -> {
            if (isDraft) {
                // 点击草稿，去编辑页面
                Intent intent = new Intent(context, AdminNoticeEditActivity.class);
                intent.putExtra("notice_id", notice.getId());
                context.startActivity(intent);
            } else {
                // 点击已发布，去详情页面
                Intent intent = new Intent(context, AdminNoticeDetailActivity.class);
                intent.putExtra("notice_id", notice.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvTime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.item_title);
            tvContent = itemView.findViewById(R.id.item_content);
            tvTime = itemView.findViewById(R.id.item_time);
        }
    }
}

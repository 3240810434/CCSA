// 文件路径: app/src/main/java/com/gxuwz/ccsa/adapter/FeeAnnouncementAdapter.java
package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.FeeAnnouncement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FeeAnnouncementAdapter extends RecyclerView.Adapter<FeeAnnouncementAdapter.ViewHolder> {

    private Context context;
    private List<FeeAnnouncement> list;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public FeeAnnouncementAdapter(Context context, List<FeeAnnouncement> list) {
        this.context = context;
        this.list = list;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_fee_announcement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FeeAnnouncement item = list.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvTime.setText("发布时间: " + sdf.format(new Date(item.getPublishTime())));

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(item);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onItemLongClick(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_item_title);
            tvTime = itemView.findViewById(R.id.tv_item_time);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(FeeAnnouncement announcement);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(FeeAnnouncement announcement);
    }
}
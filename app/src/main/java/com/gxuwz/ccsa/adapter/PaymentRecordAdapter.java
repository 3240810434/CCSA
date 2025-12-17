package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.PaymentRecord;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentRecordAdapter extends RecyclerView.Adapter<PaymentRecordAdapter.ViewHolder> {

    private Context context;
    private List<PaymentRecord> recordList;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private int expandedPosition = -1; // 记录当前展开的 Item 位置

    public PaymentRecordAdapter(Context context, List<PaymentRecord> recordList) {
        this.context = context;
        this.recordList = recordList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载 item_payment_record 布局
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 获取当前位置的记录
        PaymentRecord record = recordList.get(position);
        final int currentPos = position;

        // 1. 设置基础信息
        holder.tvPeriod.setText(record.getPeriod());
        holder.tvAmount.setText(String.format("-%.2f", record.getAmount()));
        holder.tvTime.setText(sdf.format(new Date(record.getPayTime())));

        // 2. 处理展开/折叠逻辑
        boolean isExpanded = (position == expandedPosition);
        holder.layoutDetails.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        // 点击整个卡片切换展开状态
        holder.itemView.setOnClickListener(v -> {
            expandedPosition = isExpanded ? -1 : currentPos; // 如果已展开则收起，否则展开当前
            notifyDataSetChanged(); // 刷新列表以更新视图
        });

        // 3. 设置详情数据 (仅当展开时处理，或者预先设置)
        if (isExpanded) {
            // 设置收据编号
            holder.tvReceipt.setText("电子收据号: " + (record.getReceiptNumber() != null ? record.getReceiptNumber() : "无"));

            // 解析费用明细 JSON
            if (record.getFeeDetailsSnapshot() != null) {
                try {
                    JSONObject json = new JSONObject(record.getFeeDetailsSnapshot());
                    holder.tvDetailProp.setText(String.format("物业费: ¥%.2f", json.optDouble("property", 0)));
                    holder.tvDetailMaint.setText(String.format("维修金: ¥%.2f", json.optDouble("maintenance", 0)));
                    holder.tvDetailUtil.setText(String.format("水电公摊: ¥%.2f", json.optDouble("utility", 0)));

                    double elevatorTotal = json.optDouble("elevator", 0) + json.optDouble("pressure", 0);
                    holder.tvDetailElev.setText(String.format("电梯/加压: ¥%.2f", elevatorTotal));

                    holder.tvDetailGarb.setText(String.format("垃圾费: ¥%.2f", json.optDouble("garbage", 0)));
                } catch (Exception e) {
                    holder.tvDetailProp.setText("明细解析失败");
                    e.printStackTrace();
                }
            } else {
                holder.tvDetailProp.setText("无详细费用数据");
                // 清空其他项
                holder.tvDetailMaint.setText("");
                holder.tvDetailUtil.setText("");
                holder.tvDetailElev.setText("");
                holder.tvDetailGarb.setText("");
            }
        }
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    // 更新列表数据的方法
    public void updateData(List<PaymentRecord> newRecords) {
        this.recordList.clear();
        this.recordList.addAll(newRecords);
        notifyDataSetChanged();
    }

    // ViewHolder 类：持有视图引用
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPeriod, tvAmount, tvTime;
        LinearLayout layoutDetails;
        TextView tvDetailProp, tvDetailMaint, tvDetailUtil, tvDetailElev, tvDetailGarb, tvReceipt;

        public ViewHolder(View itemView) {
            super(itemView);
            // 绑定 XML 中的 ID
            tvPeriod = itemView.findViewById(R.id.tv_period);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvTime = itemView.findViewById(R.id.tv_time);
            layoutDetails = itemView.findViewById(R.id.layout_details);
            tvDetailProp = itemView.findViewById(R.id.tv_detail_prop);
            tvDetailMaint = itemView.findViewById(R.id.tv_detail_maint);
            tvDetailUtil = itemView.findViewById(R.id.tv_detail_util);
            tvDetailElev = itemView.findViewById(R.id.tv_detail_elev);
            tvDetailGarb = itemView.findViewById(R.id.tv_detail_garb);
            tvReceipt = itemView.findViewById(R.id.tv_receipt);

            // 注意：这里删除了 R.id.tv_method 的引用，因为新布局中已经移除了它
        }
    }
}
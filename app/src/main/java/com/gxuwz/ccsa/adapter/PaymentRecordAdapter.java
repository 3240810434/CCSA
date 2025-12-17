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

    // 记录当前展开的 Item 位置，-1表示没有展开的项
    private int expandedPosition = -1;

    public PaymentRecordAdapter(Context context, List<PaymentRecord> recordList) {
        this.context = context;
        this.recordList = recordList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 使用 holder.getAdapterPosition() 获取位置更安全
        int currentPos = holder.getAdapterPosition();
        if (currentPos == RecyclerView.NO_POSITION) return;

        PaymentRecord record = recordList.get(currentPos);

        // 1. 设置基础显示信息
        holder.tvPeriod.setText(record.getPeriod());
        holder.tvAmount.setText(String.format("-%.2f", record.getAmount()));
        holder.tvTime.setText(sdf.format(new Date(record.getPayTime())));

        // 2. 处理展开/折叠逻辑
        boolean isExpanded = (currentPos == expandedPosition);
        holder.layoutDetails.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        // 设置点击监听器，点击卡片任何位置都可以展开/折叠
        holder.itemView.setOnClickListener(v -> {
            int previousExpanded = expandedPosition;
            // 如果点击的是当前已展开的项，则折叠(-1)，否则展开当前项
            expandedPosition = isExpanded ? -1 : currentPos;

            // 刷新动画：刷新旧的和新的位置
            if (previousExpanded != -1) {
                notifyItemChanged(previousExpanded);
            }
            notifyItemChanged(currentPos);
        });

        // 3. 只有当展开时，才去解析和设置详细数据（优化性能）
        if (isExpanded) {
            holder.tvReceipt.setText("电子收据号: " + (record.getReceiptNumber() != null ? record.getReceiptNumber() : "无"));

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
                // 清空其他
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

    public void updateData(List<PaymentRecord> newRecords) {
        this.recordList.clear();
        this.recordList.addAll(newRecords);
        notifyDataSetChanged();
    }

    // ViewHolder 类
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // 定义控件变量
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

            // 注意：不要在这里添加 tv_method，因为 XML 里没有这个 ID 了！
        }
    }
}
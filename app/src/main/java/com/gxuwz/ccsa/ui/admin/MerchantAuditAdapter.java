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
import com.gxuwz.ccsa.model.Merchant;
import java.util.List;

public class MerchantAuditAdapter extends RecyclerView.Adapter<MerchantAuditAdapter.ViewHolder> {

    private Context context;
    private List<Merchant> list;

    public MerchantAuditAdapter(Context context, List<Merchant> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_merchant_audit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Merchant merchant = list.get(position);
        holder.tvName.setText(merchant.getMerchantName());
        holder.tvCommunity.setText("服务小区：" + merchant.getCommunity());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MerchantAuditDetailActivity.class);
            intent.putExtra("merchant_id", merchant.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCommunity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_audit_merchant_name);
            tvCommunity = itemView.findViewById(R.id.tv_audit_community);
        }
    }
}
package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.Vote;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VoteAdapter extends RecyclerView.Adapter<VoteAdapter.VoteViewHolder> {
    private Context context;
    private List<Vote> voteList;
    private boolean isAdmin; // 是否为管理员（控制删除按钮显示）
    private OnVoteItemClickListener listener;

    public interface OnVoteItemClickListener {
        void onItemClick(Vote vote);
        void onDeleteClick(Vote vote);
    }

    public VoteAdapter(Context context, List<Vote> voteList, boolean isAdmin, OnVoteItemClickListener listener) {
        this.context = context;
        this.voteList = voteList;
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vote, parent, false);
        return new VoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoteViewHolder holder, int position) {
        Vote vote = voteList.get(position);
        holder.tvTitle.setText(vote.getTitle());
        holder.tvContent.setText(vote.getContent()); // 已与布局ID匹配

        // 格式化发布时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        holder.tvTime.setText(sdf.format(new Date(vote.getPublishTime())));

        // 管理员显示删除按钮，居民隐藏
        holder.btnDelete.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        // 点击投票项进入详情
        holder.itemView.setOnClickListener(v -> listener.onItemClick(vote));

        // 点击删除按钮
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(vote));
    }

    @Override
    public int getItemCount() {
        return voteList.size();
    }

    public static class VoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvTime;
        Button btnDelete;

        public VoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_vote_item_title); // 匹配布局ID
            tvContent = itemView.findViewById(R.id.tv_vote_item_content); // 匹配布局ID
            tvTime = itemView.findViewById(R.id.tv_vote_item_time); // 匹配布局ID
            btnDelete = itemView.findViewById(R.id.btn_vote_delete); // 匹配布局ID
        }
    }
}

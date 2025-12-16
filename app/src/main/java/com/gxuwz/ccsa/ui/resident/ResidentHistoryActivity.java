package com.gxuwz.ccsa.ui.resident;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.HelpPost;
import com.gxuwz.ccsa.model.HistoryRecord;
import com.gxuwz.ccsa.model.Post;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.util.DateUtils;
import com.gxuwz.ccsa.util.SharedPreferencesUtil;
import java.util.ArrayList;
import java.util.List;

public class ResidentHistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private HistoryAdapter adapter;
    private List<HistoryRecord> historyList = new ArrayList<>();
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 【修改点】这里去掉了 .xml 后缀
        setContentView(R.layout.activity_resident_history);

        currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadHistory();
    }

    private void initViews() {
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        findViewById(R.id.iv_delete_all).setOnClickListener(v -> showClearConfirmDialog());

        rvHistory = findViewById(R.id.rv_history);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter();
        rvHistory.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次回到页面刷新数据，确保删除或新增后数据同步
        loadHistory();
    }

    private void loadHistory() {
        new Thread(() -> {
            // 获取当前用户的历史记录，按时间倒序
            List<HistoryRecord> list = AppDatabase.getInstance(this)
                    .historyDao()
                    .getUserHistory(currentUser.getId());

            runOnUiThread(() -> {
                historyList.clear();
                historyList.addAll(list);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void showClearConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("确定要清空所有观看历史吗？")
                .setPositiveButton("清空", (dialog, which) -> clearHistory())
                .setNegativeButton("取消", null)
                .show();
    }

    private void clearHistory() {
        new Thread(() -> {
            AppDatabase.getInstance(this).historyDao().clearHistory(currentUser.getId());
            runOnUiThread(() -> {
                historyList.clear();
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "已清空", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history_record, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HistoryRecord record = historyList.get(position);
            holder.tvTitle.setText(record.title);
            holder.tvAuthor.setText("发布者: " + record.authorName);
            holder.tvTime.setText(DateUtils.formatTime(record.viewTime));

            // 根据类型显示标签
            if (record.type == 1) {
                holder.tvType.setText("生活动态");
                holder.tvType.setBackgroundResource(R.drawable.button_border_gray); // 假设的样式
            } else {
                holder.tvType.setText("邻里互助");
                holder.tvType.setBackgroundResource(R.drawable.button_border_gray);
            }

            Glide.with(holder.itemView.getContext())
                    .load(record.coverImage)
                    .placeholder(R.drawable.lan)
                    .error(R.drawable.lan)
                    .into(holder.ivCover);

            // 点击跳转到详情页
            holder.itemView.setOnClickListener(v -> {
                new Thread(() -> {
                    AppDatabase db = AppDatabase.getInstance(ResidentHistoryActivity.this);

                    if (record.type == 1) {
                        // === 跳转到生活动态详情 ===
                        // 注意：这里暂时使用遍历查找，如果PostDao有findById(int id)建议替换为直接查询
                        List<Post> posts = db.postDao().getAllPosts();
                        Post target = null;
                        for(Post p : posts) {
                            if(p.id == record.relatedId) {
                                target = p;
                                break;
                            }
                        }

                        if (target != null) {
                            Intent intent = new Intent(ResidentHistoryActivity.this, PostDetailActivity.class);
                            intent.putExtra("post", target);
                            intent.putExtra("user", currentUser);
                            runOnUiThread(() -> startActivity(intent));
                        } else {
                            runOnUiThread(() -> Toast.makeText(ResidentHistoryActivity.this, "该动态已被删除", Toast.LENGTH_SHORT).show());
                        }

                    } else if (record.type == 2) {
                        // === 跳转到邻里互助详情 ===
                        List<HelpPost> posts = db.helpPostDao().getAllHelpPosts();
                        HelpPost target = null;
                        for(HelpPost p : posts) {
                            if(p.id == record.relatedId) {
                                target = p;
                                break;
                            }
                        }

                        if (target != null) {
                            Intent intent = new Intent(ResidentHistoryActivity.this, HelpPostDetailActivity.class);
                            intent.putExtra("helpPost", target);
                            intent.putExtra("user", currentUser);
                            runOnUiThread(() -> startActivity(intent));
                        } else {
                            runOnUiThread(() -> Toast.makeText(ResidentHistoryActivity.this, "该互助帖已被删除", Toast.LENGTH_SHORT).show());
                        }
                    }
                }).start();
            });
        }

        @Override
        public int getItemCount() { return historyList.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivCover;
            TextView tvTitle, tvAuthor, tvTime, tvType;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivCover = itemView.findViewById(R.id.iv_cover);
                tvTitle = itemView.findViewById(R.id.tv_title);
                tvAuthor = itemView.findViewById(R.id.tv_author);
                tvTime = itemView.findViewById(R.id.tv_time);
                tvType = itemView.findViewById(R.id.tv_type);
            }
        }
    }
}
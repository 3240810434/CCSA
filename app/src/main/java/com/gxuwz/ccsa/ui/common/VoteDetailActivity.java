package com.gxuwz.ccsa.ui.common;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Vote;
import com.gxuwz.ccsa.model.VoteRecord;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoteDetailActivity extends AppCompatActivity {
    private Vote vote;
    private boolean isAdmin;
    private String userId;
    private AppDatabase db;

    private TextView tvTitle, tvContent, tvTime, tvTotalStats;
    private ImageView ivAttachment;
    private LinearLayout layoutVotingArea;
    private LinearLayout layoutStatsArea;
    private Button btnSubmit;

    private List<CheckBox> checkBoxes = new ArrayList<>();
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_detail);

        vote = (Vote) getIntent().getSerializableExtra("vote");
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);
        userId = getIntent().getStringExtra("userId");
        db = AppDatabase.getInstance(this);

        initViews();
        loadData();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_detail_title);
        tvContent = findViewById(R.id.tv_detail_content);
        tvTime = findViewById(R.id.tv_detail_time);
        ivAttachment = findViewById(R.id.iv_detail_attachment);
        layoutVotingArea = findViewById(R.id.layout_voting_area);
        layoutStatsArea = findViewById(R.id.layout_stats_area);
        tvTotalStats = findViewById(R.id.tv_total_stats);
        btnSubmit = findViewById(R.id.btn_submit_vote);

        tvTitle.setText(vote.getTitle());
        tvContent.setText(vote.getContent());
        // 如果有时间字段，建议在这里设置，防止空指针或显示默认文案
        // tvTime.setText(vote.getCreateTime());

        if (!TextUtils.isEmpty(vote.getAttachmentPath())) {
            ivAttachment.setVisibility(View.VISIBLE);
            Glide.with(this).load(vote.getAttachmentPath()).into(ivAttachment);
        }

        btnSubmit.setOnClickListener(v -> submitVote());
    }

    private void loadData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            boolean hasVoted = false;
            VoteRecord record = null;
            if (!isAdmin) {
                // 【修复 2】这里应该使用 voteRecordDao() 而不是 voteDao()
                record = db.voteRecordDao().getVoteRecord(vote.getId(), userId);
                hasVoted = (record != null);
            }

            // 【修复 2】同上，修正 DAO 调用
            List<VoteRecord> allRecords = db.voteRecordDao().getAllRecordsForVote(vote.getId());
            // 注意：countResidents 方法如果数据量大可能会慢，放在子线程是正确的
            int totalResidents = db.userDao().countResidents(vote.getCommunity());

            Map<Integer, Integer> counts = new HashMap<>();
            List<String> options = vote.getOptionList();
            for(int i=0; i<options.size(); i++) counts.put(i, 0);

            for (VoteRecord r : allRecords) {
                if (r.getSelectedIndices() == null) continue; // 防止空指针
                String[] indices = r.getSelectedIndices().split(",");
                for (String idxStr : indices) {
                    try {
                        if (TextUtils.isEmpty(idxStr)) continue;
                        int idx = Integer.parseInt(idxStr.trim());
                        int current = counts.containsKey(idx) ? counts.get(idx) : 0;
                        counts.put(idx, current + 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            boolean finalHasVoted = hasVoted;
            runOnUiThread(() -> {
                if (isAdmin || finalHasVoted) {
                    showStats(options, counts, allRecords.size(), totalResidents);
                } else {
                    showVotingOptions(options);
                }
            });
        });
    }

    private void showVotingOptions(List<String> options) {
        layoutStatsArea.setVisibility(View.GONE);
        layoutVotingArea.setVisibility(View.VISIBLE);
        btnSubmit.setVisibility(View.VISIBLE);

        // 【关键修复】清除之前的视图，防止重复添加导致选项翻倍
        layoutVotingArea.removeAllViews();
        checkBoxes.clear(); // 同时清空 checkbox 引用列表

        if (vote.getSelectionType() == 0) { // 单选
            radioGroup = new RadioGroup(this);
            for (int i = 0; i < options.size(); i++) {
                RadioButton rb = new RadioButton(this);
                rb.setText(options.get(i));
                rb.setId(i);
                // 优化体验：增加内边距
                rb.setPadding(0, 20, 0, 20);
                radioGroup.addView(rb);
            }
            layoutVotingArea.addView(radioGroup);
        } else { // 多选
            for (int i = 0; i < options.size(); i++) {
                CheckBox cb = new CheckBox(this);
                cb.setText(options.get(i));
                cb.setTag(i);
                // 优化体验：增加内边距
                cb.setPadding(0, 20, 0, 20);
                checkBoxes.add(cb);
                layoutVotingArea.addView(cb);
            }
        }
    }

    private void showStats(List<String> options, Map<Integer, Integer> counts, int votedCount, int totalCount) {
        layoutVotingArea.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.GONE);
        layoutStatsArea.setVisibility(View.VISIBLE);

        tvTotalStats.setText("本小区共 " + totalCount + " 户，已参与 " + votedCount + " 户");

        // 【关键修复】清除旧的统计条目。
        // 因为 layoutStatsArea 中第一个子 View 是 xml 中写的 tvTotalStats，所以从索引 1 开始移除
        int childCount = layoutStatsArea.getChildCount();
        if (childCount > 1) {
            layoutStatsArea.removeViews(1, childCount - 1);
        }

        for (int i = 0; i < options.size(); i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_vote_stat, layoutStatsArea, false);
            TextView tvName = view.findViewById(R.id.tv_opt_name);
            ProgressBar pb = view.findViewById(R.id.pb_opt_count);
            TextView tvCount = view.findViewById(R.id.tv_opt_count);

            int count = counts.containsKey(i) ? counts.get(i) : 0;
            tvName.setText(options.get(i));

            // 进度条最大值设为总户数（或者已投票数，看需求），防止除以0
            pb.setMax(totalCount == 0 ? 1 : totalCount);
            pb.setProgress(count);
            tvCount.setText(count + "票");

            layoutStatsArea.addView(view);
        }
    }

    private void submitVote() {
        StringBuilder sb = new StringBuilder();
        if (vote.getSelectionType() == 0) {
            if (radioGroup == null) return; // 防止空指针
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "请选择一个选项", Toast.LENGTH_SHORT).show();
                return;
            }
            sb.append(selectedId);
        } else {
            boolean hasSelect = false;
            for (CheckBox cb : checkBoxes) {
                if (cb.isChecked()) {
                    if (hasSelect) sb.append(",");
                    sb.append(cb.getTag());
                    hasSelect = true;
                }
            }
            if (!hasSelect) {
                Toast.makeText(this, "请至少选择一个选项", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 可以在这里禁用按钮防止重复点击
        btnSubmit.setEnabled(false);

        VoteRecord record = new VoteRecord(vote.getId(), userId, sb.toString());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 【修复 3】在提交前，再次检查是否已投票（防止并发或UI未及时刷新）
                VoteRecord existing = db.voteRecordDao().getVoteRecord(vote.getId(), userId);
                if (existing != null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "您已经投过票了，请勿重复投票", Toast.LENGTH_SHORT).show();
                        loadData(); // 刷新界面进入结果页
                    });
                    return;
                }

                // 修正 DAO 调用为 voteRecordDao()
                db.voteRecordDao().insertRecord(record);

                runOnUiThread(() -> {
                    Toast.makeText(this, "投票成功", Toast.LENGTH_SHORT).show();
                    loadData();
                    // 投票成功后不需要恢复按钮，loadData 会隐藏按钮
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "投票失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSubmit.setEnabled(true); // 失败时恢复按钮
                });
            }
        });
    }
}
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
import com.bumptech.glide.Glide; // 假设使用了Glide加载图片，如果没有请使用ImageUtils
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
    private LinearLayout layoutVotingArea; // 投票区域（居民）
    private LinearLayout layoutStatsArea;  // 统计区域（管理员/已投）
    private Button btnSubmit;

    // 动态控件引用
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
        // Simple date format omitted for brevity

        if (!TextUtils.isEmpty(vote.getAttachmentPath())) {
            ivAttachment.setVisibility(View.VISIBLE);
            // 简单图片加载
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
                record = db.voteDao().getVoteRecord(vote.getId(), userId);
                hasVoted = (record != null);
            }

            // 获取统计数据
            List<VoteRecord> allRecords = db.voteDao().getAllRecordsForVote(vote.getId());
            int totalResidents = db.userDao().countResidents(vote.getCommunity());

            // 统计每个选项的票数
            Map<Integer, Integer> counts = new HashMap<>();
            List<String> options = vote.getOptionList();
            for(int i=0; i<options.size(); i++) counts.put(i, 0);

            for (VoteRecord r : allRecords) {
                String[] indices = r.getSelectedIndices().split(",");
                for (String idxStr : indices) {
                    try {
                        int idx = Integer.parseInt(idxStr);
                        counts.put(idx, counts.getOrDefault(idx, 0) + 1);
                    } catch (Exception e) {}
                }
            }

            boolean finalHasVoted = hasVoted;
            VoteRecord finalRecord = record;
            runOnUiThread(() -> {
                if (isAdmin || finalHasVoted) {
                    showStats(options, counts, allRecords.size(), totalResidents);
                } else {
                    showVotingOptions(options);
                }
            });
        });
    }

    // 渲染投票选项（未投票居民）
    private void showVotingOptions(List<String> options) {
        layoutStatsArea.setVisibility(View.GONE);
        layoutVotingArea.setVisibility(View.VISIBLE);
        btnSubmit.setVisibility(View.VISIBLE);

        if (vote.getSelectionType() == 0) { // 单选
            radioGroup = new RadioGroup(this);
            for (int i = 0; i < options.size(); i++) {
                RadioButton rb = new RadioButton(this);
                rb.setText(options.get(i));
                rb.setId(i);
                radioGroup.addView(rb);
            }
            layoutVotingArea.addView(radioGroup);
        } else { // 多选
            checkBoxes.clear();
            for (int i = 0; i < options.size(); i++) {
                CheckBox cb = new CheckBox(this);
                cb.setText(options.get(i));
                cb.setTag(i);
                checkBoxes.add(cb);
                layoutVotingArea.addView(cb);
            }
        }
    }

    // 渲染统计条（管理员或已投票居民）
    private void showStats(List<String> options, Map<Integer, Integer> counts, int votedCount, int totalCount) {
        layoutVotingArea.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.GONE);
        layoutStatsArea.setVisibility(View.VISIBLE);

        tvTotalStats.setText("本小区共 " + totalCount + " 户，已参与 " + votedCount + " 户");

        for (int i = 0; i < options.size(); i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_vote_stat, layoutStatsArea, false);
            TextView tvName = view.findViewById(R.id.tv_opt_name);
            ProgressBar pb = view.findViewById(R.id.pb_opt_count);
            TextView tvCount = view.findViewById(R.id.tv_opt_count);

            int count = counts.getOrDefault(i, 0);
            tvName.setText(options.get(i));
            pb.setMax(totalCount == 0 ? 1 : totalCount); // 避免除以0，实际可以用最大投票数
            pb.setProgress(count);
            tvCount.setText(count + "票");

            layoutStatsArea.addView(view);
        }
    }

    private void submitVote() {
        StringBuilder sb = new StringBuilder();
        if (vote.getSelectionType() == 0) {
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

        VoteRecord record = new VoteRecord(vote.getId(), userId, sb.toString());
        Executors.newSingleThreadExecutor().execute(() -> {
            db.voteDao().insertRecord(record);
            runOnUiThread(() -> {
                Toast.makeText(this, "投票成功", Toast.LENGTH_SHORT).show();
                loadData(); // 刷新页面看统计
            });
        });
    }
}
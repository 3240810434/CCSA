package com.gxuwz.ccsa.ui.common;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Vote;
import com.gxuwz.ccsa.model.VoteRecord;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoteDetailActivity extends AppCompatActivity {
    private TextView tvTitle, tvContent, tvTime, tvResult;
    private Button btnAgree, btnOppose;
    private Vote vote;
    private boolean isAdmin;
    private String userId;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_detail);

        vote = (Vote) getIntent().getSerializableExtra("vote");
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);
        userId = getIntent().getStringExtra("userId");
        db = AppDatabase.getInstance(this);

        tvTitle = findViewById(R.id.tv_vote_title);
        tvContent = findViewById(R.id.tv_vote_content);
        tvTime = findViewById(R.id.tv_vote_time);
        tvResult = findViewById(R.id.tv_vote_result);
        btnAgree = findViewById(R.id.btn_agree);
        btnOppose = findViewById(R.id.btn_oppose);

        displayVoteDetails();
        checkIfVoted();

        btnAgree.setOnClickListener(v -> submitVote(true));
        btnOppose.setOnClickListener(v -> submitVote(false));

        if (isAdmin) {
            btnAgree.setVisibility(View.GONE);
            btnOppose.setVisibility(View.GONE);
        }
    }

    private void displayVoteDetails() {
        tvTitle.setText(vote.getTitle());
        tvContent.setText(vote.getContent());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        tvTime.setText("发布时间: " + sdf.format(new Date(vote.getPublishTime())));
        updateVoteResult();
    }

    private void updateVoteResult() {
        tvResult.setText(String.format("当前结果: 赞成 %d 票，反对 %d 票",
                vote.getAgreeCount(), vote.getOpposeCount()));
    }

    private void checkIfVoted() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            VoteRecord record = db.voteRecordDao().getVoteRecord(vote.getId(), userId);
            runOnUiThread(() -> {
                if (record != null) {
                    btnAgree.setEnabled(false);
                    btnOppose.setEnabled(false);
                    btnAgree.setText("已赞成");
                    btnOppose.setText("已反对");

                    // 修复：使用项目中已存在的按钮背景资源（替换为实际可用资源）
                    if (record.isAgree()) {
                        btnAgree.setBackgroundResource(R.drawable.blue_rounded_btn); // 复用已有蓝色按钮
                    } else {
                        btnOppose.setBackgroundResource(R.drawable.blue_rounded_btn); // 复用已有蓝色按钮
                    }
                }
            });
        });
    }

    private void submitVote(boolean isAgree) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            VoteRecord existingRecord = db.voteRecordDao().getVoteRecord(vote.getId(), userId);
            if (existingRecord != null) {
                runOnUiThread(() ->
                        Toast.makeText(this, "您已投过票，不能重复投票", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            VoteRecord record = new VoteRecord(vote.getId(), userId, isAgree);
            db.voteRecordDao().insert(record);

            if (isAgree) {
                vote.setAgreeCount(vote.getAgreeCount() + 1);
            } else {
                vote.setOpposeCount(vote.getOpposeCount() + 1);
            }
            db.voteDao().update(vote);

            runOnUiThread(() -> {
                Toast.makeText(this, "投票成功", Toast.LENGTH_SHORT).show();
                updateVoteResult();
                btnAgree.setEnabled(false);
                btnOppose.setEnabled(false);
            });
        });
    }
}
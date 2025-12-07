package com.gxuwz.ccsa.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.VoteAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Vote;
import com.gxuwz.ccsa.ui.common.VoteDetailActivity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoteManagementActivity extends AppCompatActivity implements VoteAdapter.OnVoteItemClickListener {
    private RecyclerView rvVotes;
    private VoteAdapter adapter;
    private String community;
    private String adminAccount;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_management);

        // 获取传递的小区和管理员信息
        community = getIntent().getStringExtra("community");
        adminAccount = getIntent().getStringExtra("adminAccount");

        db = AppDatabase.getInstance(this);
        rvVotes = findViewById(R.id.rv_votes);
        rvVotes.setLayoutManager(new LinearLayoutManager(this));

        // 显示发布按钮（物业专属）
        Button btnAddVote = findViewById(R.id.btn_add_vote);
        btnAddVote.setVisibility(View.VISIBLE);
        btnAddVote.setOnClickListener(v -> {
            // 跳转到创建投票页面
            Intent intent = new Intent(VoteManagementActivity.this, CreateVoteActivity.class);
            intent.putExtra("community", community);
            intent.putExtra("adminAccount", adminAccount);
            startActivity(intent);
        });

        loadVotes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVotes(); // 刷新投票列表
    }

    // 加载小区所有投票（按发布时间倒序）
    private void loadVotes() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Vote> votes = db.voteDao().getVotesByCommunity(community);
            runOnUiThread(() -> {
                // 传入isAdmin=true，显示删除按钮
                adapter = new VoteAdapter(this, votes, true, this);
                rvVotes.setAdapter(adapter);
            });
        });
    }

    // 点击投票项进入详情
    @Override
    public void onItemClick(Vote vote) {
        Intent detailIntent = new Intent(this, VoteDetailActivity.class);
        detailIntent.putExtra("vote", vote);
        detailIntent.putExtra("userId", adminAccount);
        detailIntent.putExtra("isAdmin", true); // 标记为管理员
        startActivity(detailIntent);
    }

    // 物业删除投票
    @Override
    public void onDeleteClick(Vote vote) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            db.voteDao().delete(vote);
            runOnUiThread(() -> {
                Toast.makeText(this, "投票已删除", Toast.LENGTH_SHORT).show();
                loadVotes(); // 重新加载列表
            });
        });
    }
}
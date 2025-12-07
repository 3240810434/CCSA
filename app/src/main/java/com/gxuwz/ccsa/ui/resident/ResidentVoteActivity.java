package com.gxuwz.ccsa.ui.resident;

import android.content.Intent; // 补充关键导入
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.VoteAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.model.Vote;
import com.gxuwz.ccsa.ui.common.VoteDetailActivity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResidentVoteActivity extends AppCompatActivity implements VoteAdapter.OnVoteItemClickListener {
    private RecyclerView rvVotes;
    private VoteAdapter adapter;
    private String community;
    private String userId;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_management);

        // 修复：强制转换前增加类型检查
        Intent intent = getIntent();
        User user = null;
        if (intent.hasExtra("user") && intent.getSerializableExtra("user") instanceof User) {
            user = (User) intent.getSerializableExtra("user");
        }

        if (user != null) {
            community = user.getCommunity();
            userId = user.getPhone();
        }

        db = AppDatabase.getInstance(this);
        rvVotes = findViewById(R.id.rv_votes);
        rvVotes.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.btn_add_vote).setVisibility(View.GONE);
        loadVotes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVotes();
    }

    private void loadVotes() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Vote> votes = db.voteDao().getVotesByCommunity(community);
            runOnUiThread(() -> {
                adapter = new VoteAdapter(this, votes, false, this);
                rvVotes.setAdapter(adapter);
            });
        });
    }

    @Override
    public void onItemClick(Vote vote) {
        // 修复：使用正确的Intent构造及传参方式
        Intent detailIntent = new Intent(ResidentVoteActivity.this, VoteDetailActivity.class);
        detailIntent.putExtra("vote", vote);
        detailIntent.putExtra("userId", userId);
        detailIntent.putExtra("isAdmin", false);
        startActivity(detailIntent);
    }

    @Override
    public void onDeleteClick(Vote vote) {
        // 空实现（居民无删除权限）
    }
}
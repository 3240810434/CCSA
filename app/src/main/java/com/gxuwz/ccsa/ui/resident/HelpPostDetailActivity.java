package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.HelpPost;
import com.gxuwz.ccsa.model.HistoryRecord;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.util.DateUtils;

public class HelpPostDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_post_detail);

        HelpPost post = (HelpPost) getIntent().getSerializableExtra("helpPost");
        User user = (User) getIntent().getSerializableExtra("user");

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        if (post != null) {
            ((TextView)findViewById(R.id.tv_title)).setText(post.title);
            ((TextView)findViewById(R.id.tv_content)).setText(post.content);
            ((TextView)findViewById(R.id.tv_info)).setText(
                    "发布者: " + post.userName + "  " + DateUtils.formatTime(post.createTime)
            );

            if (user != null) saveHistory(user, post);
        }
    }

    private void saveHistory(User currentUser, HelpPost post) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.historyDao().deleteRecord(currentUser.getId(), post.id, 2);

            String cover = post.userAvatar;
            if (post.mediaList != null && !post.mediaList.isEmpty()) {
                cover = post.mediaList.get(0).url;
            }

            HistoryRecord record = new HistoryRecord(
                    currentUser.getId(),
                    post.id,
                    2, // 2 for HelpPost
                    post.title,
                    cover,
                    post.userName,
                    System.currentTimeMillis()
            );
            db.historyDao().insert(record);
        }).start();
    }
}
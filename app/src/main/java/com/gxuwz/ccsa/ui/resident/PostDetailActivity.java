package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.CommentAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Comment;
import com.gxuwz.ccsa.model.Post;
import com.gxuwz.ccsa.model.PostMedia;
import com.gxuwz.ccsa.model.User;
import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {
    private Post post;
    private User currentUser; // 当前登录用户
    private RecyclerView rvComments;
    private CommentAdapter adapter;
    private List<Comment> commentList = new ArrayList<>();
    private EditText etComment;

    private TextView tvName, tvContent;
    private ImageView ivAvatar, ivBack;
    private ViewPager2 viewPager;
    private VideoView videoView;
    private LinearLayout indicatorContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail_custom);

        post = (Post) getIntent().getSerializableExtra("post");
        // 获取传递过来的当前用户
        currentUser = (User) getIntent().getSerializableExtra("user");

        initViews();
        setupPostContent();
        setupComments();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        rvComments = findViewById(R.id.rv_comments);
        etComment = findViewById(R.id.et_comment);
        Button btnSend = findViewById(R.id.btn_send);

        tvName = findViewById(R.id.detail_name);
        tvContent = findViewById(R.id.detail_content);
        ivAvatar = findViewById(R.id.detail_avatar);

        viewPager = findViewById(R.id.view_pager_images);
        videoView = findViewById(R.id.detail_video_view);
        indicatorContainer = findViewById(R.id.indicator_container);

        ivBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            String content = etComment.getText().toString();
            if (!TextUtils.isEmpty(content)) {
                sendComment(content);
            } else {
                Toast.makeText(this, "请输入评论内容", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupPostContent() {
        if (post == null) return;

        tvName.setText(post.userName);
        tvContent.setText(post.content);

        // 加载帖子发布者的头像
        Glide.with(this)
                .load(post.userAvatar)
                .placeholder(R.drawable.lan)
                .error(R.drawable.lan)
                .into(ivAvatar);

        // 媒体处理
        if (post.mediaList != null && !post.mediaList.isEmpty()) {
            if (post.type == 2) {
                // === 视频 ===
                viewPager.setVisibility(View.GONE);
                indicatorContainer.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);

                videoView.setVideoPath(post.mediaList.get(0).url);
                videoView.start();
                // 点击暂停/播放
                videoView.setOnClickListener(v -> {
                    if (videoView.isPlaying()) videoView.pause();
                    else videoView.start();
                });
            } else {
                // === 图片 ===
                videoView.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
                ImagePagerAdapter imageAdapter = new ImagePagerAdapter(post.mediaList);
                viewPager.setAdapter(imageAdapter);
                setupIndicators(post.mediaList.size());
                viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        updateIndicators(position);
                    }
                });
            }
        } else {
            viewPager.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
            indicatorContainer.setVisibility(View.GONE);
        }
    }

    private void setupIndicators(int count) {
        if (count < 2) {
            indicatorContainer.setVisibility(View.GONE);
            return;
        }
        indicatorContainer.setVisibility(View.VISIBLE);
        indicatorContainer.removeAllViews();
        for (int i = 0; i < count; i++) {
            View view = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
            params.setMargins(5, 0, 5, 0);
            view.setLayoutParams(params);
            view.setBackgroundColor(0xFFE0E0E0);
            indicatorContainer.addView(view);
        }
        updateIndicators(0);
    }

    private void updateIndicators(int position) {
        int count = indicatorContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = indicatorContainer.getChildAt(i);
            view.setBackgroundColor(i == position ? 0xFF888888 : 0xFFE0E0E0);
        }
    }

    private void setupComments() {
        adapter = new CommentAdapter(this, commentList);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(adapter);
        loadComments();
    }

    private void loadComments() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<Comment> comments = db.postDao().getCommentsForPost(post.id);

            // 关键：同步评论用户的最新头像和昵称
            for (Comment c : comments) {
                User u = db.userDao().getUserById(c.userId);
                if (u != null) {
                    c.userName = u.getName();
                    c.userAvatar = u.getAvatar();
                }
            }

            runOnUiThread(() -> {
                commentList.clear();
                commentList.addAll(comments);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void sendComment(String content) {
        if (post == null) return;
        if (currentUser == null) {
            Toast.makeText(this, "用户未登录，无法评论", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            Comment comment = new Comment();
            comment.postId = post.id;
            comment.userId = currentUser.getId();
            comment.userName = currentUser.getName();
            comment.userAvatar = currentUser.getAvatar(); // 使用当前页面显示的头像
            comment.content = content;
            comment.createTime = System.currentTimeMillis();

            AppDatabase.getInstance(this).postDao().insertComment(comment);

            runOnUiThread(() -> {
                etComment.setText("");
                Toast.makeText(this, "评论成功", Toast.LENGTH_SHORT).show();
                loadComments(); // 重新加载以显示新评论
            });
        }).start();
    }

    class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {
        private List<PostMedia> mediaList;
        public ImagePagerAdapter(List<PostMedia> mediaList) { this.mediaList = mediaList; }
        @NonNull @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return new ImageViewHolder(imageView);
        }
        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            Glide.with(holder.imageView.getContext()).load(mediaList.get(position).url).into(holder.imageView);
        }
        @Override
        public int getItemCount() { return mediaList.size(); }
        class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            public ImageViewHolder(@NonNull View itemView) { super(itemView); this.imageView = (ImageView) itemView; }
        }
    }
}
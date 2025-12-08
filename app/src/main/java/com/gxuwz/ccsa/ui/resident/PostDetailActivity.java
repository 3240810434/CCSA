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
import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {
    private Post post;
    private RecyclerView rvComments;
    private CommentAdapter adapter;
    private List<Comment> commentList = new ArrayList<>();
    private EditText etComment;

    private TextView tvName, tvContent;
    private ImageView ivAvatar, ivBack;

    // 媒体相关
    private ViewPager2 viewPager;
    private VideoView videoView;
    private LinearLayout indicatorContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail_custom);

        post = (Post) getIntent().getSerializableExtra("post");

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

        // 头像处理
        if (post.userAvatar != null && !post.userAvatar.isEmpty()) {
            Glide.with(this).load(post.userAvatar).into(ivAvatar);
        }

        // 媒体处理
        if (post.mediaList != null && !post.mediaList.isEmpty()) {
            if (post.type == 2) {
                // === 视频 ===
                viewPager.setVisibility(View.GONE);
                indicatorContainer.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);

                videoView.setVideoPath(post.mediaList.get(0).url);
                videoView.start();
                videoView.setOnClickListener(v -> {
                    if (videoView.isPlaying()) videoView.pause();
                    else videoView.start();
                });
            } else {
                // === 图片 ===
                videoView.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);

                // 设置 ViewPager Adapter
                ImagePagerAdapter imageAdapter = new ImagePagerAdapter(post.mediaList);
                viewPager.setAdapter(imageAdapter);

                // 设置指示器
                setupIndicators(post.mediaList.size());

                // 监听滑动更新指示器
                viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        updateIndicators(position);
                    }
                });
            }
        } else {
            // 纯文本
            viewPager.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
            indicatorContainer.setVisibility(View.GONE);
        }
    }

    // 初始化指示器条 (灰色透明条)
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
            params.setMargins(5, 0, 5, 0); // 间隔
            view.setLayoutParams(params);
            view.setBackgroundColor(0xFFE0E0E0); // 默认浅灰
            indicatorContainer.addView(view);
        }
        updateIndicators(0);
    }

    // 更新指示器颜色
    private void updateIndicators(int position) {
        int count = indicatorContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = indicatorContainer.getChildAt(i);
            if (i == position) {
                view.setBackgroundColor(0xFF888888); // 选中深灰
            } else {
                view.setBackgroundColor(0xFFE0E0E0); // 未选中浅灰
            }
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
            List<Comment> comments = AppDatabase.getInstance(this).postDao().getCommentsForPost(post.id);
            runOnUiThread(() -> {
                commentList.clear();
                commentList.addAll(comments);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void sendComment(String content) {
        new Thread(() -> {
            Comment comment = new Comment();
            comment.postId = post.id;
            comment.userId = 1;
            comment.userName = "我";
            comment.content = content;
            comment.createTime = System.currentTimeMillis();
            AppDatabase.getInstance(this).postDao().insertComment(comment);
            runOnUiThread(() -> {
                etComment.setText("");
                Toast.makeText(this, "评论成功", Toast.LENGTH_SHORT).show();
                loadComments();
            });
        }).start();
    }

    // 内部类：图片 ViewPager 适配器
    class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {
        private List<PostMedia> mediaList;

        public ImagePagerAdapter(List<PostMedia> mediaList) {
            this.mediaList = mediaList;
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER); // 保持比例展示
            return new ImageViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            Glide.with(holder.imageView.getContext())
                    .load(mediaList.get(position).url)
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return mediaList.size();
        }

        class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            public ImageViewHolder(@NonNull View itemView) {
                super(itemView);
                this.imageView = (ImageView) itemView;
            }
        }
    }
}
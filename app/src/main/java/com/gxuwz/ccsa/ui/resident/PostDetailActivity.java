package com.gxuwz.ccsa.ui.resident;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.CommentAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Comment;
import com.gxuwz.ccsa.model.Post;
import com.gxuwz.ccsa.model.PostMedia;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.model.HistoryRecord;

import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {
    private Post post;
    private User currentUser;
    private RecyclerView rvComments;
    private CommentAdapter adapter;
    private List<Comment> commentList = new ArrayList<>();
    private EditText etComment;

    // Body Views
    private TextView tvName, tvContent;
    private ImageView ivAvatar, ivBack;
    private LinearLayout llBodyUserInfo;

    // Header Views (For Video)
    private LinearLayout llHeaderUserInfo;
    private ImageView ivHeaderAvatar;
    private TextView tvHeaderName;

    private ViewPager2 viewPager;
    private VideoView videoView;
    private FrameLayout videoContainer;
    private ImageView btnFullScreen;
    private LinearLayout indicatorContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail_custom);

        post = (Post) getIntent().getSerializableExtra("post");
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

        // Body User Info
        llBodyUserInfo = findViewById(R.id.ll_user_info_body);
        tvName = findViewById(R.id.detail_name);
        tvContent = findViewById(R.id.detail_content);
        ivAvatar = findViewById(R.id.detail_avatar);

        // Header User Info
        llHeaderUserInfo = findViewById(R.id.ll_header_user_info);
        ivHeaderAvatar = findViewById(R.id.header_avatar);
        tvHeaderName = findViewById(R.id.header_name);

        viewPager = findViewById(R.id.view_pager_images);

        // Video Views
        videoContainer = findViewById(R.id.video_container);
        videoView = findViewById(R.id.detail_video_view);
        btnFullScreen = findViewById(R.id.btn_fullscreen);

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

        // 基础内容
        tvContent.setText(post.content);

        // 加载头像逻辑
        RequestOptions options = RequestOptions.bitmapTransform(new RoundedCorners(100));

        // 1.1 根据帖子类型决定用户信息显示位置
        if (post.type == 2) {
            // === 视频帖子：用户信息在右上角 ===
            llBodyUserInfo.setVisibility(View.GONE);
            llHeaderUserInfo.setVisibility(View.VISIBLE);

            tvHeaderName.setText(post.userName);
            Glide.with(this).load(post.userAvatar).apply(options).into(ivHeaderAvatar);
        } else {
            // === 其他帖子：用户信息在内容上方 ===
            llHeaderUserInfo.setVisibility(View.GONE);
            llBodyUserInfo.setVisibility(View.VISIBLE);

            tvName.setText(post.userName);
            Glide.with(this).load(post.userAvatar).apply(options).into(ivAvatar);
        }

        // 媒体处理
        if (post.mediaList != null && !post.mediaList.isEmpty()) {
            if (post.type == 2) {
                // === 视频 ===
                viewPager.setVisibility(View.GONE);
                indicatorContainer.setVisibility(View.GONE);
                videoContainer.setVisibility(View.VISIBLE);

                String videoUrl = post.mediaList.get(0).url;
                videoView.setVideoPath(videoUrl);

                // 1.2 视频居中显示，准备好后开始播放
                videoView.setOnPreparedListener(mp -> {
                    mp.setLooping(true);
                    mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                    videoView.start();
                });

                // 点击暂停/播放
                videoView.setOnClickListener(v -> {
                    if (videoView.isPlaying()) videoView.pause();
                    else videoView.start();
                });

                // 1.2 点击全屏图标
                btnFullScreen.setOnClickListener(v -> {
                    Intent intent = new Intent(this, VideoFullScreenActivity.class);
                    intent.putExtra("video_url", videoUrl);
                    startActivity(intent);
                });

            } else {
                // === 图片 ===
                videoContainer.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);

                // 1.4 传递 mediaList 用于点击放大
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
            videoContainer.setVisibility(View.GONE);
            indicatorContainer.setVisibility(View.GONE);
        }

        // 保存浏览历史
        if (currentUser != null) {
            saveHistory();
        }
    }

    private void saveHistory() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            // 先删除旧的记录，保证最新浏览排在最前 (1 代表 Post 类型)
            db.historyDao().deleteRecord(currentUser.getId(), post.id, 1);

            String cover = post.userAvatar; // 默认用头像
            if (post.mediaList != null && !post.mediaList.isEmpty()) {
                // 如果有图片或视频封面，则使用第一张媒体图
                cover = post.mediaList.get(0).url;
            }

            // 确保标题不为空
            String title = !TextUtils.isEmpty(post.content) ? post.content : post.userName + "的动态";

            HistoryRecord record = new HistoryRecord(
                    currentUser.getId(),
                    post.id,
                    1, // 1 for Post
                    title,
                    cover,
                    post.userName,
                    System.currentTimeMillis()
            );
            db.historyDao().insert(record);
        }).start();
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

        // 【新增】设置当前用户的ID，以便Adapter中判断是否可以删除评论
        if (currentUser != null) {
            adapter.setCurrentUserId(currentUser.getId());
        }

        // 【新增】实现点击回复的回调逻辑
        adapter.setOnReplyListener(comment -> {
            // 1. 设置输入框内容为 "回复 xxx: "
            String replyPrefix = "回复 " + comment.userName + ": ";
            etComment.setText(replyPrefix);

            // 2. 将光标移到文本末尾
            etComment.setSelection(etComment.getText().length());

            // 3. 获取焦点并弹出软键盘
            etComment.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etComment, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(adapter);
        loadComments();
    }

    private void loadComments() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<Comment> comments = db.postDao().getCommentsForPost(post.id);
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
        if (post == null || currentUser == null) {
            Toast.makeText(this, currentUser == null ? "请先登录" : "数据错误", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            Comment comment = new Comment();
            comment.postId = post.id;
            comment.userId = currentUser.getId();
            comment.userName = currentUser.getName();
            comment.userAvatar = currentUser.getAvatar();
            comment.content = content;
            comment.createTime = System.currentTimeMillis();
            AppDatabase.getInstance(this).postDao().insertComment(comment);
            runOnUiThread(() -> {
                etComment.setText("");
                // 发送完成后关闭键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
                }
                Toast.makeText(this, "评论成功", Toast.LENGTH_SHORT).show();
                loadComments();
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

            // 1.4 点击图片放大 (跳转至 ImagePreviewActivity)
            holder.imageView.setOnClickListener(v -> {
                ArrayList<String> urls = new ArrayList<>();
                for (PostMedia m : mediaList) urls.add(m.url);

                Intent intent = new Intent(PostDetailActivity.this, ImagePreviewActivity.class);
                intent.putStringArrayListExtra("images", urls);
                intent.putExtra("position", position);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return mediaList.size(); }

        class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            public ImageViewHolder(@NonNull View itemView) { super(itemView); this.imageView = (ImageView) itemView; }
        }
    }
}
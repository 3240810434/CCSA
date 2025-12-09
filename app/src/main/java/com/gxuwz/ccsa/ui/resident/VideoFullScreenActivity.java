package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import com.gxuwz.ccsa.R;

public class VideoFullScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_video_full_screen);

        String videoUrl = getIntent().getStringExtra("video_url");
        VideoView videoView = findViewById(R.id.vv_fullscreen);
        ImageView ivClose = findViewById(R.id.iv_close_fullscreen);

        if (videoUrl != null) {
            videoView.setVideoPath(videoUrl);
            MediaController mediaController = new MediaController(this);
            videoView.setMediaController(mediaController);
            videoView.start();
        }

        ivClose.setOnClickListener(v -> finish());
    }
}
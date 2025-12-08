package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import java.util.List;

public class ImagePreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        ViewPager2 viewPager = findViewById(R.id.view_pager_preview);
        ImageView ivClose = findViewById(R.id.iv_close);

        List<String> imageUrls = getIntent().getStringArrayListExtra("images");
        int position = getIntent().getIntExtra("position", 0);

        if (imageUrls != null) {
            PreviewAdapter adapter = new PreviewAdapter(imageUrls);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(position, false);
        }

        ivClose.setOnClickListener(v -> finish());
    }

    static class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.ViewHolder> {
        private List<String> urls;

        public PreviewAdapter(List<String> urls) {
            this.urls = urls;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return new ViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Glide.with(holder.itemView)
                    .load(urls.get(position))
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return urls.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ViewHolder(View itemView) {
                super(itemView);
                this.imageView = (ImageView) itemView;
            }
        }
    }
}
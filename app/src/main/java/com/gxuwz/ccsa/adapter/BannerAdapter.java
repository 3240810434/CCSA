package com.gxuwz.ccsa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
    private Context mContext;
    private List<String> mBannerImages; // 修改为 String 类型的图片路径
    private OnBannerClickListener mListener; // 点击事件接口

    public interface OnBannerClickListener {
        void onBannerClick(String imageUrl);
    }

    public BannerAdapter(Context context, List<String> bannerImages) {
        this.mContext = context;
        this.mBannerImages = bannerImages;
    }

    public void setOnBannerClickListener(OnBannerClickListener listener) {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        String imageUrl = mBannerImages.get(position);
        // 使用 Glide 加载图片
        Glide.with(mContext)
                .load(imageUrl)
                .placeholder(R.drawable.shopping) // 加载中占位图
                .error(R.drawable.shopping)       // 错误占位图
                .into(holder.bannerImage);

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onBannerClick(imageUrl);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBannerImages == null ? 0 : mBannerImages.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView bannerImage;

        BannerViewHolder(View itemView) {
            super(itemView);
            bannerImage = itemView.findViewById(R.id.iv_banner);
        }
    }
}
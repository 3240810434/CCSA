package com.gxuwz.ccsa.ui.merchant;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Product;
import com.gxuwz.ccsa.util.DateUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProductManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private int currentMerchantId = 1; // 这里应从 SharedPreferences 获取当前登录商家的ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);

        // 初始化UI
        ImageView btnBack = findViewById(R.id.btn_back);
        ImageView btnAdd = findViewById(R.id.btn_add);
        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);

        btnBack.setOnClickListener(v -> finish());

        // 1.2 点击发布按钮，弹出底部面板
        btnAdd.setOnClickListener(v -> showPublishTypeDialog());

        // 1.1 初始化列表
        adapter = new ProductAdapter();
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            // 获取当前商家的商品
            productList = AppDatabase.getInstance(this).productDao().getProductsByMerchantId(currentMerchantId);
            runOnUiThread(() -> {
                if (productList == null || productList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
            });
        }).start();
    }

    // 1.3 显示底部发布类型选择弹窗
    private void showPublishTypeDialog() {
        Dialog dialog = new Dialog(this, R.style.BottomDialogTheme);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_publish_type_selection, null);

        view.findViewById(R.id.btn_close).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.card_goods).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, PhysicalProductEditActivity.class));
        });
        view.findViewById(R.id.card_service).setOnClickListener(v -> {
            dialog.dismiss();
            // 1.7 跳转到服务编辑页面（暂为空）
            startActivity(new Intent(this, ServiceEditActivity.class));
        });

        dialog.setContentView(view);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.gravity = Gravity.BOTTOM;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.4); // 40% 高度
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setWindowAnimations(R.style.BottomDialogAnim);
        dialog.show();
    }

    // Adapter Class
    class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_card_merchant, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Product product = productList.get(position);
            holder.tvName.setText(product.name);

            // 解析第一张图片
            if (product.imagePaths != null && !product.imagePaths.isEmpty()) {
                String firstImage = product.imagePaths.split(",")[0];
                Glide.with(ProductManagementActivity.this).load(firstImage).into(holder.ivCover);
            } else {
                holder.ivCover.setImageResource(R.drawable.shopping); // 默认图
            }

            // 1.1 解析价格表第一行
            try {
                if (product.priceTableJson != null) {
                    JSONArray jsonArray = new JSONArray(product.priceTableJson);
                    if (jsonArray.length() > 0) {
                        JSONObject firstRow = jsonArray.getJSONObject(0);
                        String desc = firstRow.optString("desc");
                        String price = firstRow.optString("price");
                        holder.tvPrice.setText(desc + " ¥" + price);
                    }
                }
            } catch (Exception e) {
                holder.tvPrice.setText("¥ --");
            }

            // 1.8 点击跳转详情
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ProductManagementActivity.this, MerchantProductDetailActivity.class);
                intent.putExtra("product_id", product.id);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return productList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivCover;
            TextView tvName, tvPrice;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivCover = itemView.findViewById(R.id.iv_cover);
                tvName = itemView.findViewById(R.id.tv_name);
                tvPrice = itemView.findViewById(R.id.tv_price);
            }
        }
    }
}
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Product;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProductManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private MerchantProductAdapter adapter; // 使用内部类 Adapter 避免冲突
    private List<Product> productList = new ArrayList<>();
    private int currentMerchantId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);

        ImageView btnBack = findViewById(R.id.btn_back);
        ImageView btnAdd = findViewById(R.id.btn_add);
        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);

        btnBack.setOnClickListener(v -> finish());
        btnAdd.setOnClickListener(v -> showPublishTypeDialog());

        adapter = new MerchantProductAdapter();
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
            // 现在 ProductDao 已经有这个方法了
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

    private void showPublishTypeDialog() {
        // 使用我们在 styles.xml 定义的 BottomDialogTheme
        Dialog dialog = new Dialog(this, R.style.BottomDialogTheme);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_publish_type_selection, null);

        view.findViewById(R.id.btn_close).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.card_goods).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, PhysicalProductEditActivity.class));
        });
        view.findViewById(R.id.card_service).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, ServiceEditActivity.class));
        });

        dialog.setContentView(view);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.gravity = Gravity.BOTTOM;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.4);
        dialog.getWindow().setAttributes(lp);
        // 使用 styles.xml 定义的动画
        dialog.getWindow().setWindowAnimations(R.style.BottomDialogAnim);
        dialog.show();
    }

    // 内部 Adapter 类
    class MerchantProductAdapter extends RecyclerView.Adapter<MerchantProductAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // 引用我们在第四步创建的 item_product_card_merchant
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_card_merchant, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Product product = productList.get(position);
            holder.tvName.setText(product.name);

            if (product.imagePaths != null && !product.imagePaths.isEmpty()) {
                String firstImage = product.imagePaths.split(",")[0];
                Glide.with(ProductManagementActivity.this).load(firstImage).into(holder.ivCover);
            } else {
                holder.ivCover.setImageResource(R.drawable.shopping);
            }

            try {
                if (product.priceTableJson != null) {
                    JSONArray jsonArray = new JSONArray(product.priceTableJson);
                    if (jsonArray.length() > 0) {
                        JSONObject firstRow = jsonArray.getJSONObject(0);
                        holder.tvPrice.setText(firstRow.optString("desc") + " ¥" + firstRow.optString("price"));
                    }
                } else {
                    holder.tvPrice.setText("¥" + product.price);
                }
            } catch (Exception e) {
                holder.tvPrice.setText("¥ --");
            }

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
                // 确保这些 ID 在 item_product_card_merchant.xml 中存在
                ivCover = itemView.findViewById(R.id.iv_cover);
                tvName = itemView.findViewById(R.id.tv_name);
                tvPrice = itemView.findViewById(R.id.tv_price);
            }
        }
    }
}
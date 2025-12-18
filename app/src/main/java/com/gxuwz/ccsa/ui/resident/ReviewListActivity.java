package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.ReviewAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.ProductReview;
import java.util.List;

public class ReviewListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private int productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_list);

        productId = getIntent().getIntExtra("product_id", -1);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        recyclerView = findViewById(R.id.recycler_reviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            List<ProductReview> reviews = AppDatabase.getInstance(this).productReviewDao().getAllReviews(productId);
            runOnUiThread(() -> {
                if (reviews != null) {
                    ReviewAdapter adapter = new ReviewAdapter(this, reviews);
                    recyclerView.setAdapter(adapter);
                }
            });
        }).start();
    }
}
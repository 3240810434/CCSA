package com.gxuwz.ccsa.ui.resident;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.gxuwz.ccsa.R;

public class ResidentAfterSalesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_after_sales);

        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("售后服务");

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
    }
}
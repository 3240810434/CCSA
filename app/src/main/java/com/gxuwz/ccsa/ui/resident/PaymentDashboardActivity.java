package com.gxuwz.ccsa.ui.resident;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.PaymentRecordAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.PaymentRecord;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.util.SharedPreferencesUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PaymentDashboardActivity extends AppCompatActivity {

    private PieChart pieChart;
    private BarChart barChart;
    private RecyclerView recyclerView;
    private TextView tvTotalYearly;
    private Spinner spYear;
    private PaymentRecordAdapter adapter;
    private User currentUser;
    private List<PaymentRecord> allRecords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_dashboard);

        currentUser = SharedPreferencesUtil.getUser(this);
        initViews();
        loadData();
    }

    private void initViews() {
        pieChart = findViewById(R.id.chart_pie);
        barChart = findViewById(R.id.chart_bar);
        recyclerView = findViewById(R.id.recycler_view_records);
        tvTotalYearly = findViewById(R.id.tv_total_yearly);
        spYear = findViewById(R.id.sp_year);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        // 初始化列表
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaymentRecordAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // 初始化年份选择器
        setupYearSpinner();
    }

    private void setupYearSpinner() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        // 提供最近3年的选项
        String[] years = {String.valueOf(currentYear), String.valueOf(currentYear - 1), String.valueOf(currentYear - 2)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spYear.setAdapter(adapter);
        spYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateChartsAndList(Integer.parseInt(years[position]));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadData() {
        new Thread(() -> {
            // 获取用户所有支付记录
            allRecords = AppDatabase.getInstance(this)
                    .paymentRecordDao()
                    .getByPhone(currentUser.getPhone());

            runOnUiThread(() -> {
                // 默认加载当前年份数据
                if (spYear.getSelectedItem() != null) {
                    String selectedYear = spYear.getSelectedItem().toString();
                    updateChartsAndList(Integer.parseInt(selectedYear));
                }
            });
        }).start();
    }

    private void updateChartsAndList(int year) {
        List<PaymentRecord> yearRecords = new ArrayList<>();
        double totalAmount = 0;

        // 1. 筛选选中年份的数据
        Calendar cal = Calendar.getInstance();
        for (PaymentRecord record : allRecords) {
            cal.setTimeInMillis(record.getPayTime());
            if (cal.get(Calendar.YEAR) == year) {
                yearRecords.add(record);
                totalAmount += record.getAmount();
            }
        }

        // 2. 更新列表和总金额
        adapter.updateData(yearRecords);
        tvTotalYearly.setText(String.format("¥ %.2f", totalAmount));

        // 3. 更新图表
        if (yearRecords.isEmpty()) {
            pieChart.clear();
            barChart.clear();
        } else {
            updatePieChart(yearRecords);
            updateBarChart(yearRecords);
        }
    }

    private void updatePieChart(List<PaymentRecord> records) {
        // 使用 LinkedHashMap 保持插入顺序，确保图例顺序一致
        Map<String, Double> costMap = new LinkedHashMap<>();
        costMap.put("物业费", 0.0);
        costMap.put("维修金", 0.0);
        costMap.put("水电公摊", 0.0);
        costMap.put("电梯费", 0.0);
        costMap.put("加压费", 0.0);
        costMap.put("垃圾费", 0.0);
        costMap.put("其他/历史", 0.0); // 新增：用于处理没有明细的记录

        for (PaymentRecord record : records) {
            boolean hasDetail = false;
            // 尝试解析费用明细快照
            if (record.getFeeDetailsSnapshot() != null && !record.getFeeDetailsSnapshot().isEmpty()) {
                try {
                    JSONObject json = new JSONObject(record.getFeeDetailsSnapshot());
                    // 累加各项费用，注意这里使用英文key对应JSON中的字段
                    costMap.put("物业费", costMap.get("物业费") + json.optDouble("property", 0));
                    costMap.put("维修金", costMap.get("维修金") + json.optDouble("maintenance", 0));
                    costMap.put("水电公摊", costMap.get("水电公摊") + json.optDouble("utility", 0));
                    costMap.put("电梯费", costMap.get("电梯费") + json.optDouble("elevator", 0));
                    costMap.put("加压费", costMap.get("加压费") + json.optDouble("pressure", 0));
                    costMap.put("垃圾费", costMap.get("垃圾费") + json.optDouble("garbage", 0));
                    hasDetail = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 如果没有明细（旧数据）或解析失败，将整笔金额归入“其他”
            if (!hasDetail) {
                costMap.put("其他/历史", costMap.get("其他/历史") + record.getAmount());
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : costMap.entrySet()) {
            // 只有金额大于0的项才显示在图中
            if (entry.getValue() > 0.01) {
                entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        // 设置丰富的颜色
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.MATERIAL_COLORS) colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS) colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS) colors.add(c);
        dataSet.setColors(colors);

        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new PercentFormatter(pieChart)); // 显示百分比

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        // 样式设置
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setCenterText("支出构成");

        // 图例设置
        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setWordWrapEnabled(true);

        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void updateBarChart(List<PaymentRecord> records) {
        float[] monthlyTotals = new float[12];
        Calendar cal = Calendar.getInstance();

        // 统计每月总支出
        for (PaymentRecord record : records) {
            cal.setTimeInMillis(record.getPayTime());
            int month = cal.get(Calendar.MONTH); // 0-11
            monthlyTotals[month] += (float) record.getAmount();
        }

        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            entries.add(new BarEntry(i, monthlyTotals[i]));
        }

        BarDataSet dataSet = new BarDataSet(entries, "月度支出 (元)");
        dataSet.setColor(getResources().getColor(R.color.teal_200));
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        barChart.setData(data);
        barChart.getDescription().setEnabled(false);

        // X轴设置
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{
                "1月","2月","3月","4月","5月","6月","7月","8月","9月","10月","11月","12月"
        }));

        // 确保Y轴从0开始
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisRight().setEnabled(false); // 隐藏右侧Y轴

        barChart.animateY(1000);
        barChart.invalidate();
    }
}
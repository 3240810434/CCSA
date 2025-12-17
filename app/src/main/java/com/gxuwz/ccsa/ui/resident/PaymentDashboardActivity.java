package com.gxuwz.ccsa.ui.resident;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.PaymentRecordAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.PaymentRecord;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.util.SharedPreferencesUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PaymentDashboardActivity extends AppCompatActivity {

    private PieChart pieChart;
    private BarChart barChart;
    private RecyclerView recyclerView;
    private TextView tvTotalYearly;
    private TextView tvMonthFilter;
    private Spinner spYear;
    private PaymentRecordAdapter adapter;
    private User currentUser;
    private List<PaymentRecord> allRecords = new ArrayList<>();

    // 筛选状态
    private int currentSelectedYear;
    private boolean[] selectedMonthsState = new boolean[12];
    private final String[] monthLabels = new String[]{
            "1月", "2月", "3月", "4月", "5月", "6月",
            "7月", "8月", "9月", "10月", "11月", "12月"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_dashboard);

        // 默认选中所有月份
        Arrays.fill(selectedMonthsState, true);

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
        tvMonthFilter = findViewById(R.id.tv_month_filter);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        // 初始化列表
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaymentRecordAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // 点击月份筛选
        tvMonthFilter.setOnClickListener(v -> showMonthFilterDialog());

        // 初始化年份选择器
        setupYearSpinner();

        // 初始化图表的基本配置
        initBarChartSettings();
    }

    private void setupYearSpinner() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        currentSelectedYear = currentYear;
        // 提供最近5年的选项
        String[] years = {
                String.valueOf(currentYear),
                String.valueOf(currentYear - 1),
                String.valueOf(currentYear - 2),
                String.valueOf(currentYear - 3),
                String.valueOf(currentYear - 4)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spYear.setAdapter(adapter);
        spYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSelectedYear = Integer.parseInt(years[position]);
                updateUI(); // 年份改变时刷新
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showMonthFilterDialog() {
        boolean[] tempState = Arrays.copyOf(selectedMonthsState, selectedMonthsState.length);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择月份");
        builder.setMultiChoiceItems(monthLabels, tempState, (dialog, which, isChecked) -> {
            tempState[which] = isChecked;
        });

        builder.setPositiveButton("确定", (dialog, which) -> {
            boolean hasSelection = false;
            for (boolean b : tempState) {
                if (b) {
                    hasSelection = true;
                    break;
                }
            }
            if (!hasSelection) {
                Toast.makeText(this, "请至少选择一个月", Toast.LENGTH_SHORT).show();
                return;
            }
            System.arraycopy(tempState, 0, selectedMonthsState, 0, tempState.length);
            updateMonthFilterText();
            updateUI();
        });

        builder.setNegativeButton("取消", null);
        builder.setNeutralButton("全选", (dialog, which) -> {
            Arrays.fill(selectedMonthsState, true);
            updateMonthFilterText();
            updateUI();
        });
        builder.show();
    }

    private void updateMonthFilterText() {
        int count = 0;
        for (boolean b : selectedMonthsState) {
            if (b) count++;
        }
        if (count == 12) {
            tvMonthFilter.setText("全部月份 ▼");
        } else {
            tvMonthFilter.setText("已选 " + count + " 个月 ▼");
        }
    }

    private void loadData() {
        new Thread(() -> {
            allRecords = AppDatabase.getInstance(this)
                    .paymentRecordDao()
                    .getByPhone(currentUser.getPhone());
            runOnUiThread(this::updateUI);
        }).start();
    }

    /**
     * 统一更新UI的方法
     */
    private void updateUI() {
        List<PaymentRecord> yearRecords = new ArrayList<>();     // 当前年份的所有记录（用于柱状图趋势）
        List<PaymentRecord> filteredRecords = new ArrayList<>(); // 当前筛选条件的记录（用于列表、饼图、总计）

        double totalAmount = 0;
        Calendar cal = Calendar.getInstance();

        // 第一次遍历：分离数据
        for (PaymentRecord record : allRecords) {
            cal.setTimeInMillis(record.getPayTime());
            int recordYear = cal.get(Calendar.YEAR);
            int recordMonth = cal.get(Calendar.MONTH); // 0-11

            if (recordYear == currentSelectedYear) {
                // 只要是当年的，都加入趋势图数据源
                yearRecords.add(record);

                // 只有被选中的月份，才加入列表和饼图数据源
                if (selectedMonthsState[recordMonth]) {
                    filteredRecords.add(record);
                    totalAmount += record.getAmount();
                }
            }
        }

        // 1. 更新列表和总金额
        adapter.updateData(filteredRecords);
        tvTotalYearly.setText(String.format("¥ %.2f", totalAmount));

        // 2. 更新饼图 (使用筛选后的数据)
        if (filteredRecords.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("当前筛选月份无数据");
        } else {
            updatePieChart(filteredRecords);
        }

        // 3. 更新柱状图 (使用当年所有数据，展示完整趋势)
        updateBarChart(yearRecords);
    }

    private void updatePieChart(List<PaymentRecord> records) {
        Map<String, Double> costMap = new LinkedHashMap<>();
        // 初始化类别
        String[] types = {"物业费", "维修金", "水电公摊", "电梯费", "加压费", "垃圾费", "其他"};
        for(String t : types) costMap.put(t, 0.0);

        for (PaymentRecord record : records) {
            boolean hasDetail = false;
            if (record.getFeeDetailsSnapshot() != null && !record.getFeeDetailsSnapshot().isEmpty()) {
                try {
                    JSONObject json = new JSONObject(record.getFeeDetailsSnapshot());
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
            if (!hasDetail) {
                costMap.put("其他", costMap.get("其他") + record.getAmount());
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : costMap.entrySet()) {
            if (entry.getValue() > 0.01) {
                entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.MATERIAL_COLORS) colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS) colors.add(c);
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("支出构成");

        Legend l = pieChart.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        pieChart.animateY(800);
        pieChart.invalidate();
    }

    private void initBarChartSettings() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);

        // X轴配置
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(12); // 强制显示12个标签
        xAxis.setValueFormatter(new IndexAxisValueFormatter(monthLabels));

        // Y轴配置
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        barChart.getAxisRight().setEnabled(false);
    }

    /**
     * 重写的柱状图逻辑，确保显示1-12月数据
     */
    private void updateBarChart(List<PaymentRecord> records) {
        // 1. 初始化12个月的数据桶
        float[] monthlyTotals = new float[12];
        Calendar cal = Calendar.getInstance();

        // 2. 统计每月的总费用
        for (PaymentRecord record : records) {
            cal.setTimeInMillis(record.getPayTime());
            int month = cal.get(Calendar.MONTH); // 0-11
            if (month >= 0 && month < 12) {
                monthlyTotals[month] += (float) record.getAmount();
            }
        }

        // 3. 构造12个BarEntry，即使金额为0也需要创建，保证X轴对其
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            entries.add(new BarEntry(i, monthlyTotals[i]));
        }

        BarDataSet dataSet;
        if (barChart.getData() != null && barChart.getData().getDataSetCount() > 0) {
            dataSet = (BarDataSet) barChart.getData().getDataSetByIndex(0);
            dataSet.setValues(entries);
            barChart.getData().notifyDataChanged();
            barChart.notifyDataSetChanged();
        } else {
            dataSet = new BarDataSet(entries, "月度支出 (元)");
            dataSet.setColor(getResources().getColor(R.color.teal_200));
            // 设置显示数值在柱子上方
            dataSet.setDrawValues(true);
            dataSet.setValueTextSize(10f);
            dataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    // 只有大于0才显示数字，避免图表太乱
                    return value > 0 ? String.format("%.0f", value) : "";
                }
            });

            BarData data = new BarData(dataSet);
            data.setBarWidth(0.5f);
            barChart.setData(data);
        }

        // 修正X轴范围，让柱子居中显示
        barChart.getXAxis().setAxisMinimum(-0.5f);
        barChart.getXAxis().setAxisMaximum(11.5f);

        barChart.animateY(1000);
        barChart.invalidate();
    }
}
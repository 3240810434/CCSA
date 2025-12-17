package com.gxuwz.ccsa.ui.resident;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PaymentDashboardActivity extends AppCompatActivity {

    private PieChart pieChart;
    // private BarChart barChart; // 删除 BarChart
    private TableLayout tableReport; // 新增 TableLayout
    private RecyclerView recyclerView;
    private TextView tvTotalYearly;
    private TextView tvMonthFilter;
    private Spinner spYear;
    private PaymentRecordAdapter adapter;
    private User currentUser;

    // 原始数据
    private List<PaymentRecord> allRecords = new ArrayList<>();

    // 筛选状态
    private int currentSelectedYear;
    // 记录12个月的选中状态，默认全选 (true)
    private boolean[] selectedMonthsState = new boolean[12];

    // 标签，对应索引 0-11
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
        // barChart = findViewById(R.id.chart_bar); // 删除
        tableReport = findViewById(R.id.table_report); // 绑定 TableLayout

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

        // 移除 initBarChartSettings();
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
     * 核心刷新方法
     */
    private void updateUI() {
        // 1. 准备数据容器
        List<PaymentRecord> yearRecordsForReport = new ArrayList<>(); // 用于报表（当年所有数据）
        List<PaymentRecord> filteredListForList = new ArrayList<>(); // 用于列表和总计（受月份筛选影响）

        double totalAmount = 0;
        Calendar cal = Calendar.getInstance();

        // 2. 遍历所有记录进行归类
        for (PaymentRecord record : allRecords) {
            cal.setTimeInMillis(record.getPayTime());
            int recordYear = cal.get(Calendar.YEAR);
            int recordMonth = cal.get(Calendar.MONTH); // 0-11

            if (recordYear == currentSelectedYear) {
                // 报表使用当前选中年份的所有数据，展示完整趋势
                yearRecordsForReport.add(record);

                // 列表和总金额计算受月份筛选限制
                if (recordMonth >= 0 && recordMonth < 12 && selectedMonthsState[recordMonth]) {
                    filteredListForList.add(record);
                    totalAmount += record.getAmount();
                }
            }
        }

        // 3. 更新视图
        adapter.updateData(filteredListForList);
        tvTotalYearly.setText(String.format("¥ %.2f", totalAmount));

        // 更新饼图 (基于筛选后的数据)
        if (filteredListForList.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("无数据");
        } else {
            updatePieChart(filteredListForList);
        }

        // 更新报表 (基于当年数据，替代 BarChart)
        updateReportTable(yearRecordsForReport);
    }

    /**
     * 生成月度/年度费用明细报表表格
     */
    private void updateReportTable(List<PaymentRecord> records) {
        tableReport.removeAllViews(); // 清除旧数据

        // 1. 统计每个月的数据
        double[] monthlyAmounts = new double[12];
        int[] monthlyCounts = new int[12];
        Arrays.fill(monthlyAmounts, 0);
        Arrays.fill(monthlyCounts, 0);

        Calendar cal = Calendar.getInstance();
        boolean hasData = false;

        for (PaymentRecord record : records) {
            cal.setTimeInMillis(record.getPayTime());
            int month = cal.get(Calendar.MONTH);
            if (month >= 0 && month < 12) {
                monthlyAmounts[month] += record.getAmount();
                monthlyCounts[month]++;
                hasData = true;
            }
        }

        if (!hasData) {
            TableRow emptyRow = new TableRow(this);
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("该年度暂无缴费记录");
            tvEmpty.setGravity(Gravity.CENTER);
            tvEmpty.setPadding(0, 40, 0, 40);
            emptyRow.addView(tvEmpty);
            // 合并列需要 LayoutParams
            TableRow.LayoutParams params = new TableRow.LayoutParams();
            params.span = 3;
            tvEmpty.setLayoutParams(params);
            tableReport.addView(emptyRow);
            return;
        }

        // 2. 动态添加行
        // 倒序显示，最近的月份在上面
        for (int i = 11; i >= 0; i--) {
            // 只显示有数据的月份，或者您可以选择显示所有月份
            if (monthlyCounts[i] > 0) {
                TableRow row = new TableRow(this);
                row.setPadding(0, 15, 0, 15);
                row.setBackgroundResource(R.drawable.bg_edittext_underline); // 使用下划线背景或自定义背景

                // 月份列
                TextView tvMonth = new TextView(this);
                tvMonth.setText(monthLabels[i]);
                tvMonth.setGravity(Gravity.CENTER);
                tvMonth.setTextColor(Color.parseColor("#333333"));

                // 笔数列
                TextView tvCount = new TextView(this);
                tvCount.setText(String.valueOf(monthlyCounts[i]));
                tvCount.setGravity(Gravity.CENTER);
                tvCount.setTextColor(Color.parseColor("#666666"));

                // 金额列
                TextView tvAmount = new TextView(this);
                tvAmount.setText(String.format("¥ %.2f", monthlyAmounts[i]));
                tvAmount.setGravity(Gravity.CENTER);
                tvAmount.setTextColor(Color.parseColor("#FF5722")); // 醒目颜色
                tvAmount.setTypeface(null, android.graphics.Typeface.BOLD);

                // 添加到行，使用 layout_weight 效果需要配合 TableLayout 的 stretchColumns
                // 这里简单添加，由 TableLayout 自动分配
                row.addView(tvMonth);
                row.addView(tvCount);
                row.addView(tvAmount);

                tableReport.addView(row);
            }
        }
    }

    private void updatePieChart(List<PaymentRecord> records) {
        Map<String, Double> typeMap = new LinkedHashMap<>();
        String[] keys = {"物业费", "维修金", "公摊水电", "电梯费", "其他"};
        for(String k : keys) typeMap.put(k, 0.0);

        for (PaymentRecord r : records) {
            boolean parsed = false;
            if (r.getFeeDetailsSnapshot() != null && !r.getFeeDetailsSnapshot().isEmpty()) {
                try {
                    JSONObject json = new JSONObject(r.getFeeDetailsSnapshot());
                    double property = json.optDouble("property", 0);
                    double maintenance = json.optDouble("maintenance", 0);
                    double utility = json.optDouble("utility", 0);
                    double elevator = json.optDouble("elevator", 0);

                    if (property > 0) typeMap.put("物业费", typeMap.get("物业费") + property);
                    if (maintenance > 0) typeMap.put("维修金", typeMap.get("维修金") + maintenance);
                    if (utility > 0) typeMap.put("公摊水电", typeMap.get("公摊水电") + utility);
                    if (elevator > 0) typeMap.put("电梯费", typeMap.get("电梯费") + elevator);

                    double subTotal = property + maintenance + utility + elevator;
                    if (r.getAmount() > subTotal + 0.01) {
                        typeMap.put("其他", typeMap.get("其他") + (r.getAmount() - subTotal));
                    }
                    parsed = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!parsed) {
                typeMap.put("其他", typeMap.get("其他") + r.getAmount());
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : typeMap.entrySet()) {
            if (entry.getValue() > 0.01) {
                entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }
        }

        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        set.setValueTextSize(12f);
        set.setValueTextColor(Color.WHITE);
        set.setValueFormatter(new PercentFormatter(pieChart));

        PieData data = new PieData(set);
        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("费用构成");

        // 设置图例
        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setWordWrapEnabled(true);

        pieChart.invalidate();
    }
}
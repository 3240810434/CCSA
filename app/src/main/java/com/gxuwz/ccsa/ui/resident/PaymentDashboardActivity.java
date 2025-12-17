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

    // 原始数据
    private List<PaymentRecord> allRecords = new ArrayList<>();

    // 筛选状态
    private int currentSelectedYear;
    // 记录12个月的选中状态，默认全选 (true)
    private boolean[] selectedMonthsState = new boolean[12];

    // X轴标签，对应索引 0-11
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

        // 预设图表样式
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
        // 修复：使用系统自带的 dropdown_item 布局
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
            // 检查是否至少选择了一个
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
            // 应用选择
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
        List<PaymentRecord> yearRecordsForChart = new ArrayList<>(); // 用于柱状图
        List<PaymentRecord> filteredListForList = new ArrayList<>(); // 用于列表和总计

        double totalAmount = 0;
        Calendar cal = Calendar.getInstance();

        // 2. 遍历所有记录进行归类
        for (PaymentRecord record : allRecords) {
            cal.setTimeInMillis(record.getPayTime());
            int recordYear = cal.get(Calendar.YEAR);
            int recordMonth = cal.get(Calendar.MONTH); // 0-11

            if (recordYear == currentSelectedYear) {
                // 只要是这一年的，都算进图表数据源，保证图表显示全年趋势
                yearRecordsForChart.add(record);

                // 只有被勾选的月份，才算进列表和总金额
                if (recordMonth >= 0 && recordMonth < 12 && selectedMonthsState[recordMonth]) {
                    filteredListForList.add(record);
                    totalAmount += record.getAmount();
                }
            }
        }

        // 3. 更新各个视图
        adapter.updateData(filteredListForList);
        tvTotalYearly.setText(String.format("¥ %.2f", totalAmount));

        // 更新饼图 (基于筛选后的数据)
        if (filteredListForList.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("无数据");
        } else {
            updatePieChart(filteredListForList);
        }

        // 更新柱状图 (基于当年所有数据)
        updateBarChart(yearRecordsForChart);
    }

    // --- 图表相关方法 ---

    private void initBarChartSettings() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // 间隔为1
        xAxis.setLabelCount(12);  // 强制12个标签
        xAxis.setValueFormatter(new IndexAxisValueFormatter(monthLabels));

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // Y轴从0开始

        barChart.getAxisRight().setEnabled(false); // 隐藏右侧Y轴
        barChart.setExtraBottomOffset(10f); // 底部留白
    }

    private void updateBarChart(List<PaymentRecord> records) {
        // 1. 初始化12个桶
        float[] monthlyAmounts = new float[12];
        Arrays.fill(monthlyAmounts, 0f);

        Calendar cal = Calendar.getInstance();
        for (PaymentRecord record : records) {
            cal.setTimeInMillis(record.getPayTime());
            int month = cal.get(Calendar.MONTH); // 0-11
            if (month >= 0 && month < 12) {
                monthlyAmounts[month] += (float) record.getAmount();
            }
        }

        // 2. 构建 BarEntry
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            entries.add(new BarEntry(i, monthlyAmounts[i]));
        }

        // 3. 设置数据
        BarDataSet dataSet;
        if (barChart.getData() != null && barChart.getData().getDataSetCount() > 0) {
            dataSet = (BarDataSet) barChart.getData().getDataSetByIndex(0);
            dataSet.setValues(entries);
            barChart.getData().notifyDataChanged();
            barChart.notifyDataSetChanged();
        } else {
            dataSet = new BarDataSet(entries, "月度费用");
            dataSet.setColor(Color.rgb(33, 150, 243)); // Android Blue
            dataSet.setDrawValues(true);
            dataSet.setValueTextSize(10f);

            dataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return value > 0 ? String.format("%.0f", value) : "";
                }
            });

            BarData data = new BarData(dataSet);
            data.setBarWidth(0.5f);
            barChart.setData(data);
        }

        // 4. 强制刷新X轴范围，确保显示完整12个月
        barChart.getXAxis().setAxisMinimum(-0.5f);
        barChart.getXAxis().setAxisMaximum(11.5f);

        barChart.animateY(800);
        barChart.invalidate();
    }

    private void updatePieChart(List<PaymentRecord> records) {
        // 修复：不使用不存在的 getTitle()，改用 JSON 解析
        Map<String, Double> typeMap = new LinkedHashMap<>();
        // 定义常见 Key，保持顺序
        String[] keys = {"物业费", "维修金", "公摊水电", "电梯费", "其他"};
        for(String k : keys) typeMap.put(k, 0.0);

        for (PaymentRecord r : records) {
            boolean parsed = false;
            if (r.getFeeDetailsSnapshot() != null && !r.getFeeDetailsSnapshot().isEmpty()) {
                try {
                    // 假设数据库中存储的 JSON key 为: property, maintenance, utility, elevator 等
                    // 如果您的实际 JSON key 不同，请在此处修改字符串
                    JSONObject json = new JSONObject(r.getFeeDetailsSnapshot());

                    double property = json.optDouble("property", 0);
                    double maintenance = json.optDouble("maintenance", 0);
                    double utility = json.optDouble("utility", 0);
                    double elevator = json.optDouble("elevator", 0);

                    if (property > 0) typeMap.put("物业费", typeMap.get("物业费") + property);
                    if (maintenance > 0) typeMap.put("维修金", typeMap.get("维修金") + maintenance);
                    if (utility > 0) typeMap.put("公摊水电", typeMap.get("公摊水电") + utility);
                    if (elevator > 0) typeMap.put("电梯费", typeMap.get("电梯费") + elevator);

                    // 计算剩余部分归为其他 (可选)
                    double subTotal = property + maintenance + utility + elevator;
                    if (r.getAmount() > subTotal + 0.01) { // 加一点容差
                        typeMap.put("其他", typeMap.get("其他") + (r.getAmount() - subTotal));
                    }
                    parsed = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 如果没有详情，整个账单归为“其他”
            if (!parsed) {
                typeMap.put("其他", typeMap.get("其他") + r.getAmount());
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : typeMap.entrySet()) {
            if (entry.getValue() > 0.01) { // 只显示金额 > 0 的部分
                entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }
        }

        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(ColorTemplate.MATERIAL_COLORS); // 使用多彩颜色
        set.setValueTextSize(12f);
        set.setValueTextColor(Color.WHITE);
        set.setValueFormatter(new PercentFormatter(pieChart));

        PieData data = new PieData(set);
        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("费用构成");

        // 设置图例位置
        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setWordWrapEnabled(true);

        pieChart.invalidate();
    }
}
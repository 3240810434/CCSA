package com.gxuwz.ccsa.ui.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.DividerItemDecoration;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.PaymentAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.FilterData;
import com.gxuwz.ccsa.model.Notification;
import com.gxuwz.ccsa.model.PaymentItem;
import com.gxuwz.ccsa.model.PropertyFeeBill;
import com.gxuwz.ccsa.model.User;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaymentStatisticsActivity extends AppCompatActivity {

    // 修复日志标签长度
    private static final String TAG = "PaymentStatsActivity";
    private RecyclerView recyclerView;
    private PaymentAdapter adapter;
    private List<PaymentItem> paymentList = new ArrayList<>();
    private List<PaymentItem> filteredList = new ArrayList<>();
    private TextView tvTotalReceivable, tvPaidAmount, tvUnpaidAmount, tvPaymentRate;
    // 新增人数统计TextView
    private TextView tvTotalPeople, tvPaidPeople, tvUnpaidPeople;
    private String community;
    private DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
    private boolean isFilterApplied = false; // 标记是否应用了筛选
    // 新增：缴费提醒按钮
    private Button btnFeeReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_statistics);

        // 获取小区信息
        community = getIntent().getStringExtra("community");
        if (community == null || community.trim().isEmpty()) {
            Toast.makeText(this, "未获取到小区信息", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupListeners(); // 新增：初始化监听器
        setupRecyclerView();
        loadData();
    }

    private void initViews() {
        // 统计卡片 - 初始隐藏统计数据
        tvTotalReceivable = findViewById(R.id.tv_total_receivable);
        tvPaidAmount = findViewById(R.id.tv_paid_amount);
        tvUnpaidAmount = findViewById(R.id.tv_unpaid_amount);
        tvPaymentRate = findViewById(R.id.tv_payment_rate);
        // 初始化新增的人数统计TextView
        tvTotalPeople = findViewById(R.id.tv_total_people);
        tvPaidPeople = findViewById(R.id.tv_paid_people);
        tvUnpaidPeople = findViewById(R.id.tv_unpaid_people);

        // 初始不显示统计数据
        resetStatisticsDisplay();

        recyclerView = findViewById(R.id.recyclerView);
        // 新增：初始化缴费提醒按钮（默认隐藏）
        btnFeeReminder = findViewById(R.id.btn_fee_reminder);
        btnFeeReminder.setVisibility(View.GONE);
    }

    private void setupToolbar() {
        TextView tvBack = findViewById(R.id.tv_back);
        TextView tvFilter = findViewById(R.id.tv_filter);

        tvBack.setOnClickListener(v -> finish());
        tvFilter.setOnClickListener(v -> showFilterDialog());
    }

    // 新增：初始化按钮监听器
    private void setupListeners() {
        // 缴费提醒按钮点击事件
        btnFeeReminder.setOnClickListener(v -> sendPaymentReminders());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaymentAdapter(filteredList);
        recyclerView.setAdapter(adapter);

        // 添加分割线
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void loadData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(this);

                // 调用修复后的getByCommunity方法
                List<PropertyFeeBill> bills = db.propertyFeeBillDao().getByCommunity(community);

                // 清空列表
                paymentList.clear();

                // 遍历账单，转换为PaymentItem
                for (PropertyFeeBill bill : bills) {
                    // 调用修复后的getByPhone方法
                    User user = db.userDao().getByPhone(bill.getPhone());
                    String ownerName = user != null ? user.getName() : "未知业主";

                    // 确定缴费状态
                    String status = bill.getStatus() == 1 ? "已缴" : "未缴";

                    // 格式化缴费日期（调用修复后的getPaymentTime方法）
                    String payDate = "";
                    if (bill.getStatus() == 1 && bill.getPaymentTime() != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        payDate = sdf.format(bill.getPaymentTime());
                    }

                    // 提取年月作为缴费周期
                    String period = bill.getPeriodStart().substring(0, 7); // 假设格式为yyyy-MM-dd

                    // 添加到列表
                    paymentList.add(new PaymentItem(
                            bill.getBuilding(),
                            bill.getRoomNumber(),
                            ownerName,
                            bill.getTotalAmount(),
                            bill.getStatus() == 1 ? bill.getTotalAmount() : 0,
                            status,
                            payDate,
                            period,
                            bill.getPhone() // 保存用户标识用于统计人数
                    ));
                }

                // 初始不显示任何数据，直到应用筛选
                runOnUiThread(() -> {
                    filteredList.clear();
                    adapter.notifyDataSetChanged();
                });

            } catch (Exception e) {
                Log.e(TAG, "加载数据失败", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "加载数据失败：" + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void updateStatistics() {
        if (!isFilterApplied) {
            resetStatisticsDisplay();
            return;
        }

        double totalReceivable = 0;
        double totalPaid = 0;
        double totalUnpaid = 0;
        int totalCount = filteredList.size();
        int paidCount = 0;

        // 用于统计人数（去重）
        Set<String> totalPeopleSet = new HashSet<>();
        Set<String> paidPeopleSet = new HashSet<>();
        Set<String> unpaidPeopleSet = new HashSet<>();

        for (PaymentItem item : filteredList) {
            totalReceivable += item.getReceivable();
            totalPaid += item.getPaid();

            // 添加用户标识到总人数集合
            totalPeopleSet.add(item.getPhone());  // 需要确保PaymentItem有getPhone()方法

            if ("已缴".equals(item.getStatus())) {
                paidCount++;
                paidPeopleSet.add(item.getPhone());  // 需要确保PaymentItem有getPhone()方法
            } else {
                unpaidPeopleSet.add(item.getPhone());  // 需要确保PaymentItem有getPhone()方法
            }
        }

        totalUnpaid = totalReceivable - totalPaid;
        double paymentRate = totalCount > 0 ? (paidCount * 100.0 / totalCount) : 0;

        // 更新UI显示统计数据
        tvTotalReceivable.setText("¥" + decimalFormat.format(totalReceivable));
        tvPaidAmount.setText("¥" + decimalFormat.format(totalPaid));
        tvUnpaidAmount.setText("¥" + decimalFormat.format(totalUnpaid));
        tvPaymentRate.setText(String.format(Locale.getDefault(), "%.1f%%", paymentRate));

        // 更新人数统计
        tvTotalPeople.setText(totalPeopleSet.size() + "人");
        tvPaidPeople.setText(paidPeopleSet.size() + "人");
        tvUnpaidPeople.setText(unpaidPeopleSet.size() + "人");
    }

    // 新增：更新提醒按钮显示状态
    private void updateReminderButtonVisibility() {
        if (!isFilterApplied || filteredList.isEmpty()) {
            btnFeeReminder.setVisibility(View.GONE);
            return;
        }
        // 检查筛选结果中是否有未缴账单
        boolean hasUnpaid = false;
        for (PaymentItem item : filteredList) {
            if ("未缴".equals(item.getStatus())) {
                hasUnpaid = true;
                break;
            }
        }
        btnFeeReminder.setVisibility(hasUnpaid ? View.VISIBLE : View.GONE);
    }

    // 重置统计数据显示（不显示任何数据）
    private void resetStatisticsDisplay() {
        tvTotalReceivable.setText("--");
        tvPaidAmount.setText("--");
        tvUnpaidAmount.setText("--");
        tvPaymentRate.setText("--");
        // 重置人数统计显示
        tvTotalPeople.setText("--");
        tvPaidPeople.setText("--");
        tvUnpaidPeople.setText("--");
    }

    private void showFilterDialog() {
        FilterDialog dialog = new FilterDialog(this);
        dialog.setOnFilterListener(this::applyFilter);
        dialog.show();
    }

    private void applyFilter(FilterData filterData) {
        isFilterApplied = true; // 标记已应用筛选
        filteredList.clear();

        for (PaymentItem item : paymentList) {
            // 筛选年份
            if (filterData.getSelectedYears() != null && !filterData.getSelectedYears().isEmpty()) {
                String itemYear = item.getPeriod().substring(0, 4);
                if (!filterData.getSelectedYears().contains(itemYear)) {
                    continue;
                }
            }

            // 筛选月份
            if (filterData.getSelectedMonths() != null && !filterData.getSelectedMonths().isEmpty()) {
                String itemMonth = item.getPeriod().substring(5, 7); // 格式 yyyy-MM
                if (!filterData.getSelectedMonths().contains(itemMonth)) {
                    continue;
                }
            }

            // 筛选楼栋
            if (filterData.getSelectedBuildings() != null && !filterData.getSelectedBuildings().isEmpty()) {
                if (!filterData.getSelectedBuildings().contains(item.getBuilding())) {
                    continue;
                }
            }

            // 筛选缴费状态
            if (filterData.getPaymentStatus() != null && !"全部".equals(filterData.getPaymentStatus())) {
                if (!filterData.getPaymentStatus().equals(item.getStatus())) {
                    continue;
                }
            }

            // 符合条件，添加到筛选列表
            filteredList.add(item);
        }

        // 更新统计、提醒按钮状态和列表
        updateStatistics();
        updateReminderButtonVisibility(); // 新增：更新提醒按钮显示
        adapter.notifyDataSetChanged();

        // 显示筛选结果提示
        Toast.makeText(this, "筛选完成，共" + filteredList.size() + "条记录", Toast.LENGTH_SHORT).show();
    }

    // 新增：发送缴费提醒（修复lambda变量effectively final问题）
    private void sendPaymentReminders() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // 修复：用数组包装计数器，使其在lambda中可修改且符合effectively final规则
                final int[] successCount = {0};
                List<PaymentItem> unpaidItems = new ArrayList<>();

                // 筛选出当前筛选结果中的未缴账单
                for (PaymentItem item : filteredList) {
                    if ("未缴".equals(item.getStatus())) {
                        unpaidItems.add(item);
                    }
                }

                if (unpaidItems.isEmpty()) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "没有未缴账单需要提醒", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                // 为每个未缴账单创建通知并保存到数据库
                for (PaymentItem item : unpaidItems) {
                    // 构建通知内容
                    String title = "【" + community + "】物业费缴费提醒";
                    String content = String.format("尊敬的业主：\n您%s的物业费尚未缴纳，金额为¥%.2f元。\n请您通过[物业交费]完成支付，享受便捷生活。\n感谢您的支持与配合！\n【%s物业】",
                            item.getPeriod(), // 缴费周期（yyyy-MM）
                            item.getReceivable(), // 应收金额
                            community);

                    // 创建通知对象（参数需与Notification实体类构造方法匹配）
                    Notification notification = new Notification(
                            community,
                            item.getPhone(), // 业主手机号（用于接收提醒）
                            title,
                            content,
                            1, // 通知类型：1-缴费提醒
                            new Date(), // 发送时间
                            false // 初始状态：未读
                    );

                    // 保存通知到数据库
                    AppDatabase.getInstance(this).notificationDao().insert(notification);
                    successCount[0]++; // 修改数组元素值，而非变量本身
                }

                // 通知UI线程显示结果
                runOnUiThread(() ->
                        Toast.makeText(this, "成功发送" + successCount[0] + "条缴费提醒", Toast.LENGTH_SHORT).show()
                );
            } catch (Exception e) {
                Log.e(TAG, "发送缴费提醒失败", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "发送提醒失败：" + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } finally {
                executor.shutdown();
            }
        });
    }
}
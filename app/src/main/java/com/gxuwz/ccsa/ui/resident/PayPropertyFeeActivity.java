package com.gxuwz.ccsa.ui.resident;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.FeeBillAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.PaymentRecord;
import com.gxuwz.ccsa.model.PropertyFeeBill;
import com.gxuwz.ccsa.model.PropertyFeeStandard;
import com.gxuwz.ccsa.model.RoomArea;
import com.gxuwz.ccsa.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PayPropertyFeeActivity extends AppCompatActivity implements FeeBillAdapter.OnItemClickListener {

    private static final String TAG = "PayPropertyFeeActivity";
    private User currentUser;
    private RecyclerView recyclerView;
    private FeeBillAdapter adapter;
    private CheckBox cbSelectAll;
    private TextView tvTotalAmount;
    private Button btnPay;
    private double totalAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_property_fee);

        currentUser = (User) getIntent().getSerializableExtra("user");
        if (currentUser == null) {
            Log.e(TAG, "User对象传递失败");
            Toast.makeText(this, "用户信息错误，请重新登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d(TAG, "当前登录用户：手机号=" + currentUser.getPhone() + "，姓名=" + currentUser.getName());

        initViews();
        setupListeners();
        loadBills();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_bills);
        cbSelectAll = findViewById(R.id.cb_select_all);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        btnPay = findViewById(R.id.btn_pay);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FeeBillAdapter(this, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        cbSelectAll.setOnClickListener(v -> {
            boolean isChecked = cbSelectAll.isChecked();
            adapter.setAllChecked(isChecked);
            calculateTotal();
        });

        btnPay.setOnClickListener(v -> {
            List<PropertyFeeBill> checkedBills = adapter.getCheckedBills();
            if (checkedBills.isEmpty()) {
                Toast.makeText(this, "请选择要缴纳的费用", Toast.LENGTH_SHORT).show();
                return;
            }
            showPaymentMethodDialog(checkedBills);
        });
    }

    private void loadBills() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Log.d(TAG, "开始查询用户[" + currentUser.getPhone() + "]的物业费账单");

                // 查询该用户所有账单（按周期倒序）
                List<PropertyFeeBill> allBills = AppDatabase.getInstance(this)
                        .propertyFeeBillDao()
                        .getByPhoneOrderByPeriodDesc(currentUser.getPhone());

                Log.d(TAG, "查询结果：用户[" + currentUser.getPhone() + "]共有账单" + allBills.size() + "条");

                // 筛选未缴账单（状态0）
                List<PropertyFeeBill> unpaidBills = new ArrayList<>();
                for (PropertyFeeBill bill : allBills) {
                    Log.d(TAG, "账单详情：周期=" + bill.getPeriodStart() + "至" + bill.getPeriodEnd() +
                            "，金额=" + bill.getTotalAmount() + "元，状态=" + (bill.getStatus() == 0 ? "未缴" : "已缴"));

                    if (bill.getStatus() == 0) {
                        unpaidBills.add(bill);
                    }
                }

                // UI线程更新列表和提示
                runOnUiThread(() -> {
                    adapter.updateData(unpaidBills);
                    Log.d(TAG, "筛选后未缴账单数量：" + unpaidBills.size() + "条");

                    if (unpaidBills.isEmpty()) {
                        if (allBills.isEmpty()) {
                            Toast.makeText(this, "暂无任何物业费记录，请联系物业确认房屋绑定", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "暂无待缴物业费，所有账单已结清", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "找到" + unpaidBills.size() + "条未缴账单", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "加载账单失败", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "加载账单失败：" + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } finally {
                executor.shutdown();
            }
        });
    }

    @Override
    public void onItemCheck(int position, boolean isChecked) {
        calculateTotal();
        cbSelectAll.setChecked(adapter.isAllChecked());
    }

    @Override
    public void onDetailClick(int position) {
        PropertyFeeBill bill = adapter.getBill(position);
        if (bill != null) {
            showBillDetailDialog(bill);
        } else {
            Log.e(TAG, "未找到对应位置的账单，position=" + position);
            Toast.makeText(this, "获取账单详情失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateTotal() {
        totalAmount = 0;
        List<PropertyFeeBill> checkedBills = adapter.getCheckedBills();
        for (PropertyFeeBill bill : checkedBills) {
            totalAmount += bill.getTotalAmount();
        }
        tvTotalAmount.setText(String.format("总金额：%.2f元", totalAmount));
    }

    private void showBillDetailDialog(PropertyFeeBill bill) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_bill_detail, null);
        builder.setView(view);
        builder.setTitle("费用详情");
        builder.setPositiveButton("关闭", null);

        // 获取所有需要显示的控件
        TextView tvHouseInfo = view.findViewById(R.id.tv_house_info);
        TextView tvPropertyServiceFee = view.findViewById(R.id.tv_property_service_fee);
        TextView tvMaintenanceFund = view.findViewById(R.id.tv_maintenance_fund);
        TextView tvUtilityFee = view.findViewById(R.id.tv_utility_fee);
        TextView tvElevatorFee = view.findViewById(R.id.tv_elevator_fee);
        TextView tvElevatorFeeAbove = view.findViewById(R.id.tv_elevator_fee_above);
        TextView tvPressureFee = view.findViewById(R.id.tv_pressure_fee);
        TextView tvPressureFeeAbove = view.findViewById(R.id.tv_pressure_fee_above);
        TextView tvGarbageFee = view.findViewById(R.id.tv_garbage_fee);
        TextView tvPeriod = view.findViewById(R.id.tv_period);
        TextView tvTotal = view.findViewById(R.id.tv_total);

        // 设置已知的基本信息
        tvHouseInfo.setText(String.format("%s   %s    %s",
                bill.getCommunity(), bill.getBuilding(), bill.getRoomNumber()));
        tvPeriod.setText(String.format("缴费周期：%s 至 %s", bill.getPeriodStart(), bill.getPeriodEnd()));
        tvTotal.setText(String.format("总计：%.2f元", bill.getTotalAmount()));

        // 创建最终变量用于lambda表达式
        final TextView finalTvPropertyServiceFee = tvPropertyServiceFee;
        final TextView finalTvMaintenanceFund = tvMaintenanceFund;
        final TextView finalTvUtilityFee = tvUtilityFee;
        final TextView finalTvElevatorFee = tvElevatorFee;
        final TextView finalTvElevatorFeeAbove = tvElevatorFeeAbove;
        final TextView finalTvPressureFee = tvPressureFee;
        final TextView finalTvPressureFeeAbove = tvPressureFeeAbove;
        final TextView finalTvGarbageFee = tvGarbageFee;

        // 异步查询详细信息
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Log.d(TAG, "开始查询账单详情，standardId=" + bill.getStandardId());
                Log.d(TAG, "房屋信息：" + bill.getCommunity() + "-" + bill.getBuilding() + "-" + bill.getRoomNumber());

                // 查询账单对应的收费标准
                PropertyFeeStandard tempStandard = AppDatabase.getInstance(this)
                        .propertyFeeStandardDao()
                        .getById(bill.getStandardId());

                // 尝试根据小区名查最新的标准（如果ID查询失败）
                if (tempStandard == null && bill.getCommunity() != null) {
                    Log.w(TAG, "根据ID=" + bill.getStandardId() + "未查到收费标准，尝试根据小区名[" + bill.getCommunity() + "]查询最新标准");
                    tempStandard = AppDatabase.getInstance(this)
                            .propertyFeeStandardDao()
                            .getLatestByCommunity(bill.getCommunity());
                }
                final PropertyFeeStandard standard = tempStandard;

                // 查询房屋面积和楼层信息
                final RoomArea roomArea = AppDatabase.getInstance(this)
                        .roomAreaDao()
                        .getByCommunityBuildingAndRoom(
                                bill.getCommunity(), bill.getBuilding(), bill.getRoomNumber());

                Log.d(TAG, "收费标准查询结果：" + (standard != null ? "成功" : "失败"));
                Log.d(TAG, "房屋信息查询结果：" + (roomArea != null ? "成功" : "失败"));

                if (standard == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(PayPropertyFeeActivity.this, "获取收费标准失败，请联系物业", Toast.LENGTH_SHORT).show();
                        finalTvPropertyServiceFee.setText("物业服务费：获取数据失败");
                        finalTvMaintenanceFund.setText("日常维修资金：获取数据失败");
                        finalTvUtilityFee.setText("水电公摊费：获取数据失败");
                    });
                    return;
                }
                if (roomArea == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(PayPropertyFeeActivity.this, "获取房屋信息失败，请联系物业", Toast.LENGTH_SHORT).show();
                        finalTvPropertyServiceFee.setText("物业服务费：房屋信息缺失");
                        finalTvMaintenanceFund.setText("日常维修资金：房屋信息缺失");
                        finalTvUtilityFee.setText("水电公摊费：房屋信息缺失");
                    });
                    return;
                }

                // 获取房屋面积和楼层（声明为final）
                final double area = roomArea.getArea();
                final int floor = roomArea.getFloor();

                // 计算各项费用（声明为final）
                final double propertyServiceFee = standard.getPropertyServiceFeePerSquare() * area;
                final double maintenanceFund = standard.getDailyMaintenanceFund();
                final double utilityFee = standard.getUtilityShareFeePerSquare() * area;
                final double garbageFee = standard.getGarbageFee();

                // 计算电梯费
                final String elevatorFeeText;
                final double elevatorFeeValue;

                if (standard.getElevatorFee() > 0) {
                    if (standard.getElevatorFloorAbove() > 0 && floor >= standard.getElevatorFloorAbove()) {
                        elevatorFeeValue = standard.getElevatorFeeAbove();
                        elevatorFeeText = String.format("电梯费：%d楼及以上：%.2f元",
                                standard.getElevatorFloorAbove(), elevatorFeeValue);
                    } else if (standard.getElevatorFloorEnd() > 0 && floor <= standard.getElevatorFloorEnd()) {
                        elevatorFeeValue = standard.getElevatorFee();
                        elevatorFeeText = String.format("电梯费：%d楼及以下：%.2f元",
                                standard.getElevatorFloorEnd(), elevatorFeeValue);
                    } else {
                        elevatorFeeValue = standard.getElevatorFee();
                        elevatorFeeText = String.format("电梯费：%.2f元", elevatorFeeValue);
                    }
                } else {
                    elevatorFeeValue = 0;
                    elevatorFeeText = "电梯费：不收取";
                }

                // 计算加压费
                final String pressureFeeText;
                final double pressureFeeValue;

                if (standard.getPressureFee() > 0) {
                    if (standard.getPressureFloorAbove() > 0 && floor >= standard.getPressureFloorAbove()) {
                        pressureFeeValue = standard.getPressureFeeAbove();
                        pressureFeeText = String.format("加压费：%d楼及以上：%.2f元",
                                standard.getPressureFloorAbove(), pressureFeeValue);
                    } else if (standard.getPressureFloorStart() > 0 && standard.getPressureFloorEnd() > 0
                            && floor >= standard.getPressureFloorStart() && floor <= standard.getPressureFloorEnd()) {
                        pressureFeeValue = standard.getPressureFee();
                        pressureFeeText = String.format("加压费：%d-%d楼：%.2f元",
                                standard.getPressureFloorStart(), standard.getPressureFloorEnd(), pressureFeeValue);
                    } else {
                        pressureFeeValue = standard.getPressureFee();
                        pressureFeeText = String.format("加压费：%.2f元", pressureFeeValue);
                    }
                } else {
                    pressureFeeValue = 0;
                    pressureFeeText = "加压费：不收取";
                }

                // 计算总额验证
                double calculatedTotal = propertyServiceFee + maintenanceFund + utilityFee
                        + elevatorFeeValue + pressureFeeValue + garbageFee;
                Log.d(TAG, "计算总额：" + calculatedTotal + ", 账单总额：" + bill.getTotalAmount());

                // 更新UI
                runOnUiThread(() -> {
                    finalTvPropertyServiceFee.setText(String.format("物业服务费：%.2f元 (%.2f元/㎡ × %.2f㎡)",
                            propertyServiceFee, standard.getPropertyServiceFeePerSquare(), area));
                    finalTvMaintenanceFund.setText(String.format("日常维修资金：%.2f元", maintenanceFund));
                    finalTvUtilityFee.setText(String.format("水电公摊费：%.2f元 (%.2f元/㎡ × %.2f㎡)",
                            utilityFee, standard.getUtilityShareFeePerSquare(), area));

                    finalTvElevatorFee.setText(elevatorFeeText);
                    finalTvElevatorFeeAbove.setVisibility(View.GONE);

                    finalTvPressureFee.setText(pressureFeeText);
                    finalTvPressureFeeAbove.setVisibility(View.GONE);

                    finalTvGarbageFee.setText(String.format("生活垃圾处理费：%.2f元", garbageFee));

                    Toast.makeText(PayPropertyFeeActivity.this, "费用详情加载完成", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                Log.e(TAG, "加载费用详情时发生异常", e);
                runOnUiThread(() -> {
                    Toast.makeText(PayPropertyFeeActivity.this, "加载费用详情失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
                    finalTvPropertyServiceFee.setText("物业服务费：加载失败");
                    finalTvMaintenanceFund.setText("日常维修资金：加载失败");
                    finalTvUtilityFee.setText("水电公摊费：加载失败");
                    finalTvElevatorFee.setText("电梯费：加载失败");
                    finalTvPressureFee.setText("加压费：加载失败");
                    finalTvGarbageFee.setText("生活垃圾处理费：加载失败");
                });
            } finally {
                executor.shutdown();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showPaymentMethodDialog(List<PropertyFeeBill> checkedBills) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择支付方式");
        String[] methods = {"支付宝", "微信支付", "银行卡支付"};

        final List<PropertyFeeBill> finalCheckedBills = checkedBills;

        builder.setItems(methods, (dialog, which) -> {
            String paymentMethod = methods[which];
            processPayment(finalCheckedBills, paymentMethod);
        });
        builder.show();
    }

    private void processPayment(List<PropertyFeeBill> bills, String paymentMethod) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        final List<PropertyFeeBill> finalBills = bills;
        final String finalPaymentMethod = paymentMethod;

        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(this);
                String receiptNumber = generateReceiptNumber();

                // 保存支付记录+更新账单状态
                for (PropertyFeeBill bill : finalBills) {
                    PaymentRecord record = new PaymentRecord(
                            bill.getCommunity() != null ? bill.getCommunity() : "未知小区",
                            bill.getBuilding() != null ? bill.getBuilding() : "未知楼栋",
                            bill.getRoomNumber() != null ? bill.getRoomNumber() : "未知房号",
                            currentUser.getPhone(),
                            bill.getTotalAmount(),
                            bill.getPeriodStart() + "至" + bill.getPeriodEnd(),
                            1, // 状态：1-已缴
                            System.currentTimeMillis(), // 支付时间
                            receiptNumber // 收据编号
                    );

                    long recordId = db.paymentRecordDao().insert(record);
                    Log.d(TAG, "支付记录保存成功，ID: " + recordId + "，收据编号: " + receiptNumber);
                }

                // 批量更新账单状态为已缴
                List<Long> billIds = new ArrayList<>();
                for (PropertyFeeBill bill : finalBills) {
                    billIds.add(bill.getId());
                }
                db.propertyFeeBillDao().updateStatusByIds(1, billIds);
                Log.d(TAG, "已更新" + billIds.size() + "条账单状态为已缴");

                runOnUiThread(() -> {
                    Toast.makeText(this, "支付成功！收据编号：" + receiptNumber, Toast.LENGTH_SHORT).show();
                    loadBills(); // 刷新账单列表
                });
            } catch (Exception e) {
                Log.e(TAG, "支付处理失败", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "支付失败：" + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } finally {
                executor.shutdown();
            }
        });
    }

    // 生成收据编号
    private String generateReceiptNumber() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return "RCP" + sdf.format(new Date()) + new Random().nextInt(100000);
    }
}
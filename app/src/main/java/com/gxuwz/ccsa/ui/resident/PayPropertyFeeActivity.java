package com.gxuwz.ccsa.ui.resident;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.adapter.FeeBillAdapter;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.PaymentRecord;
import com.gxuwz.ccsa.model.PropertyFeeBill;
import com.gxuwz.ccsa.model.PropertyFeeStandard;
import com.gxuwz.ccsa.model.RoomArea;
import com.gxuwz.ccsa.model.User;

import org.json.JSONException;
import org.json.JSONObject;

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
                // 查询账单对应的收费标准
                PropertyFeeStandard tempStandard = AppDatabase.getInstance(this)
                        .propertyFeeStandardDao()
                        .getById(bill.getStandardId());

                if (tempStandard == null && bill.getCommunity() != null) {
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

                if (standard == null || roomArea == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(PayPropertyFeeActivity.this, "获取详细信息失败", Toast.LENGTH_SHORT).show();
                        finalTvPropertyServiceFee.setText("数据加载失败");
                    });
                    return;
                }

                // 获取房屋面积和楼层
                final double area = roomArea.getArea();
                final int floor = roomArea.getFloor();

                // 计算各项费用
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
                });

            } catch (Exception e) {
                Log.e(TAG, "加载费用详情时发生异常", e);
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

    /**
     * 处理支付逻辑：计算快照、生成记录、更新账单、跳转页面
     */
    private void processPayment(List<PropertyFeeBill> bills, String paymentMethod) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        final List<PropertyFeeBill> finalBills = bills;

        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(this);
                String receiptNumber = generateReceiptNumber();

                for (PropertyFeeBill bill : finalBills) {
                    // 1. 获取计算所需的标准和房屋信息
                    PropertyFeeStandard standard = db.propertyFeeStandardDao().getById(bill.getStandardId());
                    // 容错处理：如果ID查不到，尝试按小区名查最新
                    if (standard == null && bill.getCommunity() != null) {
                        standard = db.propertyFeeStandardDao().getLatestByCommunity(bill.getCommunity());
                    }

                    RoomArea roomArea = db.roomAreaDao().getByCommunityBuildingAndRoom(
                            bill.getCommunity(), bill.getBuilding(), bill.getRoomNumber());

                    // 2. 计算各项费用并生成JSON快照
                    String snapshotJson = "{}";
                    if (standard != null && roomArea != null) {
                        snapshotJson = calculateFeeDetailsJson(standard, roomArea);
                        Log.d(TAG, "生成费用快照成功: " + snapshotJson);
                    } else {
                        Log.w(TAG, "无法生成费用快照，缺失标准或房屋信息");
                    }

                    // 3. 创建记录（包含快照）
                    // 注意：此处假设 PaymentRecord 构造函数已更新以接受 snapshotJson 参数
                    PaymentRecord record = new PaymentRecord(
                            bill.getCommunity() != null ? bill.getCommunity() : "未知小区",
                            bill.getBuilding() != null ? bill.getBuilding() : "未知楼栋",
                            bill.getRoomNumber() != null ? bill.getRoomNumber() : "未知房号",
                            currentUser.getPhone(),
                            bill.getTotalAmount(),
                            bill.getPeriodStart() + "至" + bill.getPeriodEnd(),
                            1, // 状态：1-已缴
                            System.currentTimeMillis(), // 支付时间
                            receiptNumber, // 收据编号
                            snapshotJson   // 【新增】传入快照 JSON 字符串
                    );

                    db.paymentRecordDao().insert(record);
                }

                // 4. 批量更新账单状态为已缴
                List<Long> billIds = new ArrayList<>();
                for (PropertyFeeBill bill : finalBills) {
                    billIds.add(bill.getId());
                }
                db.propertyFeeBillDao().updateStatusByIds(1, billIds);
                Log.d(TAG, "已更新" + billIds.size() + "条账单状态为已缴");

                runOnUiThread(() -> {
                    Toast.makeText(this, "支付成功！收据编号：" + receiptNumber, Toast.LENGTH_SHORT).show();
                    loadBills(); // 刷新当前列表
                    // 【新增】支付完成后跳转到新的仪表盘页面
                    Intent intent = new Intent(this, PaymentDashboardActivity.class);
                    // 也可以传递一些数据到 Dashboard，如收据号等
                    intent.putExtra("receipt_number", receiptNumber);
                    startActivity(intent);
                    finish(); // 结束当前支付页面
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

    /**
     * 【新增】计算费用并返回JSON的方法
     * 根据收费标准和房屋面积，计算各项细分费用，用于存入数据库快照
     */
    private String calculateFeeDetailsJson(PropertyFeeStandard standard, RoomArea roomArea) {
        double area = roomArea.getArea();
        int floor = roomArea.getFloor();

        double propertyFee = standard.getPropertyServiceFeePerSquare() * area;
        double maintenanceFee = standard.getDailyMaintenanceFund();
        double utilityFee = standard.getUtilityShareFeePerSquare() * area;
        double garbageFee = standard.getGarbageFee();

        // 计算电梯费
        double elevatorFee = 0;
        if (standard.getElevatorFee() > 0) {
            if (standard.getElevatorFloorAbove() > 0 && floor >= standard.getElevatorFloorAbove()) {
                elevatorFee = standard.getElevatorFeeAbove();
            } else if (standard.getElevatorFloorEnd() > 0 && floor <= standard.getElevatorFloorEnd()) {
                elevatorFee = standard.getElevatorFee();
            } else {
                elevatorFee = standard.getElevatorFee();
            }
        }

        // 计算加压费
        double pressureFee = 0;
        if (standard.getPressureFee() > 0) {
            if (standard.getPressureFloorAbove() > 0 && floor >= standard.getPressureFloorAbove()) {
                pressureFee = standard.getPressureFeeAbove();
            } else if (standard.getPressureFloorStart() > 0 && floor >= standard.getPressureFloorStart() && floor <= standard.getPressureFloorEnd()) {
                pressureFee = standard.getPressureFee();
            } else {
                pressureFee = standard.getPressureFee();
            }
        }

        JSONObject json = new JSONObject();
        try {
            json.put("property", propertyFee);
            json.put("maintenance", maintenanceFee);
            json.put("utility", utilityFee);
            json.put("elevator", elevatorFee);
            json.put("pressure", pressureFee);
            json.put("garbage", garbageFee);
            // 还可以存入总计，方便核对
            double total = propertyFee + maintenanceFee + utilityFee + elevatorFee + pressureFee + garbageFee;
            json.put("calculated_total", total);
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}"; // 发生异常返回空JSON
        }
        return json.toString();
    }

    // 生成收据编号
    private String generateReceiptNumber() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return "RCP" + sdf.format(new Date()) + new Random().nextInt(100000);
    }
}
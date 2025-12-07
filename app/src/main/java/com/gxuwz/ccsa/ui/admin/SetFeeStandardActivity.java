package com.gxuwz.ccsa.ui.admin;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.PropertyFeeBill;
import com.gxuwz.ccsa.model.PropertyFeeStandard;
import com.gxuwz.ccsa.model.RoomArea;
import com.gxuwz.ccsa.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SetFeeStandardActivity extends AppCompatActivity {

    private static final String TAG = "SetFeeStandardActivity"; // 新增日志标签
    private String community;
    private EditText etPropertyServiceFee;
    private EditText etMaintenanceFund;
    private EditText etUtilityFee;
    private EditText etElevatorFloorEnd;
    private EditText etElevatorFee;
    private EditText etElevatorFloorAbove;
    private EditText etElevatorFeeAbove;
    private EditText etPressureFloorStart;
    private EditText etPressureFloorEnd;
    private EditText etPressureFee;
    private EditText etPressureFloorAbove;
    private EditText etPressureFeeAbove;
    private EditText etGarbageFee;
    private EditText etEffectiveDate;
    private Button btnSave;
    private Button btnReset;
    private Button btnRoomArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_fee_standard);

        // 初始化Toolbar并设置为ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 添加返回按钮（符合ActionBar常规交互）
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // 获取小区信息
        community = getIntent().getStringExtra("community");
        if (community == null || community.trim().isEmpty()) {
            Toast.makeText(this, "未获取到小区信息，无法设置物业费标准", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadExistingStandard();
    }

    private void initViews() {
        // ActionBar标题设置
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setTitle(community + " - 物业费标准设置");
        }

        // 初始化视图
        etPropertyServiceFee = findViewById(R.id.et_property_service_fee);
        etMaintenanceFund = findViewById(R.id.et_maintenance_fund);
        etUtilityFee = findViewById(R.id.et_utility_fee);
        etElevatorFloorEnd = findViewById(R.id.et_elevator_floor_end);
        etElevatorFee = findViewById(R.id.et_elevator_fee);
        etElevatorFloorAbove = findViewById(R.id.et_elevator_floor_above);
        etElevatorFeeAbove = findViewById(R.id.et_elevator_fee_above);
        etPressureFloorStart = findViewById(R.id.et_pressure_floor_start);
        etPressureFloorEnd = findViewById(R.id.et_pressure_floor_end);
        etPressureFee = findViewById(R.id.et_pressure_fee);
        etPressureFloorAbove = findViewById(R.id.et_pressure_floor_above);
        etPressureFeeAbove = findViewById(R.id.et_pressure_fee_above);
        etGarbageFee = findViewById(R.id.et_garbage_fee);
        etEffectiveDate = findViewById(R.id.et_effective_date);
        btnSave = findViewById(R.id.btn_save);
        btnReset = findViewById(R.id.btn_reset);

        // 创建标题栏右侧按钮
        btnRoomArea = new Button(this);
        btnRoomArea.setText("房屋面积信息");
        btnRoomArea.setTextSize(14);
        btnRoomArea.setTextColor(getResources().getColor(android.R.color.white));
        btnRoomArea.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        // 设置自定义视图到ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setCustomView(btnRoomArea);
        }
    }

    private void setupListeners() {
        // 房屋面积信息按钮点击事件
        btnRoomArea.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(SetFeeStandardActivity.this, RoomAreaManagementActivity.class);
            intent.putExtra("community", community);
            startActivity(intent);
        });

        // 保存按钮点击事件
        btnSave.setOnClickListener(v -> saveFeeStandard());
        // 重置按钮点击事件
        btnReset.setOnClickListener(v -> resetForm());

        // 电梯费楼层输入焦点变化监听
        etElevatorFloorEnd.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                updateElevatorFloorAbove();
            }
        });
    }

    // 处理返回按钮点击事件
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // 返回上一页
        return true;
    }

    // 更新电梯费上层楼层输入框状态
    private void updateElevatorFloorAbove() {
        String endFloorStr = etElevatorFloorEnd.getText().toString().trim();
        if (!endFloorStr.isEmpty()) {
            try {
                int endFloor = Integer.parseInt(endFloorStr);
                if (endFloor >= 10) {
                    etElevatorFloorAbove.setEnabled(false);
                    etElevatorFeeAbove.setEnabled(false);
                } else {
                    etElevatorFloorAbove.setEnabled(true);
                    etElevatorFeeAbove.setEnabled(true);
                    if (etElevatorFloorAbove.getText().toString().trim().isEmpty()) {
                        etElevatorFloorAbove.setText(String.valueOf(endFloor + 1));
                    }
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "电梯楼层请输入有效数字", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 加载已有物业费标准
    private void loadExistingStandard() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(this);
                PropertyFeeStandard standard = db.propertyFeeStandardDao().getLatestByCommunity(community);

                if (standard != null) {
                    runOnUiThread(() -> {
                        etPropertyServiceFee.setText(String.valueOf(standard.getPropertyServiceFeePerSquare()));
                        etMaintenanceFund.setText(String.valueOf(standard.getDailyMaintenanceFund()));
                        etUtilityFee.setText(String.valueOf(standard.getUtilityShareFeePerSquare()));
                        etElevatorFloorEnd.setText(String.valueOf(standard.getElevatorFloorEnd()));
                        etElevatorFee.setText(String.valueOf(standard.getElevatorFee()));
                        etElevatorFloorAbove.setText(String.valueOf(standard.getElevatorFloorAbove()));
                        etElevatorFeeAbove.setText(String.valueOf(standard.getElevatorFeeAbove()));
                        etPressureFloorStart.setText(String.valueOf(standard.getPressureFloorStart()));
                        etPressureFloorEnd.setText(String.valueOf(standard.getPressureFloorEnd()));
                        etPressureFee.setText(String.valueOf(standard.getPressureFee()));
                        etPressureFloorAbove.setText(String.valueOf(standard.getPressureFloorAbove()));
                        etPressureFeeAbove.setText(String.valueOf(standard.getPressureFeeAbove()));
                        etGarbageFee.setText(String.valueOf(standard.getGarbageFee()));
                        etEffectiveDate.setText(standard.getEffectiveDate());

                        updateElevatorFloorAbove();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "加载已有标准失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            } finally {
                executor.shutdown();
            }
        });
    }

    // 重置表单
    private void resetForm() {
        etPropertyServiceFee.setText("");
        etMaintenanceFund.setText("");
        etUtilityFee.setText("");
        etElevatorFloorEnd.setText("");
        etElevatorFee.setText("");
        etElevatorFloorAbove.setText("");
        etElevatorFeeAbove.setText("");
        etPressureFloorStart.setText("");
        etPressureFloorEnd.setText("");
        etPressureFee.setText("");
        etPressureFloorAbove.setText("");
        etPressureFeeAbove.setText("");
        etGarbageFee.setText("");
        etEffectiveDate.setText("");

        etElevatorFloorAbove.setEnabled(true);
        etElevatorFeeAbove.setEnabled(true);
    }

    // 保存物业费标准（带二次确认）
    private void saveFeeStandard() {
        String dateStr = etEffectiveDate.getText().toString().trim();
        if (dateStr.isEmpty()) {
            Toast.makeText(this, "请输入日期", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证日期格式是否为yyyy-MM（添加Locale避免格式异常）
        if (!isValidMonthFormat(dateStr)) {
            Toast.makeText(this, "日期格式错误，请使用yyyy-MM格式", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取所有费用项的值
        String propertyServiceFee = etPropertyServiceFee.getText().toString().trim();
        String maintenanceFund = etMaintenanceFund.getText().toString().trim();
        String utilityFee = etUtilityFee.getText().toString().trim();
        String elevatorFloorEnd = etElevatorFloorEnd.getText().toString().trim();
        String elevatorFee = etElevatorFee.getText().toString().trim();
        String elevatorFloorAbove = etElevatorFloorAbove.getText().toString().trim();
        String elevatorFeeAbove = etElevatorFeeAbove.getText().toString().trim();
        String pressureFloorStart = etPressureFloorStart.getText().toString().trim();
        String pressureFloorEnd = etPressureFloorEnd.getText().toString().trim();
        String pressureFee = etPressureFee.getText().toString().trim();
        String pressureFloorAbove = etPressureFloorAbove.getText().toString().trim();
        String pressureFeeAbove = etPressureFeeAbove.getText().toString().trim();
        String garbageFee = etGarbageFee.getText().toString().trim();

        // 构建详细信息字符串
        StringBuilder detailBuilder = new StringBuilder();
        detailBuilder.append("物业服务费每平方米每月费用：").append(propertyServiceFee).append("\n")
                .append("日常维修资金：").append(maintenanceFund).append("\n")
                .append("水电公摊费：").append(utilityFee).append("\n")
                .append("电梯费：2-").append(elevatorFloorEnd).append("楼：").append(elevatorFee).append("\n")
                .append("         ").append(elevatorFloorAbove).append("楼及以上：").append(elevatorFeeAbove).append("\n")
                .append("加压费：").append(pressureFloorStart).append("-").append(pressureFloorEnd).append("楼：").append(pressureFee).append("\n")
                .append("         ").append(pressureFloorAbove).append("楼及以上：").append(pressureFeeAbove).append("\n")
                .append("生活垃圾处理费：").append(garbageFee).append("\n")
                .append("生效日期：").append(dateStr).append("\n")
                .append("缴费周期：月");

        // 显示确认对话框
        new AlertDialog.Builder(this)
                .setTitle("确认保存")
                .setMessage(detailBuilder.toString())
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (dialog, which) -> {
                    // 执行保存逻辑
                    try {
                        saveToDatabase(dateStr);
                    } catch (ParseException e) {
                        Toast.makeText(this, "日期解析错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    // 日期格式验证方法
    private boolean isValidMonthFormat(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    // 保存到数据库的方法（原有逻辑保持不变）
    private void saveToDatabase(String dateStr) throws ParseException {
        // 此处保持原有保存逻辑不变
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // 解析日期并设置缴费周期
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                Date effectiveDate = sdf.parse(dateStr);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(effectiveDate);

                // 创建费用标准对象
                PropertyFeeStandard standard = new PropertyFeeStandard(
                        community,
                        parseDouble(etPropertyServiceFee.getText().toString()),
                        parseDouble(etMaintenanceFund.getText().toString()),
                        parseDouble(etUtilityFee.getText().toString()),
                        2, // 电梯起始楼层固定为2楼
                        parseInt(etElevatorFloorEnd.getText().toString()),
                        parseDouble(etElevatorFee.getText().toString()),
                        parseInt(etElevatorFloorAbove.getText().toString()),
                        parseDouble(etElevatorFeeAbove.getText().toString()),
                        parseInt(etPressureFloorStart.getText().toString()),
                        parseInt(etPressureFloorEnd.getText().toString()),
                        parseDouble(etPressureFee.getText().toString()),
                        parseInt(etPressureFloorAbove.getText().toString()),
                        parseDouble(etPressureFeeAbove.getText().toString()),
                        parseDouble(etGarbageFee.getText().toString()),
                        dateStr,
                        "月", // 缴费周期
                        System.currentTimeMillis()
                );

                // 保存到数据库
                AppDatabase db = AppDatabase.getInstance(this);
                long standardId = db.propertyFeeStandardDao().insert(standard);
                Log.d(TAG, "物业费标准保存成功，ID: " + standardId);

                // 计算该月的开始和结束日期
                String periodStart = dateStr + "-01";
                String periodEnd = getLastDayOfMonth(dateStr);
                if (periodEnd != null) {
                    // 为小区所有住户生成账单
                    generateBillsForAllResidents(db, standardId, standard, periodStart, periodEnd);
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "物业费标准保存成功", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                Log.e(TAG, "保存物业费标准失败", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } finally {
                executor.shutdown();
            }
        });
    }

    // 辅助方法：字符串转double
    private double parseDouble(String value) {
        if (value.isEmpty()) return 0;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // 辅助方法：字符串转int
    private int parseInt(String value) {
        if (value.isEmpty()) return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // 获取月份的最后一天（优化异常处理）
    private String getLastDayOfMonth(String yearMonth) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            Date date = sdf.parse(yearMonth);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
        } catch (ParseException e) {
            Log.e(TAG, "计算月末日期失败", e);
            return null;
        }
    }

    /**
     * 核心新增：为小区所有住户生成缴费账单
     * @param db 数据库实例
     * @param standardId 物业费标准ID
     * @param standard 物业费标准对象
     * @param periodStart 账单周期开始日期（yyyy-MM-dd）
     * @param periodEnd 账单周期结束日期（yyyy-MM-dd）
     */
    private void generateBillsForAllResidents(AppDatabase db, long standardId,
                                              PropertyFeeStandard standard,
                                              String periodStart, String periodEnd) {
        try {
            // 1. 获取该小区所有房屋信息
            List<RoomArea> roomAreas = db.roomAreaDao().getByCommunity(community);
            Log.d(TAG, "查询到小区[" + community + "]的房屋数量：" + roomAreas.size());

            if (roomAreas.isEmpty()) {
                runOnUiThread(() ->
                        Toast.makeText(this, "未找到该小区的房屋信息，无法生成账单", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            // 2. 为每个房屋生成账单
            int successCount = 0;
            for (RoomArea room : roomAreas) {
                // 获取房屋对应的住户（通过小区+楼栋+房号关联）
                User resident = db.userDao().getByRoom(community, room.getBuilding(), room.getRoomNumber());
                if (resident == null || resident.getPhone() == null) {
                    Log.w(TAG, "房屋[" + room.getBuilding() + "-" + room.getRoomNumber() + "]未绑定住户，跳过账单生成");
                    continue;
                }

                // 3. 计算该房屋的总费用
                double totalAmount = calculateTotalFee(standard, room, room.getRoomNumber());
                Log.d(TAG, "房屋[" + room.getBuilding() + "-" + room.getRoomNumber() + "]费用计算结果：" + totalAmount);

                // 4. 创建账单对象（状态0：未缴）
                PropertyFeeBill bill = new PropertyFeeBill(
                        community,
                        room.getBuilding(),
                        room.getRoomNumber(),
                        resident.getPhone(),
                        totalAmount,
                        periodStart,
                        periodEnd,
                        0, // 0-未缴，1-已缴
                        System.currentTimeMillis(),
                        standardId // 关联物业费标准
                );

                // 5. 保存账单到数据库
                db.propertyFeeBillDao().insert(bill);
                successCount++;
            }

            Log.d(TAG, "账单生成完成，成功生成[" + successCount + "]条账单");
        } catch (Exception e) {
            Log.e(TAG, "生成账单失败", e);
            throw new RuntimeException("生成账单失败: " + e.getMessage());
        }
    }

    /**
     * 新增：根据房屋信息和收费标准计算总费用
     * @param standard 收费标准
     * @param room 房屋信息（含面积）
     * @param roomNumber 房号（用于提取楼层）
     * @return 总费用（保留两位小数）
     */
    private double calculateTotalFee(PropertyFeeStandard standard, RoomArea room, String roomNumber) {
        double total = 0;
        double area = room.getArea();
        int floor = extractFloorFromRoomNumber(roomNumber); // 提取楼层

        // 1. 物业服务费（每平米费用 × 房屋面积）
        total += standard.getPropertyServiceFeePerSquare() * area;
        // 2. 日常维修资金（固定费用）
        total += standard.getDailyMaintenanceFund();
        // 3. 水电公摊费（每平米费用 × 房屋面积）
        total += standard.getUtilityShareFeePerSquare() * area;
        // 4. 垃圾处理费（固定费用）
        total += standard.getGarbageFee();

        // 5. 电梯费（按楼层区间计算）
        if (floor >= standard.getElevatorFloorStart()) {
            if (floor <= standard.getElevatorFloorEnd()) {
                total += standard.getElevatorFee();
            } else if (floor >= standard.getElevatorFloorAbove() && standard.getElevatorFloorAbove() > 0) {
                total += standard.getElevatorFeeAbove();
            }
        }

        // 6. 加压费（按楼层区间计算）
        if (floor >= standard.getPressureFloorStart() && standard.getPressureFloorStart() > 0) {
            if (floor <= standard.getPressureFloorEnd()) {
                total += standard.getPressureFee();
            } else if (floor >= standard.getPressureFloorAbove() && standard.getPressureFloorAbove() > 0) {
                total += standard.getPressureFeeAbove();
            }
        }

        // 保留两位小数（避免浮点精度问题）
        return Math.round(total * 100.0) / 100.0;
    }

    /**
     * 新增：从房号中提取楼层（如"1001"→10，"302"→3）
     * @param roomNumber 房号
     * @return 楼层（解析失败返回1楼）
     */
    private int extractFloorFromRoomNumber(String roomNumber) {
        try {
            if (roomNumber.length() >= 3) {
                // 截取最后两位之前的字符作为楼层（兼容2位房号+多位楼层）
                String floorStr = roomNumber.substring(0, roomNumber.length() - 2);
                return Integer.parseInt(floorStr);
            } else if (roomNumber.length() == 2) {
                // 两位房号默认1楼（如"01"→1）
                return 1;
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "解析楼层失败，房号：" + roomNumber, e);
        }
        return 1; // 默认1楼（无电梯/加压费）
    }
}
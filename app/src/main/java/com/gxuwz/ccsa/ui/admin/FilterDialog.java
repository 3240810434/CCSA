package com.gxuwz.ccsa.ui.admin;

import android.app.Dialog;
import androidx.annotation.NonNull;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.GridLayout;
import androidx.core.content.ContextCompat;

import com.gxuwz.ccsa.R;
import com.gxuwz.ccsa.model.FilterData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class FilterDialog extends Dialog {

    private Context context;
    private OnFilterListener listener;

    private GridLayout yearContainer;
    private GridLayout monthContainer;
    private CheckBox cbSelectAllBuildings;
    private RadioGroup rgStatus;
    private GridLayout buildingContainer;

    // 年份数据（近5年）
    private List<String> yearList;
    // 月份数据
    private List<String> monthList = Arrays.asList("01", "02", "03", "04", "05", "06",
            "07", "08", "09", "10", "11", "12");
    // 楼栋数据（假设最多10栋）
    private List<String> buildingList = Arrays.asList("1栋", "2栋", "3栋", "4栋", "5栋",
            "6栋", "7栋", "8栋", "9栋", "10栋");

    private List<CheckBox> yearCheckboxes = new ArrayList<>();
    private List<CheckBox> monthCheckboxes = new ArrayList<>();
    private List<CheckBox> buildingCheckboxes = new ArrayList<>();

    public interface OnFilterListener {
        void onFilterApplied(FilterData filterData);
    }

    public FilterDialog(@NonNull Context context) {
        super(context);
        this.context = context;

        // 初始化年份列表（近5年：2023-2027）
        yearList = new ArrayList<>();
        yearList.add("2023");
        yearList.add("2024");
        yearList.add("2025");
        yearList.add("2026");
        yearList.add("2027");
    }

    public void setOnFilterListener(OnFilterListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_filter);

        setupDialog();
        initViews();
        setupCheckboxes();
        setupListeners();

        // 默认选中全部楼栋
        cbSelectAllBuildings.setChecked(true);
        for (CheckBox checkBox : buildingCheckboxes) {
            checkBox.setChecked(true);
        }
    }

    private void setupDialog() {
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
            // 添加动画效果
            window.setWindowAnimations(R.style.BottomDialogAnimation);
        }
    }

    private void initViews() {
        yearContainer = findViewById(R.id.year_container);
        monthContainer = findViewById(R.id.month_container);
        cbSelectAllBuildings = findViewById(R.id.cb_select_all_buildings);
        rgStatus = findViewById(R.id.rg_status);
        buildingContainer = findViewById(R.id.building_container);

        // 默认选中"全部"状态
        ((RadioButton) findViewById(R.id.rb_all)).setChecked(true);
    }

    private void setupCheckboxes() {
        // 设置年份复选框
        for (String year : yearList) {
            CheckBox checkBox = createCheckbox(year);
            yearContainer.addView(checkBox);
            yearCheckboxes.add(checkBox);
        }

        // 设置月份复选框
        for (String month : monthList) {
            CheckBox checkBox = createCheckbox(month + "月");
            monthContainer.addView(checkBox);
            monthCheckboxes.add(checkBox);
        }

        // 设置楼栋复选框
        for (String building : buildingList) {
            CheckBox checkBox = createCheckbox(building);
            buildingContainer.addView(checkBox);
            buildingCheckboxes.add(checkBox);
        }
    }

    // 修改createCheckbox方法中的布局参数
    private CheckBox createCheckbox(String text) {
        CheckBox checkBox = new CheckBox(context);
        checkBox.setText(text);
        checkBox.setTextSize(14);
        checkBox.setPadding(10, 10, 10, 10);

        // 将LinearLayout.LayoutParams改为GridLayout.LayoutParams
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.setMargins(10, 5, 10, 5);
        // 设置宽高为包裹内容
        params.width = GridLayout.LayoutParams.WRAP_CONTENT;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        checkBox.setLayoutParams(params);

        // 自定义复选框样式
        checkBox.setButtonDrawable(R.drawable.checkbox_selector);

        return checkBox;
    }

    private void setupListeners() {
        // 关闭按钮
        findViewById(R.id.tv_close).setOnClickListener(v -> dismiss());

        // 重置按钮
        findViewById(R.id.btn_reset).setOnClickListener(v -> resetFilters());

        // 确认按钮
        findViewById(R.id.btn_confirm).setOnClickListener(v -> applyFilters());

        // 楼栋全选
        cbSelectAllBuildings.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (CheckBox checkBox : buildingCheckboxes) {
                checkBox.setChecked(isChecked);
            }
        });
    }

    private void resetFilters() {
        // 重置年份选择
        for (CheckBox checkBox : yearCheckboxes) {
            checkBox.setChecked(false);
        }

        // 重置月份选择
        for (CheckBox checkBox : monthCheckboxes) {
            checkBox.setChecked(false);
        }

        // 重置楼栋选择
        cbSelectAllBuildings.setChecked(true);
        for (CheckBox checkBox : buildingCheckboxes) {
            checkBox.setChecked(true);
        }

        // 重置状态选择
        rgStatus.check(R.id.rb_all);
    }

    private void applyFilters() {
        FilterData filterData = new FilterData();

        // 获取选中的年份
        List<String> selectedYears = new ArrayList<>();
        for (CheckBox checkBox : yearCheckboxes) {
            if (checkBox.isChecked()) {
                selectedYears.add(checkBox.getText().toString());
            }
        }
        filterData.setSelectedYears(selectedYears);

        // 获取选中的月份
        List<String> selectedMonths = new ArrayList<>();
        for (int i = 0; i < monthCheckboxes.size(); i++) {
            if (monthCheckboxes.get(i).isChecked()) {
                selectedMonths.add(monthList.get(i)); // 存储数字格式如"01"而非"1月"
            }
        }
        filterData.setSelectedMonths(selectedMonths);

        // 获取选中的楼栋
        List<String> selectedBuildings = new ArrayList<>();
        for (CheckBox checkBox : buildingCheckboxes) {
            if (checkBox.isChecked()) {
                selectedBuildings.add(checkBox.getText().toString());
            }
        }
        filterData.setSelectedBuildings(selectedBuildings);

        // 获取缴费状态
        int checkedId = rgStatus.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_paid) {
            filterData.setPaymentStatus("已缴");
        } else if (checkedId == R.id.rb_unpaid) {
            filterData.setPaymentStatus("未缴");
        } else {
            filterData.setPaymentStatus("全部");
        }

        if (listener != null) {
            listener.onFilterApplied(filterData);
        }

        dismiss();
    }
}

package com.gxuwz.ccsa.ui.resident;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.gxuwz.ccsa.R;

public class EmergencyAlarmDialog extends Dialog {

    private View viewRipple1, viewRipple2;
    private View btnSosMain;
    private TextView tvStatusTitle, tvCountdown, tvSosHint;
    private Button btnCancel;

    // 双击逻辑变量
    private long lastClickTime = 0;
    private static final long DOUBLE_CLICK_INTERVAL = 500; // 500ms内算双击

    // 状态
    private boolean isAlarmTriggered = false;
    private OnAlarmConfirmedListener listener;
    private Vibrator vibrator;

    public interface OnAlarmConfirmedListener {
        void onAlarmConfirmed();
    }

    public EmergencyAlarmDialog(@NonNull Context context, OnAlarmConfirmedListener listener) {
        // 修改点1：使用系统自带的全屏透明主题，解决 Cannot resolve symbol 错误
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        this.listener = listener;
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_emergency_alarm);

        // 再次确保全屏属性
        if (getWindow() != null) {
            getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        initViews();
        startRippleAnimation();
    }

    private void initViews() {
        viewRipple1 = findViewById(R.id.view_ripple_1);
        viewRipple2 = findViewById(R.id.view_ripple_2);
        btnSosMain = findViewById(R.id.btn_sos_main);
        tvStatusTitle = findViewById(R.id.tv_status_title);
        tvCountdown = findViewById(R.id.tv_countdown);
        tvSosHint = findViewById(R.id.tv_sos_hint);
        btnCancel = findViewById(R.id.btn_cancel_alarm);

        btnSosMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSosClick();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void handleSosClick() {
        if (isAlarmTriggered) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < DOUBLE_CLICK_INTERVAL) {
            // 双击成功，触发报警流程
            triggerAlarmProcess();
        } else {
            lastClickTime = currentTime;
            // 单次点击震动反馈
            if (vibrator != null) vibrator.vibrate(50);
        }
    }

    private void triggerAlarmProcess() {
        isAlarmTriggered = true;
        tvSosHint.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);

        // 修改点2：修复 Cannot resolve symbol 'text'，Java中使用 setText
        tvStatusTitle.setText("正在联通物业...");

        // 模拟倒计时连接
        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // 修改点2：Java中使用 setText
                tvCountdown.setText("正在呼叫值班室 " + (millisUntilFinished / 1000 + 1));
                // 每次倒计时震动一下
                if (vibrator != null) vibrator.vibrate(200);
            }

            @Override
            public void onFinish() {
                // 修改点2：Java中使用 setText
                tvCountdown.setText("报警成功！已通知保安队");
                tvStatusTitle.setText("连接成功");

                // 成功长震动
                if (vibrator != null) vibrator.vibrate(1000);

                // 延迟关闭，并回调主界面处理后续逻辑
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismiss();
                        if (listener != null) listener.onAlarmConfirmed();
                    }
                }, 1500);
            }
        }.start();
    }

    private void startRippleAnimation() {
        // 简单的呼吸/雷达动画
        ScaleAnimation anim1 = new ScaleAnimation(1.0f, 1.5f, 1.0f, 1.5f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim1.setDuration(1500);
        anim1.setRepeatCount(Animation.INFINITE);
        anim1.setRepeatMode(Animation.REVERSE);
        viewRipple1.startAnimation(anim1);

        ScaleAnimation anim2 = new ScaleAnimation(1.0f, 1.4f, 1.0f, 1.4f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim2.setDuration(1500);
        anim2.setStartOffset(500); // 错开时间
        anim2.setRepeatCount(Animation.INFINITE);
        anim2.setRepeatMode(Animation.REVERSE);
        viewRipple2.startAnimation(anim2);
    }
}
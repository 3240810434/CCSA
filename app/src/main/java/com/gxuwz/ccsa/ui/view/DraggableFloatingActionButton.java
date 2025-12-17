package com.gxuwz.ccsa.ui.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DraggableFloatingActionButton extends FloatingActionButton implements View.OnTouchListener {

    private int parentHeight;
    private int parentWidth;
    private float lastX, lastY;
    private float dX, dY;
    private boolean isDrag = false;
    private long lastTouchTime = 0;

    // 透明度处理
    private Handler alphaHandler = new Handler(Looper.getMainLooper());
    private Runnable alphaRunnable = () -> animate().alpha(0.5f).setDuration(500).start();

    public DraggableFloatingActionButton(Context context) {
        super(context);
        init();
    }

    public DraggableFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DraggableFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnTouchListener(this);
        // 初始延迟3秒后变透明
        resetAlphaTimer();
    }

    private void resetAlphaTimer() {
        animate().alpha(1.0f).setDuration(200).start();
        alphaHandler.removeCallbacks(alphaRunnable);
        alphaHandler.postDelayed(alphaRunnable, 3000); // 3秒无操作变淡
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        float rawX = event.getRawX();
        float rawY = event.getRawY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isDrag = false;
                lastX = rawX;
                lastY = rawY;
                dX = view.getX() - rawX;
                dY = view.getY() - rawY;
                lastTouchTime = System.currentTimeMillis();

                // 触摸时恢复不透明并移除计时器
                animate().alpha(1.0f).setDuration(100).start();
                alphaHandler.removeCallbacks(alphaRunnable);

                ViewGroup parent = (ViewGroup) view.getParent();
                parentHeight = parent.getHeight();
                parentWidth = parent.getWidth();
                break;

            case MotionEvent.ACTION_MOVE:
                float distanceX = rawX - lastX;
                float distanceY = rawY - lastY;
                // 移动距离很小则视为点击，不视为拖拽
                if (Math.abs(distanceX) > 10 || Math.abs(distanceY) > 10) {
                    isDrag = true;
                }

                // 更新位置
                float newX = rawX + dX;
                float newY = rawY + dY;

                // 边界检查，防止拖出屏幕
                newX = Math.max(0, Math.min(newX, parentWidth - view.getWidth()));
                newY = Math.max(0, Math.min(newY, parentHeight - view.getHeight()));

                view.setX(newX);
                view.setY(newY);
                break;

            case MotionEvent.ACTION_UP:
                if (!isDrag) {
                    // 如果不是拖拽，则是点击，触发 performClick
                    if (System.currentTimeMillis() - lastTouchTime < 300) {
                        performClick();
                    }
                } else {
                    // 拖拽结束，吸附边缘逻辑
                    float currentX = view.getX();
                    float destX;
                    // 判断靠左还是靠右
                    if (currentX + view.getWidth() / 2f < parentWidth / 2f) {
                        destX = 0; // 吸附左边
                    } else {
                        destX = parentWidth - view.getWidth(); // 吸附右边
                    }

                    // 动画吸附
                    ObjectAnimator animator = ObjectAnimator.ofFloat(view, "x", currentX, destX);
                    animator.setInterpolator(new DecelerateInterpolator());
                    animator.setDuration(300);
                    animator.start();
                }
                // 手指抬起，重新开始倒计时变透明
                resetAlphaTimer();
                break;
        }
        return true; // 消费触摸事件
    }
}

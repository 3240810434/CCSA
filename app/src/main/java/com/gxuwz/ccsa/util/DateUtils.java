package com.gxuwz.ccsa.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public static String formatTime(long timeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }

    public static String formatDate(long timeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }
    public static String getRelativeTime(long time) {
        long now = System.currentTimeMillis();
        long diff = now - time;
        if (diff < 60 * 1000) return "刚刚";
        if (diff < 60 * 60 * 1000) return (diff / (60 * 1000)) + "分钟前";
        if (diff < 24 * 60 * 60 * 1000) return (diff / (60 * 60 * 1000)) + "小时前";
        return "很久以前"; // 实际可用SimpleDateFormat格式化日期
    }
}
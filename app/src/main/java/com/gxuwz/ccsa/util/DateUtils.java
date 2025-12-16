package com.gxuwz.ccsa.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    /**
     * 格式化时间，精确到分
     * 格式：yyyy-MM-dd HH:mm
     */
    public static String formatTime(long timeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }

    /**
     * 格式化日期，只显示年月日
     * 格式：yyyy-MM-dd
     */
    public static String formatDate(long timeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }

    /**
     * 将Date对象转换为字符串 (修复报错的核心方法)
     */
    public static String dateToString(Date date) {
        if (date == null) return "";
        return formatTime(date.getTime());
    }

    /**
     * 获取相对时间（如：刚刚、xx分钟前）
     */
    public static String getRelativeTime(long time) {
        long now = System.currentTimeMillis();
        long diff = now - time;
        if (diff < 60 * 1000) return "刚刚";
        if (diff < 60 * 60 * 1000) return (diff / (60 * 1000)) + "分钟前";
        if (diff < 24 * 60 * 60 * 1000) return (diff / (60 * 60 * 1000)) + "小时前";
        return formatDate(time);
    }

    /**
     * 获取当前时间完整字符串
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    public static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}
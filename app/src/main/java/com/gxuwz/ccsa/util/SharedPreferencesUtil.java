package com.gxuwz.ccsa.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {
    private static final String PREF_NAME = "ccsa_prefs";
    private static SharedPreferencesUtil instance;
    private final SharedPreferences sharedPreferences;

    private SharedPreferencesUtil(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPreferencesUtil getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesUtil(context);
        }
        return instance;
    }

    // 获取当前登录商家的ID (假设存储的是String类型的ID)
    public String getMerchantId() {
        // 这里的key "merchant_id" 需要和你登录时保存的key一致
        // 如果你登录时还没保存，可以暂时返回默认测试ID "1"
        return sharedPreferences.getString("merchant_id", "1");
    }

    // 保存商家ID (供登录页面使用)
    public void saveMerchantId(String id) {
        sharedPreferences.edit().putString("merchant_id", id).apply();
    }
}
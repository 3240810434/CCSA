package com.gxuwz.ccsa.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import com.gxuwz.ccsa.model.User;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SharedPreferencesUtil {
    private static final String PREF_NAME = "ccsa_prefs";
    private static final String KEY_USER = "user_data";
    private static final String KEY_MERCHANT_ID = "merchant_id";

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

    // --- 商家相关 ---
    public String getMerchantId() {
        return sharedPreferences.getString(KEY_MERCHANT_ID, "1");
    }

    public void saveMerchantId(String id) {
        sharedPreferences.edit().putString(KEY_MERCHANT_ID, id).apply();
    }

    // --- 用户相关 (静态方法，适配 MyDynamicsActivity 等调用) ---

    /**
     * 保存用户信息
     */
    public static void saveUser(Context context, User user) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (user == null) {
            sp.edit().remove(KEY_USER).apply();
            return;
        }

        // 将 User 对象序列化为 String 保存
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(user);
            String userBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
            sp.edit().putString(KEY_USER, userBase64).apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取用户信息
     */
    public static User getUser(Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String userBase64 = sp.getString(KEY_USER, "");

        if (TextUtils.isEmpty(userBase64)) {
            return null;
        }

        try {
            byte[] mobileBytes = Base64.decode(userBase64.getBytes(), Base64.DEFAULT);
            ByteArrayInputStream bais = new ByteArrayInputStream(mobileBytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (User) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 清除用户信息 (退出登录时使用)
     */
    public static void clearUser(Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().remove(KEY_USER).apply();
    }
}
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
    // 统一使用同一个 SharedPreferences 文件名，避免数据分散
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

    // ================== 1. 商家相关 (原有实例方法) ==================

    public String getMerchantId() {
        return sharedPreferences.getString(KEY_MERCHANT_ID, "1");
    }

    public void saveMerchantId(String id) {
        sharedPreferences.edit().putString(KEY_MERCHANT_ID, id).apply();
    }

    // ================== 2. 用户对象相关 (原有静态方法) ==================

    /**
     * 保存用户信息 (对象序列化)
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
     * 获取用户信息 (对象反序列化)
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

    // ================== 3. 通用数据存取 (新增通用方法) ==================

    /**
     * 通用数据保存方法
     * 支持 Integer, Boolean, String, Float, Long
     */
    public static void saveData(Context context, String key, Object data) {
        // 统一使用 PREF_NAME
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (data instanceof Integer) {
            editor.putInt(key, (Integer) data);
        } else if (data instanceof Boolean) {
            editor.putBoolean(key, (Boolean) data);
        } else if (data instanceof String) {
            editor.putString(key, (String) data);
        } else if (data instanceof Float) {
            editor.putFloat(key, (Float) data);
        } else if (data instanceof Long) {
            editor.putLong(key, (Long) data);
        }
        editor.apply();
    }

    /**
     * 通用数据获取方法 (默认返回 String)
     */
    public static String getData(Context context, String key, String defValue) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(key, defValue);
    }

    /**
     * 通用数据获取方法 (根据默认值类型返回对应数据)
     * 支持 String, Integer, Boolean
     */
    public static Object getData(Context context, String key, Object defValue) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        if (defValue instanceof String) {
            return sp.getString(key, (String) defValue);
        } else if (defValue instanceof Integer) {
            return sp.getInt(key, (Integer) defValue);
        } else if (defValue instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defValue);
        } else if (defValue instanceof Float) {
            // 补充：既然 saveData 支持 Float，这里最好也支持
            return sp.getFloat(key, (Float) defValue);
        } else if (defValue instanceof Long) {
            // 补充：既然 saveData 支持 Long，这里最好也支持
            return sp.getLong(key, (Long) defValue);
        }

        return null;
    }
}
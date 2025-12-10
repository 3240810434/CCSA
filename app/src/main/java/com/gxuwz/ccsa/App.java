package com.gxuwz.ccsa;

import android.app.Application;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Admin;

public class App extends Application {
    private static AppDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();
        db = AppDatabase.getInstance(this);
        initDefaultAdmin();
    }

    // 初始化默认管理员账号
    private void initDefaultAdmin() {
        if (db.adminDao().findByAccount("1") == null) {
            Admin defaultAdmin = new Admin("1", "1", "悦景小区");
            db.adminDao().insert(defaultAdmin);
        }
    }

    public static AppDatabase getDb() {
        return db;
    }
}
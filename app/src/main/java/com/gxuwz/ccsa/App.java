package com.gxuwz.ccsa;

import android.app.Application;
import com.gxuwz.ccsa.db.AppDatabase;
import com.gxuwz.ccsa.model.Admin;

public class App extends Application {
    private static AppDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();
        // 获取数据库实例
        db = AppDatabase.getInstance(this);
        // 初始化所有小区管理员账号
        initDefaultAdmin();
    }

    // 初始化默认管理员账号
    private void initDefaultAdmin() {
        // 0. 悦景小区 (您代码里的默认账号)
        checkAndInsertAdmin("1", "1", "悦景小区");

        // 1. 梧桐小区
        checkAndInsertAdmin("11", "11", "梧桐小区");

        // 2. 阳光小区
        checkAndInsertAdmin("111", "111", "阳光小区");

        // 3. 锦园小区
        checkAndInsertAdmin("1111", "1111", "锦园小区");

        // 4. 幸福小区
        checkAndInsertAdmin("11111", "11111", "幸福小区");

        // 5. 芳邻小区
        checkAndInsertAdmin("111111", "111111", "芳邻小区");

        // 6. 逸景小区
        checkAndInsertAdmin("1111111", "1111111", "逸景小区");

        // 7. 康城小区
        checkAndInsertAdmin("11111111", "11111111", "康城小区");

        // 8. 沁园小区 (账号9个1，密码8个1)
        checkAndInsertAdmin("111111111", "11111111", "沁园小区");

        // 9. 静安小区 (账号10个1，密码8个1)
        checkAndInsertAdmin("1111111111", "11111111", "静安小区");
    }

    /**
     * 辅助方法：检查账号是否存在，不存在则插入
     */
    private void checkAndInsertAdmin(String account, String password, String community) {
        // 使用 DAO 查询账号是否存在
        if (db.adminDao().findByAccount(account) == null) {
            Admin admin = new Admin(account, password, community);
            db.adminDao().insert(admin);
        }
    }

    public static AppDatabase getDb() {
        return db;
    }
}
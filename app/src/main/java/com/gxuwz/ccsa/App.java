package com.gxuwz.ccsa;

import android.app.Application;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 原有的本地数据库初始化逻辑已移除，改由连接后端服务器
    }
}
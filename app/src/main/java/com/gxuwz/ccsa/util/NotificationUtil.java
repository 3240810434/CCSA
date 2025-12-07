// CCSA/app/src/main/java/com/gxuwz/ccsa/util/NotificationUtil.java
package com.gxuwz.ccsa.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.gxuwz.ccsa.R;

public class NotificationUtil {
    private static final String CHANNEL_ID = "vote_channel";
    private static final String CHANNEL_NAME = "小区投票通知";

    public static void sendVoteNotification(Context context, String title, String content) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 创建通知渠道（Android O及以上）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // 构建通知
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        // 发送通知
        notificationManager.notify((int) System.currentTimeMillis(), notification);
    }
}

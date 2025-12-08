package com.gxuwz.ccsa.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.model.Merchant;
import com.gxuwz.ccsa.model.Admin;
import com.gxuwz.ccsa.model.Vote;
import com.gxuwz.ccsa.model.VoteRecord;
import com.gxuwz.ccsa.model.PropertyFeeStandard;
import com.gxuwz.ccsa.model.PaymentRecord;
import com.gxuwz.ccsa.model.FeeAnnouncement;
import com.gxuwz.ccsa.model.PaymentAppeal;
import com.gxuwz.ccsa.model.RoomArea;
import com.gxuwz.ccsa.model.PropertyFeeBill;
import com.gxuwz.ccsa.model.Notification;
import com.gxuwz.ccsa.model.Repair;
// --- 新增引入 ---
import com.gxuwz.ccsa.model.Post;
import com.gxuwz.ccsa.model.PostMedia;
import com.gxuwz.ccsa.model.Comment;

// 添加TypeConverters注解注册日期转换器
@Database(
        entities = {
                User.class,
                Merchant.class,
                Admin.class,
                Vote.class,
                VoteRecord.class,
                PropertyFeeStandard.class,
                PaymentRecord.class,
                FeeAnnouncement.class,
                PaymentAppeal.class,
                RoomArea.class,
                PropertyFeeBill.class,
                Notification.class,
                Repair.class,
                // --- 新增实体 ---
                Post.class,
                PostMedia.class,
                Comment.class
        },
        version = 8, // 升级版本号，原为7
        exportSchema = false
)
@TypeConverters(DateConverter.class) // 关键：注册日期类型转换器
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    // 原有DAO
    public abstract UserDao userDao();
    public abstract MerchantDao merchantDao();
    public abstract AdminDao adminDao();
    public abstract VoteDao voteDao();
    public abstract VoteRecordDao voteRecordDao();
    public abstract RepairDao repairDao();
    // 原有新增DAO
    public abstract PropertyFeeStandardDao propertyFeeStandardDao();
    public abstract PaymentRecordDao paymentRecordDao();
    public abstract FeeAnnouncementDao feeAnnouncementDao();
    public abstract PaymentAppealDao paymentAppealDao();
    public abstract RoomAreaDao roomAreaDao();
    public abstract PropertyFeeBillDao propertyFeeBillDao();
    public abstract NotificationDao notificationDao();

    // --- 新增：生活动态DAO ---
    public abstract PostDao postDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "ccsa_database"
                            )
                            .allowMainThreadQueries()
                            // 允许破坏性迁移（数据库版本更新时清空数据重建表），开发阶段方便调试
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
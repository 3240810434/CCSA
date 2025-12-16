package com.gxuwz.ccsa.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

// 引入所有的 Model 类
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
import com.gxuwz.ccsa.model.Post;
import com.gxuwz.ccsa.model.PostMedia;
import com.gxuwz.ccsa.model.Comment;
import com.gxuwz.ccsa.model.ChatMessage;
import com.gxuwz.ccsa.model.HelpPost;
import com.gxuwz.ccsa.model.HelpPostMedia;
import com.gxuwz.ccsa.model.Product;
import com.gxuwz.ccsa.model.Order;
import com.gxuwz.ccsa.model.AfterSalesRecord;
import com.gxuwz.ccsa.model.AdminNotice; // 【新增】引入 AdminNotice 类

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
                Post.class,
                PostMedia.class,
                Comment.class,
                ChatMessage.class,
                HelpPost.class,
                HelpPostMedia.class,
                Product.class,
                Order.class,
                AfterSalesRecord.class,
                AdminNotice.class // 【新增】必须在这里注册新表 AdminNotice
        },
        version = 15, // 【修改】数据库版本号，基于原有的14升级为15
        exportSchema = false
)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    // --- DAO 定义 ---
    public abstract UserDao userDao();
    public abstract MerchantDao merchantDao();
    public abstract AdminDao adminDao();
    public abstract VoteDao voteDao();
    public abstract VoteRecordDao voteRecordDao();
    public abstract RepairDao repairDao();
    public abstract PropertyFeeStandardDao propertyFeeStandardDao();
    public abstract PaymentRecordDao paymentRecordDao();
    public abstract FeeAnnouncementDao feeAnnouncementDao();
    public abstract PaymentAppealDao paymentAppealDao();
    public abstract RoomAreaDao roomAreaDao();
    public abstract PropertyFeeBillDao propertyFeeBillDao();
    public abstract NotificationDao notificationDao();
    public abstract PostDao postDao();
    public abstract HelpPostDao helpPostDao();
    public abstract ChatDao chatDao();
    public abstract ProductDao productDao();
    public abstract OrderDao orderDao();
    public abstract AfterSalesRecordDao afterSalesRecordDao();

    // 【新增】注册 AdminNoticeDao
    public abstract AdminNoticeDao adminNoticeDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "ccsa_database"
                            )
                            .allowMainThreadQueries() // 允许主线程查询（保持原有设置）
                            .fallbackToDestructiveMigration() // 版本升级时清空数据重建
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
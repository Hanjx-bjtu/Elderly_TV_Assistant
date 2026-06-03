package com.elderly.tvassistant.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.elderly.tvassistant.model.Channel
import com.elderly.tvassistant.model.Favorite

/**
 * Room数据库单例类
 * 管理数据库的创建和版本迁移
 *
 * 数据库包含两张表：
 * - channel_table: 频道信息表
 * - favorite_table: 收藏记录表
 */
@Database(
    entities = [Channel::class, Favorite::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** 频道数据访问对象 */
    abstract fun channelDao(): ChannelDao

    /** 收藏数据访问对象 */
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * 获取数据库单例实例
         * 使用双重检查锁定（DCL）确保线程安全
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "elderly_tv_database"
                )
                    // 允许在主线程查询（开发阶段方便调试，生产环境应使用异步）
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

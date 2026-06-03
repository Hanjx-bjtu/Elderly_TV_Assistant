package com.elderly.tvassistant.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.elderly.tvassistant.model.Channel

/**
 * 频道数据访问对象（DAO）
 * 提供对频道表的增删改查操作
 * 使用Room注解定义SQL查询
 */
@Dao
interface ChannelDao {

    /**
     * 获取所有频道列表，按排序序号升序排列
     * 返回LiveData，数据变化时自动通知观察者
     */
    @Query("SELECT * FROM channel_table ORDER BY sort_order ASC")
    fun getAllChannels(): LiveData<List<Channel>>

    /**
     * 获取所有频道列表（挂起函数，用于协程中调用）
     */
    @Query("SELECT * FROM channel_table ORDER BY sort_order ASC")
    suspend fun getAllChannelsList(): List<Channel>

    /**
     * 根据频道ID获取单个频道
     */
    @Query("SELECT * FROM channel_table WHERE id = :channelId")
    suspend fun getChannelById(channelId: Int): Channel?

    /**
     * 获取频道总数（用于判断是否需要加载预设数据）
     */
    @Query("SELECT COUNT(*) FROM channel_table")
    suspend fun getChannelCount(): Int

    /**
     * 插入单个频道
     * 遇到主键冲突时替换（ON CONFLICT REPLACE）
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: Channel)

    /**
     * 批量插入频道（首次加载预设数据时使用）
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllChannels(channels: List<Channel>)

    /**
     * 更新频道信息
     */
    @Update
    suspend fun updateChannel(channel: Channel)

    /**
     * 删除频道
     */
    @Delete
    suspend fun deleteChannel(channel: Channel)
}

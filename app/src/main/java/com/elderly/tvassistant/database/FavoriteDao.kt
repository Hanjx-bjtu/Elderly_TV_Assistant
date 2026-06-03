package com.elderly.tvassistant.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.elderly.tvassistant.model.Channel
import com.elderly.tvassistant.model.Favorite

/**
 * 收藏数据访问对象（DAO）
 * 提供对收藏表的增删查操作
 * 通过联合查询获取收藏的频道完整信息
 */
@Dao
interface FavoriteDao {

    /**
     * 获取所有收藏的频道（联合查询channel_table）
     * 返回LiveData，数据变化时自动通知观察者
     */
    @Query("""
        SELECT c.* FROM channel_table c 
        INNER JOIN favorite_table f ON c.id = f.channel_id 
        ORDER BY f.favorite_time DESC
    """)
    fun getFavoriteChannels(): LiveData<List<Channel>>

    /**
     * 获取收藏的频道列表（挂起函数）
     */
    @Query("""
        SELECT c.* FROM channel_table c 
        INNER JOIN favorite_table f ON c.id = f.channel_id 
        ORDER BY f.favorite_time DESC
    """)
    suspend fun getFavoriteChannelsList(): List<Channel>

    /**
     * 根据频道ID获取收藏记录
     */
    @Query("SELECT * FROM favorite_table WHERE channel_id = :channelId")
    suspend fun getFavoriteByChannelId(channelId: Int): Favorite?

    /**
     * 添加频道到收藏
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavorite(favorite: Favorite)

    /**
     * 从收藏中移除
     */
    @Delete
    suspend fun removeFromFavorite(favorite: Favorite)

    /**
     * 根据频道ID移除收藏（更方便的删除方式）
     */
    @Query("DELETE FROM favorite_table WHERE channel_id = :channelId")
    suspend fun removeByChannelId(channelId: Int)

    /**
     * 判断某个频道是否已被收藏
     */
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_table WHERE channel_id = :channelId)")
    suspend fun isFavorite(channelId: Int): Boolean
}

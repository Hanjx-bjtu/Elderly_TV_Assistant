package com.elderly.tvassistant.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 收藏实体类
 * 对应数据库中的favorite_table表
 * 用户可将常看的频道加入收藏，便于快速切换
 *
 * @param id 收藏记录唯一标识（自增主键）
 * @param channelId 关联的频道ID，对应Channel.id
 * @param favoriteTime 收藏时间戳（毫秒级）
 */
@Entity(tableName = "favorite_table")
data class Favorite(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "channel_id")
    val channelId: Int,

    @ColumnInfo(name = "favorite_time")
    val favoriteTime: Long = System.currentTimeMillis()
)

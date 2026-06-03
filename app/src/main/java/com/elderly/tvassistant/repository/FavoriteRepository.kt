package com.elderly.tvassistant.repository

import androidx.lifecycle.LiveData
import com.elderly.tvassistant.database.ChannelDao
import com.elderly.tvassistant.database.FavoriteDao
import com.elderly.tvassistant.model.Channel

/**
 * 收藏数据仓库
 * 封装收藏数据的访问逻辑，为ViewModel提供统一的收藏操作接口
 * 协调FavoriteDao和ChannelDao的联合查询
 */
class FavoriteRepository(
    private val favoriteDao: FavoriteDao,
    private val channelDao: ChannelDao
) {

    /** 收藏频道列表的LiveData */
    val favoriteChannels: LiveData<List<Channel>> = favoriteDao.getFavoriteChannels()

    /**
     * 将频道添加到收藏
     * @param channelId 要收藏的频道ID
     */
    suspend fun addToFavorite(channelId: Int) {
        val favorite = com.elderly.tvassistant.model.Favorite(channelId = channelId)
        favoriteDao.addToFavorite(favorite)
    }

    /**
     * 将频道从收藏中移除
     * @param channelId 要移除的频道ID
     */
    suspend fun removeFromFavorite(channelId: Int) {
        favoriteDao.removeByChannelId(channelId)
    }

    /**
     * 判断频道是否已被收藏
     * @param channelId 频道ID
     * @return true表示已收藏
     */
    suspend fun isFavorite(channelId: Int): Boolean {
        return favoriteDao.isFavorite(channelId)
    }

    /**
     * 切换收藏状态：已收藏则取消，未收藏则添加
     * @param channelId 频道ID
     * @return 切换后的收藏状态
     */
    suspend fun toggleFavorite(channelId: Int): Boolean {
        return if (favoriteDao.isFavorite(channelId)) {
            favoriteDao.removeByChannelId(channelId)
            false
        } else {
            val favorite = com.elderly.tvassistant.model.Favorite(channelId = channelId)
            favoriteDao.addToFavorite(favorite)
            true
        }
    }
}

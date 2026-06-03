package com.elderly.tvassistant.repository

import androidx.lifecycle.LiveData
import com.elderly.tvassistant.database.ChannelDao
import com.elderly.tvassistant.model.Channel

/**
 * 频道数据仓库
 * 封装频道数据的访问逻辑，为ViewModel提供统一的数据接口
 * 遵循Repository模式，隔离数据来源
 */
class ChannelRepository(private val channelDao: ChannelDao) {

    /** 所有频道的LiveData，UI层直接观察此数据 */
    val allChannels: LiveData<List<Channel>> = channelDao.getAllChannels()

    /**
     * 获取频道ID对应的频道
     */
    suspend fun getChannelById(channelId: Int): Channel? {
        return channelDao.getChannelById(channelId)
    }

    /**
     * 批量插入频道（首次启动加载预设数据时使用）
     */
    suspend fun insertChannels(channels: List<Channel>) {
        channelDao.insertAllChannels(channels)
    }

    /**
     * 更新单个频道信息
     */
    suspend fun updateChannel(channel: Channel) {
        channelDao.updateChannel(channel)
    }

    /**
     * 获取所有频道列表（非LiveData版本）
     */
    suspend fun getAllChannelsList(): List<Channel> {
        return channelDao.getAllChannelsList()
    }
}

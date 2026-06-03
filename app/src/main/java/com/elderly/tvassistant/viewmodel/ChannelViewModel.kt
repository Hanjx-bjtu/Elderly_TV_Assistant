package com.elderly.tvassistant.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.elderly.tvassistant.model.Channel
import com.elderly.tvassistant.repository.ChannelRepository
import com.elderly.tvassistant.repository.FavoriteRepository
import kotlinx.coroutines.launch

/**
 * 频道ViewModel
 * 管理频道列表数据和收藏状态，作为UI层与数据层的桥梁
 */
class ChannelViewModel(
    private val channelRepository: ChannelRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    /** 所有频道列表 */
    val allChannels: LiveData<List<Channel>> = channelRepository.allChannels

    /** 当前频道是否被收藏 */
    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    /** 收藏频道列表 */
    val favoriteChannels: LiveData<List<Channel>> = favoriteRepository.favoriteChannels

    fun checkFavorite(channelId: Int) {
        viewModelScope.launch {
            _isFavorite.value = favoriteRepository.isFavorite(channelId)
        }
    }

    fun toggleFavorite(channelId: Int) {
        viewModelScope.launch {
            val newState = favoriteRepository.toggleFavorite(channelId)
            _isFavorite.value = newState
        }
    }

    /**
     * ViewModel工厂类
     * 用于创建带参数的ViewModel实例
     */
    class ChannelViewModelFactory(
        private val channelRepository: ChannelRepository,
        private val favoriteRepository: FavoriteRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChannelViewModel::class.java)) {
                return ChannelViewModel(channelRepository, favoriteRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

package com.elderly.tvassistant.utils

import android.content.Context
import android.util.Log
import com.elderly.tvassistant.model.Channel
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * 频道数据加载器
 * 从assets目录中的channels.json文件加载预设频道数据
 * 首次启动时使用，将数据写入Room数据库
 */
object ChannelDataLoader {

    private const val TAG = "ChannelDataLoader"
//    private const val CHANNELS_FILE = "com/elderly/tvassistant/utils/channels.json"

    private const val CHANNELS_FILE = "channels.json"
    /**
     * 数据模型（JSON解析用）
     */
    data class ChannelData(
        val id: Int = 0,
        val name: String = "",
        val display_name: String? = null,
        val keywords: List<String> = emptyList(),
        val url: String = "",
        val icon_url: String? = null,
        val sort_order: Int = 0
    )

    /**
     * 从assets目录加载频道数据
     * @param context 上下文
     * @return 频道列表，加载失败返回空列表
     */
    fun loadFromAssets(context: Context): List<Channel> {
        return try {
            val json = readAssetFile(context, CHANNELS_FILE)
            if (json.isNullOrEmpty()) {
                Log.e(TAG, "频道数据文件为空")
                return emptyList()
            }

            val jsonObject = JsonParser.parseString(json).asJsonObject
            val channelsJson = jsonObject.getAsJsonArray("channels").toString()

            val typee = object : TypeToken<List<ChannelData>>() {}.type
            val channelDataList: List<ChannelData> = Gson().fromJson(channelsJson, typee)

            channelDataList.map { data ->
                Channel(
                    id = data.id,
                    name = data.name,
                    displayName = data.display_name,
                    keywordsJson = Gson().toJson(data.keywords),
                    url = data.url,
                    iconUrl = data.icon_url,
                    sortOrder = data.sort_order
                )
            }.also {
                Log.d(TAG, "成功加载 ${it.size} 个频道")
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载频道数据失败: ${e.message}")
            // 返回默认频道列表作为兜底
            getDefaultChannels()
        }
    }

    /**
     * 读取assets目录中的文本文件
     */
    private fun readAssetFile(context: Context, fileName: String): String? {
        return try {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val sb = StringBuilder()
            reader.useLines { lines ->
                lines.forEach { sb.append(it).append('\n') }
            }
            sb.toString().trim()
        } catch (e: IOException) {
            Log.e(TAG, "读取文件失败: $fileName, ${e.message}")
            null
        }
    }

    /**
     * 获取默认频道列表（硬编码兜底方案）
     * 当JSON文件加载失败时使用
     */
    fun getDefaultChannels(): List<Channel> {
        return listOf(
            Channel(
                name = "CCTV-1 综合",
                displayName = "中央一台",
                keywordsJson = "[\"中央一台\",\"央视一套\",\"综合频道\",\"cctv1\",\"cctv 1\"]",
                url = "https://tv.cctv.com/live/cctv1",
                iconUrl = "ic_cctv1",
                sortOrder = 1
            ),
            Channel(
                name = "CCTV-2 财经",
                displayName = "中央二台",
                keywordsJson = "[\"中央二台\",\"财经频道\",\"cctv2\",\"cctv 2\"]",
                url = "https://tv.cctv.com/live/cctv2",
                iconUrl = "ic_cctv2",
                sortOrder = 2
            ),
            Channel(
                name = "CCTV-3 综艺",
                displayName = "中央三台",
                keywordsJson = "[\"中央三台\",\"综艺频道\",\"cctv3\",\"cctv 3\"]",
                url = "https://tv.cctv.com/live/cctv3",
                iconUrl = "ic_cctv3",
                sortOrder = 3
            ),
            Channel(
                name = "CCTV-4 中文国际",
                displayName = "中央四台",
                keywordsJson = "[\"中央四台\",\"中文国际\",\"cctv4\",\"cctv 4\"]",
                url = "https://tv.cctv.com/live/cctv4",
                iconUrl = "ic_cctv4",
                sortOrder = 4
            ),
            Channel(
                name = "CCTV-5 体育",
                displayName = "中央五台",
                keywordsJson = "[\"中央五台\",\"体育频道\",\"cctv5\",\"cctv 5\"]",
                url = "https://tv.cctv.com/live/cctv5",
                iconUrl = "ic_cctv5",
                sortOrder = 5
            ),
            Channel(
                name = "CCTV-6 电影",
                displayName = "中央六台",
                keywordsJson = "[\"中央六台\",\"电影频道\",\"cctv6\",\"cctv 6\"]",
                url = "https://tv.cctv.com/live/cctv6",
                iconUrl = "ic_cctv6",
                sortOrder = 6
            ),
            Channel(
                name = "CCTV-7 国防军事",
                displayName = "中央七台",
                keywordsJson = "[\"中央七台\",\"国防军事\",\"cctv7\",\"cctv 7\"]",
                url = "https://tv.cctv.com/live/cctv7",
                iconUrl = "ic_cctv7",
                sortOrder = 7
            ),
            Channel(
                name = "CCTV-8 电视剧",
                displayName = "中央八台",
                keywordsJson = "[\"中央八台\",\"电视剧频道\",\"cctv8\",\"cctv 8\"]",
                url = "https://tv.cctv.com/live/cctv8",
                iconUrl = "ic_cctv8",
                sortOrder = 8
            )
        )
    }
}

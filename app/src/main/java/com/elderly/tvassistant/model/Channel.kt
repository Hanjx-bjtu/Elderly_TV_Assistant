package com.elderly.tvassistant.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 频道实体类
 * 对应数据库中的channel_table表
 *
 * @param id 频道唯一标识（自增主键）
 * @param name 频道名称（如"CCTV-1 综合"）
 * @param displayName 大字版显示名称（如"中央一台"），用于语音播报和列表显示
 * @param keywordsJson 关键词JSON字符串，用于语音匹配（存储为JSON数组格式）
 * @param url 视频源地址（WebView加载的URL）
 * @param iconUrl 频道图标资源名称
 * @param sortOrder 排序序号，数值越小越靠前
 */
@Entity(tableName = "channel_table")
data class Channel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String = "",

    @ColumnInfo(name = "display_name")
    val displayName: String? = null,

    @ColumnInfo(name = "keywords")
    val keywordsJson: String = "[]",

    @ColumnInfo(name = "url")
    val url: String = "",

    @ColumnInfo(name = "icon_url")
    val iconUrl: String? = null,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0
) {
    /**
     * 从JSON字符串解析关键词列表
     * 例如: "[\"中央一台\", \"央视一套\", \"综合频道\"]" -> ["中央一台", "央视一套", "综合频道"]
     */
    @get:Ignore
    val keywords: List<String>
        get() {
            return try {
                val type = object : TypeToken<List<String>>() {}.type
                Gson().fromJson(keywordsJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
}

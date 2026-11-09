package com.anilog.omor.dev.data.database

import androidx.room.TypeConverter
import com.anilog.omor.dev.domain.model.AnimeStatus
import org.json.JSONArray

class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list.isNullOrEmpty()) return "[]"
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toStringList(jsonString: String?): List<String> {
        if (jsonString.isNullOrBlank()) return emptyList()
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromAnimeStatus(status: AnimeStatus?): String {
        return (status ?: AnimeStatus.PLAN_TO_WATCH).name
    }

    @TypeConverter
    fun toAnimeStatus(value: String?): AnimeStatus {
        return AnimeStatus.fromString(value)
    }
}

package com.anilog.omor.dev.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

data class OnlineAnimeMetadata(
    val malId: Long,
    val title: String,
    val originalTitle: String,
    val imageUrl: String?,
    val synopsis: String?,
    val genres: List<String>,
    val year: Int?,
    val episodes: Int
)

class JikanApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun searchAnime(query: String): Result<List<OnlineAnimeMetadata>> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext Result.success(emptyList())
        try {
            val encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.name())
            val url = "https://api.jikan.moe/v4/anime?q=$encoded&limit=10&sfw=true"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "AniLog-Android-App/1.0 (Personal Anime Journal)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Online lookup returned HTTP ${response.code}. Please check internet connection.")
                )
            }

            val bodyString = response.body?.string() ?: return@withContext Result.success(emptyList())
            val root = JSONObject(bodyString)
            val dataArray = root.optJSONArray("data") ?: return@withContext Result.success(emptyList())

            val results = mutableListOf<OnlineAnimeMetadata>()
            for (i in 0 until dataArray.length()) {
                val item = dataArray.getJSONObject(i)
                val malId = item.optLong("mal_id", 0L)
                val title = item.optString("title", "Unknown")
                val titleJapanese = item.optString("title_japanese", "")

                // Images
                val imagesObj = item.optJSONObject("images")
                val webpObj = imagesObj?.optJSONObject("webp") ?: imagesObj?.optJSONObject("jpg")
                val imageUrl = webpObj?.optString("large_image_url") ?: webpObj?.optString("image_url")

                val synopsis = item.optString("synopsis", "")
                val episodes = item.optInt("episodes", 0)

                // Year
                var year: Int? = if (item.has("year") && !item.isNull("year")) item.getInt("year") else null
                if (year == null) {
                    val aired = item.optJSONObject("aired")
                    val prop = aired?.optJSONObject("prop")
                    val from = prop?.optJSONObject("from")
                    if (from != null && from.has("year") && !from.isNull("year")) {
                        year = from.getInt("year")
                    }
                }

                // Genres
                val genresList = mutableListOf<String>()
                item.optJSONArray("genres")?.let { arr ->
                    for (g in 0 until arr.length()) {
                        val gObj = arr.getJSONObject(g)
                        genresList.add(gObj.optString("name"))
                    }
                }

                results.add(
                    OnlineAnimeMetadata(
                        malId = malId,
                        title = title,
                        originalTitle = titleJapanese,
                        imageUrl = imageUrl,
                        synopsis = synopsis,
                        genres = genresList,
                        year = year,
                        episodes = episodes
                    )
                )
            }

            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

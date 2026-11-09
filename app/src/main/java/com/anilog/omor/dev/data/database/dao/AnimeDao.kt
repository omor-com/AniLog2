package com.anilog.omor.dev.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.anilog.omor.dev.data.database.entity.AnimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {
    @Query("SELECT * FROM anime ORDER BY createdAt DESC")
    fun getAllAnime(): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime ORDER BY createdAt DESC")
    suspend fun getAllAnimeList(): List<AnimeEntity>

    @Query("SELECT * FROM anime WHERE id = :id LIMIT 1")
    fun getAnimeById(id: Long): Flow<AnimeEntity?>

    @Query("SELECT * FROM anime WHERE id = :id LIMIT 1")
    suspend fun getAnimeByIdOnce(id: Long): AnimeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnime(anime: AnimeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(animeList: List<AnimeEntity>): List<Long>

    @Update
    suspend fun updateAnime(anime: AnimeEntity)

    @Delete
    suspend fun deleteAnime(anime: AnimeEntity)

    @Query("DELETE FROM anime WHERE id = :id")
    suspend fun deleteAnimeById(id: Long)

    @Query("DELETE FROM anime")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM anime")
    fun getAnimeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM anime")
    suspend fun getAnimeCountOnce(): Int

    @Query("SELECT DISTINCT genres FROM anime")
    fun getAllRawGenres(): Flow<List<String>>
}

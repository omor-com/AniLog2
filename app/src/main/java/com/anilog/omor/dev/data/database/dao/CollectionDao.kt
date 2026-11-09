package com.anilog.omor.dev.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.data.database.entity.CollectionAnimeCrossRef
import com.anilog.omor.dev.data.database.entity.CollectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collections ORDER BY createdAt DESC")
    fun getAllCollections(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections ORDER BY createdAt DESC")
    suspend fun getAllCollectionsList(): List<CollectionEntity>

    @Query("SELECT * FROM collections WHERE id = :id LIMIT 1")
    fun getCollectionById(id: Long): Flow<CollectionEntity?>

    @Query("SELECT * FROM collections WHERE id = :id LIMIT 1")
    suspend fun getCollectionByIdOnce(id: Long): CollectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCollections(collections: List<CollectionEntity>): List<Long>

    @Update
    suspend fun updateCollection(collection: CollectionEntity)

    @Delete
    suspend fun deleteCollection(collection: CollectionEntity)

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun deleteCollectionById(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAnimeToCollection(crossRef: CollectionAnimeCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCrossRefs(crossRefs: List<CollectionAnimeCrossRef>)

    @Query("DELETE FROM collection_anime_cross_ref WHERE collectionId = :collectionId AND animeId = :animeId")
    suspend fun removeAnimeFromCollection(collectionId: Long, animeId: Long)

    @Query("""
        SELECT a.* FROM anime a
        INNER JOIN collection_anime_cross_ref c ON a.id = c.animeId
        WHERE c.collectionId = :collectionId
        ORDER BY c.sortOrder ASC, a.createdAt DESC
    """)
    fun getAnimeForCollection(collectionId: Long): Flow<List<AnimeEntity>>

    @Query("""
        SELECT a.* FROM anime a
        INNER JOIN collection_anime_cross_ref c ON a.id = c.animeId
        WHERE c.collectionId = :collectionId
        ORDER BY c.sortOrder ASC, a.createdAt DESC
    """)
    suspend fun getAnimeForCollectionOnce(collectionId: Long): List<AnimeEntity>

    @Query("SELECT * FROM collection_anime_cross_ref WHERE collectionId = :collectionId ORDER BY sortOrder ASC")
    suspend fun getCrossRefsForCollection(collectionId: Long): List<CollectionAnimeCrossRef>

    @Query("SELECT * FROM collection_anime_cross_ref")
    suspend fun getAllCrossRefs(): List<CollectionAnimeCrossRef>

    @Update
    suspend fun updateCrossRef(crossRef: CollectionAnimeCrossRef)

    @Query("DELETE FROM collections")
    suspend fun deleteAllCollections()

    @Query("DELETE FROM collection_anime_cross_ref")
    suspend fun deleteAllCrossRefs()
}

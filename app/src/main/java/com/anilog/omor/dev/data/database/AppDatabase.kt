package com.anilog.omor.dev.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.anilog.omor.dev.data.database.dao.AnimeDao
import com.anilog.omor.dev.data.database.dao.CollectionDao
import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.data.database.entity.CollectionAnimeCrossRef
import com.anilog.omor.dev.data.database.entity.CollectionEntity

@Database(
    entities = [
        AnimeEntity::class,
        CollectionEntity::class,
        CollectionAnimeCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao
    abstract fun collectionDao(): CollectionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "anilog.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

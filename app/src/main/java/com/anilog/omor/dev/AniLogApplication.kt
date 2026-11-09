package com.anilog.omor.dev

import android.app.Application
import com.anilog.omor.dev.data.backup.BackupManager
import com.anilog.omor.dev.data.database.AppDatabase
import com.anilog.omor.dev.data.repository.AnimeRepository
import com.anilog.omor.dev.data.repository.UserPreferencesRepository

class AniLogApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var animeRepository: AnimeRepository
        private set

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    lateinit var backupManager: BackupManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        animeRepository = AnimeRepository(this, database)
        userPreferencesRepository = UserPreferencesRepository(this)
        backupManager = BackupManager(this, database)
    }
}

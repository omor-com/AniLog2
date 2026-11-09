package com.anilog.omor.dev.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "collection_anime_cross_ref",
    primaryKeys = ["collectionId", "animeId"],
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AnimeEntity::class,
            parentColumns = ["id"],
            childColumns = ["animeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["collectionId"]),
        Index(value = ["animeId"])
    ]
)
data class CollectionAnimeCrossRef(
    val collectionId: Long,
    val animeId: Long,
    val sortOrder: Int = 0
)

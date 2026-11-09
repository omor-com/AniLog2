package com.anilog.omor.dev.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Library : Screen("library")
    data object Watchlist : Screen("watchlist")
    data object Collections : Screen("collections")
    data object CollectionDetail : Screen("collection/{collectionId}") {
        fun createRoute(collectionId: Long) = "collection/$collectionId"
    }
    data object Statistics : Screen("statistics")
    data object Compare : Screen("compare")
    data object AddEditAnime : Screen("add_edit_anime?animeId={animeId}") {
        fun createRoute(animeId: Long? = null) = if (animeId != null) "add_edit_anime?animeId=$animeId" else "add_edit_anime"
    }
    data object AnimeDetail : Screen("anime_detail/{animeId}") {
        fun createRoute(animeId: Long) = "anime_detail/$animeId"
    }
    data object ReviewCard : Screen("review_card/{animeId}") {
        fun createRoute(animeId: Long) = "review_card/$animeId"
    }
    data object BackupRestore : Screen("backup_restore")
    data object Settings : Screen("settings")
    data object PrivacyPolicy : Screen("privacy_policy")
    data object PasscodeLock : Screen("passcode_lock")
}

package com.anilog.omor.dev.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.anilog.omor.dev.AniLogApplication
import com.anilog.omor.dev.ui.anime.AddEditAnimeScreen
import com.anilog.omor.dev.ui.anime.AddEditViewModel
import com.anilog.omor.dev.ui.anime.AnimeDetailScreen
import com.anilog.omor.dev.ui.anime.AnimeDetailViewModel
import com.anilog.omor.dev.ui.backup.BackupRestoreScreen
import com.anilog.omor.dev.ui.backup.BackupViewModel
import com.anilog.omor.dev.ui.bulk.BulkImportScreen
import com.anilog.omor.dev.ui.bulk.BulkImportViewModel
import com.anilog.omor.dev.ui.collections.CollectionDetailScreen
import com.anilog.omor.dev.ui.collections.CollectionsScreen
import com.anilog.omor.dev.ui.collections.CollectionsViewModel
import com.anilog.omor.dev.ui.comparison.CompareScreen
import com.anilog.omor.dev.ui.comparison.CompareViewModel
import com.anilog.omor.dev.ui.components.AniLogBottomBar
import com.anilog.omor.dev.ui.export.ExportFormatScreen
import com.anilog.omor.dev.ui.home.HomeScreen
import com.anilog.omor.dev.ui.home.HomeViewModel
import com.anilog.omor.dev.ui.language.LanguageSelectionScreen
import com.anilog.omor.dev.ui.library.LibraryScreen
import com.anilog.omor.dev.ui.library.LibraryViewModel
import com.anilog.omor.dev.ui.privacy.PasscodeLockScreen
import com.anilog.omor.dev.ui.reviewcard.ReviewCardGeneratorScreen
import com.anilog.omor.dev.ui.reviewcard.ReviewCardViewModel
import com.anilog.omor.dev.ui.settings.PrivacyPolicyScreen
import com.anilog.omor.dev.ui.settings.SettingsScreen
import com.anilog.omor.dev.ui.settings.SettingsViewModel
import com.anilog.omor.dev.ui.statistics.StatisticsScreen
import com.anilog.omor.dev.ui.statistics.StatisticsViewModel

@Composable
fun AniLogNavGraph(
    app: AniLogApplication,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // 1. First-launch Language Selection
    val hasCompletedLangSelection by app.userPreferencesRepository.hasCompletedLanguageSelection.collectAsStateWithLifecycle(true)
    var localLangCompleted by remember { mutableStateOf(false) }

    if (!hasCompletedLangSelection && !localLangCompleted) {
        LanguageSelectionScreen(
            preferencesRepository = app.userPreferencesRepository,
            onLanguageSelected = { localLangCompleted = true }
        )
        return
    }

    // 2. Passcode & Enforced Recovery
    val isPasscodeEnabled by app.userPreferencesRepository.isPasscodeEnabled.collectAsStateWithLifecycle(false)
    val activeRecoveryDeadline by app.userPreferencesRepository.activeRecoveryDeadline.collectAsStateWithLifecycle(0L)
    val isRecoveryActive = app.userPreferencesRepository.isRecoveryActive(activeRecoveryDeadline)
    var isUnlocked by remember { mutableStateOf(false) }

    if ((isPasscodeEnabled || isRecoveryActive) && !isUnlocked) {
        PasscodeLockScreen(
            preferencesRepository = app.userPreferencesRepository,
            onUnlocked = { isUnlocked = true }
        )
        return
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = setOf(
        Screen.Home.route,
        Screen.Library.route,
        Screen.Watchlist.route,
        Screen.Statistics.route,
        Screen.Settings.route
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                AniLogBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Home Screen
            composable(Screen.Home.route) {
                val homeViewModel = remember {
                    HomeViewModel(app.animeRepository, app.userPreferencesRepository)
                }
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToAddAnime = { navController.navigate(Screen.AddEditAnime.createRoute()) },
                    onNavigateToAnimeDetail = { id -> navController.navigate(Screen.AnimeDetail.createRoute(id)) },
                    onNavigateToCollections = { navController.navigate(Screen.Collections.route) },
                    onNavigateToCompare = { navController.navigate(Screen.Compare.route) }
                )
            }

            // 2. Library Screen
            composable(Screen.Library.route) {
                val libraryViewModel = remember {
                    LibraryViewModel(app.animeRepository)
                }
                LibraryScreen(
                    viewModel = libraryViewModel,
                    onNavigateToAnimeDetail = { id -> navController.navigate(Screen.AnimeDetail.createRoute(id)) },
                    onNavigateToAddAnime = { navController.navigate(Screen.AddEditAnime.createRoute()) }
                )
            }

            // 3. Watchlist Screen
            composable(Screen.Watchlist.route) {
                val watchlistViewModel = remember {
                    com.anilog.omor.dev.ui.watchlist.WatchlistViewModel(app.animeRepository)
                }
                com.anilog.omor.dev.ui.watchlist.WatchlistScreen(
                    viewModel = watchlistViewModel,
                    onNavigateToDetail = { id -> navController.navigate(Screen.AnimeDetail.createRoute(id)) },
                    onNavigateToAddAnime = { navController.navigate(Screen.AddEditAnime.createRoute()) }
                )
            }

            // 4. Collections Screen
            composable(Screen.Collections.route) {
                val collectionsViewModel = remember {
                    CollectionsViewModel(app.animeRepository)
                }
                CollectionsScreen(
                    viewModel = collectionsViewModel,
                    onNavigateToCollectionDetail = { id -> navController.navigate(Screen.CollectionDetail.createRoute(id)) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 4b. Collection Detail Screen
            composable(
                route = Screen.CollectionDetail.route,
                arguments = listOf(navArgument("collectionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val colId = backStackEntry.arguments?.getLong("collectionId") ?: 0L
                val collectionsViewModel = remember {
                    CollectionsViewModel(app.animeRepository)
                }
                CollectionDetailScreen(
                    collectionId = colId,
                    viewModel = collectionsViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAnimeDetail = { id -> navController.navigate(Screen.AnimeDetail.createRoute(id)) }
                )
            }

            // 5. Statistics Screen
            composable(Screen.Statistics.route) {
                val statsViewModel = remember {
                    StatisticsViewModel(app.animeRepository)
                }
                StatisticsScreen(
                    viewModel = statsViewModel,
                    onNavigateToAnimeDetail = { id -> navController.navigate(Screen.AnimeDetail.createRoute(id)) }
                )
            }

            // 6. Compare Screen
            composable(Screen.Compare.route) {
                val compareViewModel = remember {
                    CompareViewModel(app.animeRepository)
                }
                CompareScreen(
                    viewModel = compareViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 7. Add / Edit Anime
            composable(
                route = Screen.AddEditAnime.route,
                arguments = listOf(
                    navArgument("animeId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val animeId = backStackEntry.arguments?.getLong("animeId")?.takeIf { it > 0 }
                val addEditViewModel = remember(animeId) {
                    AddEditViewModel(app.animeRepository)
                }
                AddEditAnimeScreen(
                    animeId = animeId,
                    viewModel = addEditViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 8. Anime Detail
            composable(
                route = Screen.AnimeDetail.route,
                arguments = listOf(navArgument("animeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val animeId = backStackEntry.arguments?.getLong("animeId") ?: 0L
                val detailViewModel = remember(animeId) {
                    AnimeDetailViewModel(animeId, app.animeRepository)
                }
                AnimeDetailScreen(
                    viewModel = detailViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id -> navController.navigate(Screen.AddEditAnime.createRoute(id)) },
                    onNavigateToReviewCard = { id -> navController.navigate(Screen.ReviewCard.createRoute(id)) }
                )
            }

            // 9. Review Card Generator / Sharing Studio
            composable(
                route = Screen.ReviewCard.route,
                arguments = listOf(navArgument("animeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val animeId = backStackEntry.arguments?.getLong("animeId") ?: 0L
                val reviewCardViewModel = remember(animeId) {
                    ReviewCardViewModel(animeId, app.animeRepository)
                }
                ReviewCardGeneratorScreen(
                    animeId = animeId,
                    viewModel = reviewCardViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 10. Bulk Import Screen
            composable(Screen.BulkImport.route) {
                val bulkViewModel = remember {
                    BulkImportViewModel(app.animeRepository)
                }
                BulkImportScreen(
                    viewModel = bulkViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 11. Export Formats Screen
            composable(Screen.ExportFormats.route) {
                ExportFormatScreen(
                    repository = app.animeRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 12. Backup & Restore
            composable(Screen.BackupRestore.route) {
                val backupViewModel = remember {
                    BackupViewModel(app.backupManager)
                }
                BackupRestoreScreen(
                    viewModel = backupViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 13. Settings
            composable(Screen.Settings.route) {
                val settingsViewModel = remember {
                    SettingsViewModel(app.userPreferencesRepository)
                }
                SettingsScreen(
                    viewModel = settingsViewModel,
                    preferencesRepository = app.userPreferencesRepository,
                    onNavigateToBackup = { navController.navigate(Screen.BackupRestore.route) },
                    onNavigateToBulkImport = { navController.navigate(Screen.BulkImport.route) },
                    onNavigateToExportFormats = { navController.navigate(Screen.ExportFormats.route) },
                    onNavigateToPrivacyPolicy = { navController.navigate(Screen.PrivacyPolicy.route) }
                )
            }

            // 14. Privacy Policy
            composable(Screen.PrivacyPolicy.route) {
                PrivacyPolicyScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

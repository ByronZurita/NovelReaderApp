package com.example.novelreaderapp.ui.screens.common

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.novelreaderapp.ui.components.NovelReaderAppTheme
import com.example.novelreaderapp.ui.screens.*
import com.example.novelreaderapp.viewmodel.*

/**
 * Object holding all navigation route constants and helper functions.
 * Replaces the former Routes.kt.
 */
object AppRoutes {
    /** Home Screen route */
    const val Home = "home"

    /** RoyalRoad scraper source screen */
    const val RoyalRoad = "royalroad"

    /** NovelBin scraper source screen */
    const val NovelBin = "novelbin"

    /** Settings screen */
    const val Settings = "settings"

    /** Authentication screen (login/register) */
    const val AuthScreen = "auth"

    /** Chapter List screen base route */
    const val ChapterList = "chapterList/{novelId}/{novelUrl}"

    /** Chapter Content screen with chapterIndex argument */
    const val ChapterContent = "chapterContent/{chapterIndex}"

    /**
     * Helper to build ChapterList route with URL encoded parameters.
     * @param novelUrl the URL of the novel (encoded)
     * @param novelId the unique ID of the novel
     * @param novelTitle the title of the novel (encoded)
     * @param coverUrl the cover image URL (encoded)
     */

    fun chapterListRoute(novelId: String, novelUrl: String): String =
        "chapterList/$novelId/${Uri.encode(novelUrl)}"

    /**
     * Helper to build ChapterContent route with chapter index.
     */
    fun chapterContentRoute(index: Int): String = "chapterContent/$index"
}

/**
 * Main App Navigation composable which defines navigation graph and routes.
 *
 * @param authViewModel AuthViewModel instance for auth-related screens.
 * @param chapterViewModel ChapterViewModel instance for novel/chapter screens.
 * @param settingsViewModel SettingsViewModel instance for settings screen.
 */
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    chapterViewModel: ChapterViewModel,
    settingsViewModel: SettingsViewModel,
) {
    val navController = rememberNavController()

    NovelReaderAppTheme {
        Scaffold(topBar = {}) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppRoutes.Home,
                modifier = Modifier.padding(innerPadding)
            ) {

                // Home Screen
                composable(AppRoutes.Home) {
                    HomeScreen(
                        authViewModel = authViewModel,
                        onScraperClick = { source ->
                            when (source.id) {
                                "royalroad" -> navController.navigate(AppRoutes.RoyalRoad)
                                "novelbin" -> navController.navigate(AppRoutes.NovelBin)
                            }
                        },
                        onNavigateTo = { navController.navigate(it) }
                    )
                }

                // NovelBin source main screen
                composable(AppRoutes.NovelBin) {
                    NovelBinScreen(
                        onNovelClick = { novelId, novelUrl, novelTitle, coverUrl ->
                            chapterViewModel.loadChapters(novelId, novelUrl)
                            navController.navigate(
                                AppRoutes.chapterListRoute(
                                    novelId,
                                    novelUrl
                                )
                            )
                        },
                        onNavigateToSettings = { navController.navigate(AppRoutes.Settings) }
                    )
                }

                // RoyalRoad source main screen
                composable(AppRoutes.RoyalRoad) {
                    RoyalRoadScreen(
                        onNovelClick = { novelId, novelUrl, novelTitle, coverUrl ->
                            chapterViewModel.loadChapters(novelId, novelUrl)
                            navController.navigate(
                                AppRoutes.chapterListRoute(
                                    novelId,
                                    novelUrl
                                )
                            )
                        },
                        onNavigateToSettings = { navController.navigate(AppRoutes.Settings) }
                    )
                }


                // Chapter List Screen with multiple arguments
                composable(
                    route = AppRoutes.ChapterList,
                    arguments = listOf(
                        navArgument("novelId") { type = NavType.StringType },
                        navArgument("novelUrl") { type = NavType.StringType }
                    )
                ) { backStackEntry ->

                    val novelId = backStackEntry.arguments?.getString("novelId") ?: ""
                    val novelUrl = backStackEntry.arguments?.getString("novelUrl") ?: ""

                    // Obtain ViewModel with backStackEntry as owner to share the same instance
                    val chapterViewModel: ChapterViewModel = viewModel(backStackEntry)

                    LaunchedEffect(novelId, novelUrl) {
                        chapterViewModel.loadChapters(novelId, novelUrl)
                    }

                    ChapterListScreen(
                        viewModel = chapterViewModel,  // Pass the ViewModel instance here!
                        onChapterClick = { chapter ->
                            val chapters = chapterViewModel.chapters.value
                            val index = chapters.indexOf(chapter)
                            navController.navigate(AppRoutes.chapterContentRoute(index))
                        },
                        onNavigateToSettings = {
                            navController.navigate(AppRoutes.Settings)
                        }
                    )
                }




                // Chapter Content Screen with chapterIndex parameter
                composable(
                    route = AppRoutes.ChapterContent,
                    arguments = listOf(navArgument("chapterIndex") { type = NavType.StringType })
                ) { backStackEntry ->
                    val chapterIndex = backStackEntry.arguments
                        ?.getString("chapterIndex")?.toIntOrNull() ?: 0
                    val chapters by chapterViewModel.chapters.collectAsState()

                    ChapterHostScreen(
                        chapters = chapters,
                        viewModel = chapterViewModel,
                        startIndex = chapterIndex,
                        onNavigateToSettings = {
                            navController.navigate(AppRoutes.Settings)
                        },
                        settingsViewModel = settingsViewModel
                    )
                }

                // Authentication Screen (login/register)
                composable(
                    route = AppRoutes.AuthScreen + "?autoNavigate={autoNavigate}",
                    arguments = listOf(navArgument("autoNavigate") {
                        type = NavType.BoolType
                        defaultValue = false
                    })
                ) { backStackEntry ->
                    val autoNavigate = backStackEntry.arguments
                        ?.getBoolean("autoNavigate") ?: false
                    AuthScreen(
                        authViewModel = authViewModel,
                        autoNavigate = autoNavigate,
                        onAuthSuccess = {
                            navController.navigate(AppRoutes.Home) {
                                popUpTo(AppRoutes.AuthScreen) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                // Settings Screen
                composable(AppRoutes.Settings) {
                    SettingsScreen(
                        settingsViewModel = settingsViewModel,
                        onTTSStart = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

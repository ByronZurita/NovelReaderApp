package com.byron.novelreaderapp.ui.screens.common

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
import com.byron.novelreaderapp.ui.theme.NovelReaderAppTheme
import com.byron.novelreaderapp.ui.screens.*
import com.byron.novelreaderapp.viewmodel.*

/**
 * Object holding all navigation route constants and helper functions.
 */
object AppRoutes {
    const val Home = "home"
    const val RoyalRoad = "royalroad"
    const val NovelBin = "novelbin"
    const val NovelasLigera = "novelasligera"
    const val Settings = "settings"
    const val AuthScreen = "auth"
    const val ChapterList = "chapterList/{novelId}/{novelUrl}"
    const val ChapterContent = "chapterContent/{chapterIndex}"

    fun chapterListRoute(novelId: String, novelUrl: String): String =
        "chapterList/$novelId/${Uri.encode(novelUrl)}"

    fun chapterContentRoute(index: Int): String = "chapterContent/$index"
}

/**
 * Main App Navigation composable which defines navigation graph and routes.
 *
 * Note: function parameters match the ViewModel instances your app provides from MainActivity.
 */
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    chapterViewModel: ChapterViewModel,
    settingsViewModel: SettingsViewModel,
) {
    val navController = rememberNavController()

    // Note: MainActivity already passes settingsViewModel into NovelReaderAppTheme,
    // but it's safe to call theme here too if you prefer (it accepts a nullable viewModel).
    NovelReaderAppTheme(
        settingsViewModel = settingsViewModel)
    {
        Scaffold(topBar = {}) { innerPadding ->
        NavHost(
                navController = navController,
                startDestination = AppRoutes.Home,
                modifier = Modifier.padding(innerPadding)
            ) {

                // Home Screen — matches your HomeScreen(navController, settingsViewModel)
                composable(AppRoutes.Home) {
                    HomeScreen(
                        navController = navController,
                        settingsViewModel = settingsViewModel
                    )
                }

                // Novel list screens (uses the same NovelListScreen signature you provided)
                composable(AppRoutes.RoyalRoad) {
                    NovelListScreen(
                        source = "royalroad",
                        onNovelClick = { novelId, novelUrl, novelTitle, coverUrl ->
                            chapterViewModel.loadChapters(novelId, novelUrl)
                            navController.navigate(AppRoutes.chapterListRoute(novelId, novelUrl))
                        },
                        onNavigateToSettings = { navController.navigate(AppRoutes.Settings) }
                    )
                }

                composable(AppRoutes.NovelBin) {
                    NovelListScreen(
                        source = "novelbin",
                        onNovelClick = { novelId, novelUrl, novelTitle, coverUrl ->
                            chapterViewModel.loadChapters(novelId, novelUrl)
                            navController.navigate(AppRoutes.chapterListRoute(novelId, novelUrl))
                        },
                        onNavigateToSettings = { navController.navigate(AppRoutes.Settings) }
                    )
                }

                composable(AppRoutes.NovelasLigera) {
                    NovelListScreen(
                        source = "novelasligera",
                        onNovelClick = { novelId, novelUrl, novelTitle, coverUrl ->
                            chapterViewModel.loadChapters(novelId, novelUrl)
                            navController.navigate(AppRoutes.chapterListRoute(novelId, novelUrl))
                        },
                        onNavigateToSettings = { navController.navigate(AppRoutes.Settings) }
                    )
                }

                // Chapter List Screen with arguments — create a scoped ViewModel instance and pass it
                composable(
                    route = AppRoutes.ChapterList,
                    arguments = listOf(
                        navArgument("novelId") { type = NavType.StringType },
                        navArgument("novelUrl") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val novelId = backStackEntry.arguments?.getString("novelId") ?: ""
                    val novelUrl = backStackEntry.arguments?.getString("novelUrl") ?: ""

                    // Scoped ViewModel instance (shared with ChapterContent if needed)
                    val localChapterViewModel: ChapterViewModel = viewModel(backStackEntry)

                    LaunchedEffect(novelId, novelUrl) {
                        localChapterViewModel.loadChapters(novelId, novelUrl)
                    }

                    ChapterListScreen(
                        onChapterClick = { chapter ->
                            val chapters = localChapterViewModel.chapters.value
                            val index = chapters.indexOf(chapter)
                            navController.navigate(AppRoutes.chapterContentRoute(index))
                        },
                        onNavigateToSettings = { navController.navigate(AppRoutes.Settings) },
                        modifier = Modifier,
                        viewModel = localChapterViewModel
                    )
                }

                // Chapter Content screen — uses the ChapterViewModel provided by MainActivity for shared state
                composable(
                    route = AppRoutes.ChapterContent,
                    arguments = listOf(navArgument("chapterIndex") { type = NavType.StringType })
                ) { backStackEntry ->
                    val chapterIndex = backStackEntry.arguments
                        ?.getString("chapterIndex")?.toIntOrNull() ?: 0

                    // Use the chapterViewModel passed to AppNavigation (shared instance)
                    val chapters by chapterViewModel.chapters.collectAsState()

                    ChapterHostScreen(
                        chapters = chapters,
                        viewModel = chapterViewModel,
                        startIndex = chapterIndex,
                        onNavigateToSettings = { navController.navigate(AppRoutes.Settings) },
                        settingsViewModel = settingsViewModel
                    )
                }

                // Auth screen (login/register)
                composable(
                    route = AppRoutes.AuthScreen + "?autoNavigate={autoNavigate}",
                    arguments = listOf(navArgument("autoNavigate") {
                        type = NavType.BoolType
                        defaultValue = false
                    })
                ) { backStackEntry ->
                    val autoNavigate = backStackEntry.arguments?.getBoolean("autoNavigate") ?: false
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

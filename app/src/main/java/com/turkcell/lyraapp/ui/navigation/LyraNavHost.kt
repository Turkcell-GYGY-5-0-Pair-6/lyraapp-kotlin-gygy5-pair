package com.turkcell.lyraapp.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.turkcell.lyraapp.ui.auth.login.LoginRoute
import com.turkcell.lyraapp.ui.auth.register.RegisterRoute
import com.turkcell.lyraapp.ui.home.HomeRoute
import com.turkcell.lyraapp.ui.favorites.FavoritesRoute
import com.turkcell.lyraapp.ui.library.LibraryRoute
import com.turkcell.lyraapp.ui.library.create.CreatePlaylistRoute
import com.turkcell.lyraapp.ui.profile.ProfileRoute
import com.turkcell.lyraapp.ui.premium.PremiumRoute
import com.turkcell.lyraapp.ui.checkout.CheckoutRoute
import com.turkcell.lyraapp.ui.search.SearchRoute
import com.turkcell.lyraapp.ui.playlist.PlaylistDetailRoute
import com.turkcell.lyraapp.ui.player.NowPlayingRoute
import com.turkcell.lyraapp.ui.player.MiniPlayerRoute

@Composable
fun LyraNavHost(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
     val backStackEntry by navController.currentBackStackEntryAsState()
     val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (isTopLevelRoute(currentRoute)) {
                LyraBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = navController::navigateToTab,
                )
            }
        },
    ){
        innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = LyraDestination.Login.route,
                modifier = Modifier.padding(innerPadding),
            ){
                composable(LyraDestination.Login.route){
                    LoginRoute(
                        onNavigateToHome = {navController.navigateToHomeClearingAuth()},
                        onNavigateToRegister = {
                            navController.navigate(LyraDestination.Register.route){
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(LyraDestination.Register.route){
                    RegisterRoute(
                        onNavigateToHome = { navController.navigateToHomeClearingAuth()},
                        onNavigateToLogin = {
                            navController.navigate(LyraDestination.Login.route){
                                popUpTo(LyraDestination.Login.route) { inclusive = false}
                                launchSingleTop = true
                            }
                        },
                        onNavigateBack = { navController.popBackStack()},
                    )

                }
                composable(LyraDestination.Home.route) {
                    HomeRoute(
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onNavigateToPlaylistDetail = { playlistId ->
                            navController.navigate("playlist_detail/$playlistId")
                        },
                        onSongClick = { songId ->
                            navController.navigate("now_playing/$songId")
                        },
                    )
                }
                composable(LyraDestination.Search.route) { SearchRoute() }
                composable(LyraDestination.Library.route) {
                    LibraryRoute(
                        onNavigateToCreatePlaylist = {
                            navController.navigate(LyraDestination.CreatePlaylist.route)
                        },
                        onNavigateToPlaylistDetail = { playlistId ->
                            navController.navigate("playlist_detail/$playlistId")
                        }
                    )
                }
                composable(LyraDestination.Favorites.route) {
                    FavoritesRoute(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToNowPlaying = { songId ->
                            navController.navigate("now_playing/$songId")
                        }
                    )
                }
                composable(LyraDestination.CreatePlaylist.route) {
                    CreatePlaylistRoute(
                        onNavigateBack = { navController.popBackStack() },
                        onSaveSuccess = {
                            navController.popBackStack()
                        }
                    )
                }
                composable(LyraDestination.Profile.route) {
                    ProfileRoute(
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onNavigateToLogin = {
                            navController.navigate(LyraDestination.Login.route) {
                                popUpTo(LyraDestination.Home.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToPremium = {
                            navController.navigate(LyraDestination.Premium.route)
                        }
                    )
                }
                composable(LyraDestination.Premium.route) {
                    PremiumRoute(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToCheckout = { planId ->
                            navController.navigate("checkout/$planId")
                        }
                    )
                }
                composable(
                    route = LyraDestination.Checkout.route,
                    arguments = listOf(
                        navArgument("planId") { type = NavType.StringType }
                    )
                ) {
                    CheckoutRoute(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToProfile = {
                            navController.navigate(LyraDestination.Profile.route) {
                                popUpTo(LyraDestination.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(
                    route = LyraDestination.PlaylistDetail.route,
                    arguments = listOf(
                        navArgument("playlistId") { type = NavType.StringType }
                    )
                ) {
                    PlaylistDetailRoute(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToNowPlaying = { songId ->
                            navController.navigate("now_playing/$songId")
                        }
                    )
                }
                composable(
                    route = LyraDestination.NowPlaying.route,
                    arguments = listOf(
                        navArgument("songId") { type = NavType.StringType }
                    )
                ) {
                    NowPlayingRoute(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            if (shouldShowMiniPlayer(currentRoute)) {
                MiniPlayerRoute(
                    onCardClick = { songId ->
                        navController.navigate("now_playing/$songId") {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = innerPadding.calculateBottomPadding())
                )
            }
        }
    }
}

private fun NavHostController.navigateToTab(destination: LyraDestination){
    navigate(destination.route){
        popUpTo(LyraDestination.Home.route) {saveState = true}
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavHostController.navigateToHomeClearingAuth(){
    navigate(LyraDestination.Home.route){
        // Login ekranına kadar geri git ve back stack’i temizle
        popUpTo(LyraDestination.Login.route) {

            // Login ekranını da stack’ten tamamen kaldır
            // (geri tuşuyla Login’e dönülmesin)
            inclusive = true
        }

        // Aynı ekran zaten stack’te varsa tekrar oluşturma
        launchSingleTop = true
    }
}


@Composable
private fun PlaceholderScreen(
    title: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun shouldShowMiniPlayer(route: String?): Boolean {
    if (route == null) return false
    val isAuth = route == LyraDestination.Login.route || route == LyraDestination.Register.route
    val isNowPlaying = route.startsWith("now_playing")
    val isProfile = route == LyraDestination.Profile.route
    val isPlaylistDetail = route == LyraDestination.PlaylistDetail.route || route.startsWith("playlist_detail")
    val isCheckout = route.startsWith("checkout")
    return !isAuth && !isNowPlaying && !isProfile && !isPlaylistDetail && !isCheckout
}
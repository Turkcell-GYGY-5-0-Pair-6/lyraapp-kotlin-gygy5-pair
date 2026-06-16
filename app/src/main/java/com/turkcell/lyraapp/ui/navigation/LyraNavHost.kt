package com.turkcell.lyraapp.ui.navigation

import androidx.compose.foundation.layout.Box
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
import com.turkcell.lyraapp.ui.auth.login.LoginRoute
import com.turkcell.lyraapp.ui.auth.register.RegisterRoute
import com.turkcell.lyraapp.ui.home.HomeRoute
import com.turkcell.lyraapp.ui.profile.ProfileRoute
import com.turkcell.lyraapp.ui.search.SearchRoute

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
            if(isTopLevelRoute(currentRoute)){
                LyraBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = navController::navigateToTab,
                )
            }
        },



    ){
        innerPadding ->
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
            composable(LyraDestination.Home.route) { HomeRoute(isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
            composable(LyraDestination.Search.route) { SearchRoute() }
            composable(LyraDestination.Library.route) { PlaceholderScreen(title = "Kütüphane") }
            composable(LyraDestination.Favorites.route) { PlaceholderScreen(title = "Favoriler") }
            composable(LyraDestination.Profile.route) {
                ProfileRoute(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    onNavigateToLogin = {
                        navController.navigate(LyraDestination.Login.route) {
                            popUpTo(LyraDestination.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
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
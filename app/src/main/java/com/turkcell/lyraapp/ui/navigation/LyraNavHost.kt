package com.turkcell.lyraapp.ui.navigation

import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.turkcell.lyraapp.ui.auth.login.LoginRoute
import com.turkcell.lyraapp.ui.auth.register.RegisterRoute
import com.turkcell.lyraapp.ui.home.HomeRoute
@Composable
fun LyraNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = LyraDestination.Login.route,
        modifier = modifier,
    ) {
        composable(LyraDestination.Login.route) {
            LoginRoute(
                onNavigateToHome = {
                    navController.navigate(LyraDestination.Home.route) {
                        popUpTo(LyraDestination.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(LyraDestination.Register.route) {
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(LyraDestination.Register.route) {
            RegisterRoute(
                onNavigateToHome = {
                    navController.navigate(LyraDestination.Home.route) {
                        popUpTo(LyraDestination.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(LyraDestination.Login.route) {
                        popUpTo(LyraDestination.Login.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(LyraDestination.Home.route) {
            HomeRoute(
                onNavigateToLogin = {
                    navController.navigate(LyraDestination.Login.route) {
                        popUpTo(LyraDestination.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}
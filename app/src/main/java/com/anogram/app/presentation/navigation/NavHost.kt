package com.anogram.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.anogram.app.presentation.ui.screens.BleSettingsScreen
import com.anogram.app.presentation.ui.screens.ChatDetailScreen
import com.anogram.app.presentation.ui.screens.ChatListScreen
import com.anogram.app.presentation.ui.screens.SettingsScreen
import com.anogram.app.presentation.ui.screens.SplashScreen

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object ChatList : Screen("chat_list")
    data object ChatDetail : Screen("chat_detail/{chatId}") {
        fun createRoute(chatId: Long) = "chat_detail/$chatId"
    }
    data object Settings : Screen("settings")
    data object BleSettings : Screen("ble_settings")
}

@Composable
fun AnoGramNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToMain = {
                    navController.navigate(Screen.ChatList.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ChatList.route) {
            ChatListScreen(
                onChatClick = { chatId ->
                    navController.navigate(Screen.ChatDetail.createRoute(chatId))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(
                navArgument("chatId") { type = NavType.LongType }
            )
        ) {
            ChatDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onProfileClick = { },
                onBleSettingsClick = {
                    navController.navigate(Screen.BleSettings.route)
                }
            )
        }

        composable(Screen.BleSettings.route) {
            BleSettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

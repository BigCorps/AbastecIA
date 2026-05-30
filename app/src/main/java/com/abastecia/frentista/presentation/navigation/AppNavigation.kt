package com.abastecia.frentista.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.abastecia.frentista.presentation.ui.config.ConfigScreen
import com.abastecia.frentista.presentation.ui.painel.PainelScreen

sealed class Screen(val route: String) {
    object Config : Screen("config")
    object Painel : Screen("painel")
    object Debug : Screen("debug")
}

@Composable
fun AppNavigation(startDestination: String = Screen.Painel.route) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Config.route) {
            ConfigScreen(
                onSaved = {
                    navController.navigate(Screen.Painel.route) {
                        popUpTo(Screen.Config.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Painel.route) {
            PainelScreen(
                onNavigateToConfig = {
                    navController.navigate(Screen.Config.route)
                },
                onNavigateToDebug = {
                    navController.navigate(Screen.Debug.route)
                }
            )
        }
        composable(Screen.Debug.route) {
            com.abastecia.frentista.presentation.ui.debug.DebugScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

package com.example.railspacing

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.railspacing.ui.navigation.Screen
import com.example.railspacing.ui.screens.about.AboutScreen
import com.example.railspacing.ui.screens.calculator.CalculatorScreen
import com.example.railspacing.ui.screens.calculator.CalculatorViewModel
import com.example.railspacing.ui.screens.history.HistoryScreen
import com.example.railspacing.ui.screens.history.HistoryViewModel
import com.example.railspacing.ui.screens.settings.SettingsScreen
import com.example.railspacing.ui.screens.settings.SettingsViewModel
import com.example.railspacing.ui.screens.splash.SplashScreen
import com.example.railspacing.ui.theme.RailSpacingTheme

@Composable
fun RailSpacingApp() {
    val settingsViewModel: SettingsViewModel = viewModel()
    val calculatorViewModel: CalculatorViewModel = viewModel()
    val historyViewModel: HistoryViewModel = viewModel()

    val settings by settingsViewModel.settingsState.collectAsState()

    RailSpacingTheme(
        appTheme = settings.theme,
        dynamicColor = settings.dynamicColor
    ) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val showBottomBar = currentRoute != Screen.Splash.route

        Scaffold(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        Screen.bottomBarScreens.forEach { screen ->
                            NavigationBarItem(
                                icon = {
                                    screen.icon?.let { icon ->
                                        Icon(icon, contentDescription = screen.title)
                                    }
                                },
                                label = { Text(screen.title) },
                                selected = currentRoute == screen.route,
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                enterTransition = { fadeIn(animationSpec = tween(150)) },
                exitTransition = { fadeOut(animationSpec = tween(150)) },
                popEnterTransition = { fadeIn(animationSpec = tween(150)) },
                popExitTransition = { fadeOut(animationSpec = tween(150)) },
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onSplashFinished = {
                            navController.navigate(Screen.Calculator.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Calculator.route) {
                    CalculatorScreen(viewModel = calculatorViewModel)
                }

                composable(Screen.History.route) {
                    HistoryScreen(
                        viewModel = historyViewModel,
                        onRestoreCalculation = { saved ->
                            calculatorViewModel.restoreCalculation(saved)
                            navController.navigate(Screen.Calculator.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = settingsViewModel)
                }

                composable(Screen.About.route) {
                    AboutScreen()
                }
            }
        }
    }
}

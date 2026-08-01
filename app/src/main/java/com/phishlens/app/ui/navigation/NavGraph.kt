package com.phishlens.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.phishlens.app.ui.screens.HomeScreen
import com.phishlens.app.ui.screens.OnboardingScreen
import com.phishlens.app.ui.screens.QrScannerScreen
import com.phishlens.app.ui.screens.ResultScreen
import com.phishlens.app.ui.screens.SplashScreen
import com.phishlens.app.viewmodel.MainViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { 800 }, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350))
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -800 }, animationSpec = tween(350)) + fadeOut(animationSpec = tween(350))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -800 }, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { 800 }, animationSpec = tween(350)) + fadeOut(animationSpec = tween(350))
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onOnboardingFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                uiState = uiState,
                viewModel = viewModel,
                onNavigateToResult = { navController.navigate(Screen.Result.route) },
                onNavigateToQrScanner = { navController.navigate(Screen.QrScanner.route) }
            )
        }

        composable(Screen.Result.route) {
            ResultScreen(
                uiState = uiState,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.QrScanner.route) {
            QrScannerScreen(
                viewModel = viewModel,
                onNavigateToResult = {
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

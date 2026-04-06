package com.english.flashcard.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.english.flashcard.ui.screens.favorites.FavoritesScreen
import com.english.flashcard.ui.screens.home.HomeScreen
import com.english.flashcard.ui.screens.learning.LearningScreen
import com.english.flashcard.ui.screens.library.LibraryScreen
import com.english.flashcard.ui.screens.me.MeScreen
import com.english.flashcard.ui.screens.settings.SettingsScreen
import com.english.flashcard.ui.screens.completion.CompletionScreen
import com.english.flashcard.ui.screens.onboarding.OnboardingScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen()
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onStartLearning = {
                        navController.navigate(Screen.Learning.createRoute(LearningType.Today))
                    },
                    onNavigateToWrongWords = {
                        navController.navigate(Screen.Learning.createRoute(LearningType.WrongWords))
                    },
                    onNavigateToFavorites = {
                        navController.navigate(Screen.Favorites.route)
                    }
                )
            }
            composable(Screen.Library.route) {
                LibraryScreen()
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onNavigateToLearning = { type ->
                        navController.navigate(Screen.Learning.createRoute(type))
                    }
                )
            }
            composable(Screen.Me.route) {
                MeScreen(
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }
            composable(
                route = Screen.Learning.route,
                arguments = listOf(
                    navArgument("learningType") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val learningType = backStackEntry.arguments?.getString("learningType") ?: "today"
                LearningScreen(
                    learningType = learningType,
                    onClose = { navController.popBackStack() },
                    onComplete = { totalWords, correctCount, accuracy, duration ->
                        navController.navigate(
                            Screen.Completion.createRoute(totalWords, correctCount, accuracy, duration)
                        ) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.Completion.route,
                arguments = listOf(
                    navArgument("totalWords") { type = NavType.IntType },
                    navArgument("correctCount") { type = NavType.IntType },
                    navArgument("accuracy") { type = NavType.FloatType },
                    navArgument("duration") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val totalWords = backStackEntry.arguments?.getInt("totalWords") ?: 0
                val correctCount = backStackEntry.arguments?.getInt("correctCount") ?: 0
                val accuracy = backStackEntry.arguments?.getFloat("accuracy") ?: 0f
                val duration = backStackEntry.arguments?.getLong("duration") ?: 0L
                CompletionScreen(
                    totalWords = totalWords,
                    correctCount = correctCount,
                    accuracy = accuracy,
                    durationSeconds = duration,
                    onBackToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

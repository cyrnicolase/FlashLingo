package com.english.flashcard.ui.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Library : Screen("library")
    data object Favorites : Screen("favorites")
    data object Me : Screen("me")
    
    data object Learning : Screen("learning/{learningType}") {
        fun createRoute(learningType: LearningType): String = "learning/${learningType.value}"
    }
    
    data object Settings : Screen("settings")
    
    data object Completion : Screen("completion/{totalWords}/{correctCount}/{accuracy}/{duration}") {
        fun createRoute(totalWords: Int, correctCount: Int, accuracy: Float, duration: Long): String =
            "completion/$totalWords/$correctCount/$accuracy/$duration"
    }
}

enum class LearningType(val value: String) {
    Today("today"),
    Review("review"),
    WrongWords("wrong"),
    Favorites("favorites")
}

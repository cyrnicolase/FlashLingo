package com.english.flashcard

object AppConstants {
    
    object Animation {
        const val FLASH_CARD_FLIP_DURATION_MS = 300
        const val PAGE_TRANSITION_DURATION_MS = 300
        const val MODAL_APPEAR_DURATION_MS = 250
        const val ANSWER_FEEDBACK_DELAY_MS = 1000
    }
    
    object Learning {
        const val DEFAULT_DAILY_NEW_WORDS = 20
        val DAILY_NEW_WORDS_OPTIONS = listOf(5, 10, 15, 20, 30, 50)
        
        const val CORRECT_STREAK_FOR_MASTERY = 2
        const val WRONG_WORD_REMOVAL_STREAK = 3
        
        const val REVIEW_INTERVAL_AFTER_WRONG_DAYS = 1
        const val REVIEW_INTERVAL_AFTER_FIRST_CORRECT_DAYS = 1
        const val REVIEW_INTERVAL_AFTER_SECOND_CORRECT_DAYS = 3
        const val REVIEW_INTERVAL_DEFAULT_DAYS = 7
        
        const val DEFAULT_REVIEW_LIMIT = 50
    }
    
    object Quiz {
        const val OPTION_COUNT = 4
        const val MIN_SAME_LETTER_OPTIONS = 3
    }
    
    object UI {
        const val FLASH_CARD_HEIGHT_DP = 300
        const val PAGE_MARGIN_DP = 16
        const val CARD_PADDING_DP = 16
        const val BUTTON_HEIGHT_DP = 48
        const val LIST_ITEM_SPACING_DP = 8
        const val MODAL_PADDING_DP = 24
    }
    
    object Colors {
        const val SUCCESS_GREEN_HEX = 0xFF4CAF50
        const val ERROR_RED_HEX = 0xFFF44336
        const val WARNING_ORANGE_HEX = 0xFFFF9800
    }
}

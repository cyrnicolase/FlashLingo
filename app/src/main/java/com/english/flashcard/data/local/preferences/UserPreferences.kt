package com.english.flashcard.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore
    
    companion object {
        private val KEY_IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val KEY_DAILY_NEW_WORDS = intPreferencesKey("daily_new_words")
        private val KEY_ONBOARDING_SHOWN = booleanPreferencesKey("onboarding_shown")
        private val KEY_LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
    }
    
    val isDarkMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_IS_DARK_MODE] ?: false
    }
    
    val dailyNewWords: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_DAILY_NEW_WORDS] ?: 20
    }
    
    val isOnboardingShown: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_SHOWN] ?: false
    }
    
    suspend fun getLastSyncTimestamp(): Long {
        return dataStore.data.first()[KEY_LAST_SYNC_TIMESTAMP] ?: 0L
    }
    
    suspend fun setLastSyncTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_SYNC_TIMESTAMP] = timestamp
        }
    }
    
    suspend fun setDarkMode(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_DARK_MODE] = isDark
        }
    }
    
    suspend fun setDailyNewWords(count: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_DAILY_NEW_WORDS] = count
        }
    }
    
    suspend fun setOnboardingShown(shown: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_SHOWN] = shown
        }
    }
}

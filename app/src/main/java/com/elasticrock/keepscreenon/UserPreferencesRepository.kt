package com.elasticrock.keepscreenon

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private val previousScreenTimeout = intPreferencesKey("previous_screen_timeout")

    suspend fun saveScreenTimeout(screenTimeout: Int) {
        try {
            dataStore.edit { preferences ->
                preferences[previousScreenTimeout] = screenTimeout
            }
        } catch (e: IOException) {
            Log.e("QS","Error writing screen timeout value")
        }
    }

    val readScreenTimeout: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[previousScreenTimeout] ?: 120000
        }
}
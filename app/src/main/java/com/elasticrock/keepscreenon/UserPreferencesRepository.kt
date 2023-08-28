package com.elasticrock.keepscreenon

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private val previousScreenTimeoutKey = intPreferencesKey("previous_screen_timeout")
    private val listenForBatteryLowKey = booleanPreferencesKey("listen_for_battery_low")
    private val listenForScreenOffKey = booleanPreferencesKey("listen_for_screen_off")
    private val isTileAddedKey = booleanPreferencesKey("is_tile_added")

    private val tag = "UserPreferencesRepository"

    suspend fun saveListenForBatteryLow(listenForBatteryLow: Boolean) {
        Log.d(tag, "saveListenForBatteryLow $listenForBatteryLow")
        try {
            dataStore.edit { preferences ->
                preferences[listenForBatteryLowKey] = listenForBatteryLow
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing listenForBatteryLow user preference")
        }
    }

    suspend fun saveListenForScreenOff(listenForScreenOff: Boolean) {
        Log.d(tag, "saveListenForScreenOff $listenForScreenOff")
        try {
            dataStore.edit { preferences ->
                preferences[listenForScreenOffKey] = listenForScreenOff
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing listenForScreenOff user preference")
        }
    }

    suspend fun saveIsTileAdded(isTileAdded: Boolean) {
        try {
            dataStore.edit { preferences ->
                preferences[isTileAddedKey] = isTileAdded
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing isTileAdded")
        }
    }

    suspend fun saveScreenTimeout(screenTimeout: Int) {
        try {
            dataStore.edit { preferences ->
                preferences[previousScreenTimeoutKey] = screenTimeout
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing screen timeout value")
        }
    }

    val readScreenTimeout: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[previousScreenTimeoutKey] ?: 120000
        }

    val readListenForBatteryLow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[listenForBatteryLowKey] ?: false
        }

    val readListenForScreenOff: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[listenForScreenOffKey] ?: false
        }

    val readIsTileAdded: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[isTileAddedKey] ?: false
        }
}
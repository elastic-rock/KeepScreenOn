package com.elasticrock.keepscreenon.data.preferences

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(private val dataStore: DataStore<Preferences>) {

    private val tag = "UserPreferencesRepository"
    private val batteryLowKey = booleanPreferencesKey("listen_for_battery_low")
    private val screenOffKey = booleanPreferencesKey("listen_for_screen_off")
    private val previousScreenTimeoutKey = intPreferencesKey("previous_screen_timeout")
    private val maximumTimeoutKey = intPreferencesKey("maximum_timeout")
    private val isTileAddedKey = booleanPreferencesKey("is_tile_added")
    private val openCountKey = intPreferencesKey("open_count")
    private val isNotificationPermissionDeniedPermanentlyKey = booleanPreferencesKey("is_notification_permission_denied_permanently")

    suspend fun saveListenForBatteryLow(listenForBatteryLow: Boolean) {
        try {
            dataStore.edit { preferences ->
                preferences[batteryLowKey] = listenForBatteryLow
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing listenForBatteryLow property")
        }
    }

    val listenForBatteryLow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[batteryLowKey] ?: false
        }

    suspend fun saveListenForScreenOff(listenForScreenOff: Boolean) {
        try {
            dataStore.edit { preferences ->
                preferences[screenOffKey] = listenForScreenOff
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing listenForScreenOff property")
        }
    }

    val listenForScreenOff: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[screenOffKey] ?: false
        }

    suspend fun savePreviousScreenTimeout(screenTimeout: Int) {
        try {
            dataStore.edit { preferences ->
                preferences[previousScreenTimeoutKey] = screenTimeout
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing previous screen timeout")
        }
    }

    val previousScreenTimeout: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[previousScreenTimeoutKey] ?: 120000
        }

    suspend fun saveMaximumTimeout(maximumTimeout: Int) {
        try {
            dataStore.edit { preferences ->
                preferences[maximumTimeoutKey] = maximumTimeout
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing maximum timeout")
        }
    }

    val maximumTimeout: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[maximumTimeoutKey] ?: Int.MAX_VALUE
        }

    suspend fun saveIsTileAdded(isTileAdded: Boolean) {
        try {
            dataStore.edit { preferences ->
                preferences[isTileAddedKey] = isTileAdded
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing isTileAdded property")
        }
    }

    val isTileAdded: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[isTileAddedKey] ?: false
        }

    val openCount: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[openCountKey] ?: 0
        }

    suspend fun incrementOpenCount() {
        try {
            val previousCount = openCount.first()
            dataStore.edit { preferences ->
                preferences[openCountKey] = previousCount + 1
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing isFirstTimeLaunch property")
        }
    }

    suspend fun saveIsNotificationPermissionDeniedPermanently(isNotificationPermissionDeniedPermanently: Boolean) {
        try {
            dataStore.edit { preferences ->
                preferences[isNotificationPermissionDeniedPermanentlyKey] = isNotificationPermissionDeniedPermanently
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing isNotificationPermissionDeniedPermanently property")
        }
    }

    val isNotificationPermissionDeniedPermanently: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[isNotificationPermissionDeniedPermanentlyKey] ?: false
        }
}
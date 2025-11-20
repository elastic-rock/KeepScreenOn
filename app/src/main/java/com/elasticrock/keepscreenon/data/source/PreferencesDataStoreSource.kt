package com.elasticrock.keepscreenon.data.source

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class PreferencesDataStoreSource @Inject constructor(private val dataStore: DataStore<Preferences>) {
    val batteryLowKey = booleanPreferencesKey("listen_for_battery_low")
    val screenOffKey = booleanPreferencesKey("listen_for_screen_off")
    val previousScreenTimeoutKey = intPreferencesKey("previous_screen_timeout")
    val maximumTimeoutKey = intPreferencesKey("maximum_timeout")
    val isTileAddedKey = booleanPreferencesKey("is_tile_added")
    val openCountKey = intPreferencesKey("open_count")
    val isNotificationPermissionDeniedPermanentlyKey = booleanPreferencesKey("is_notification_permission_denied_permanently")

    private val preferencesKeys: Array<Preferences.Key<*>> = arrayOf(
        batteryLowKey,
        screenOffKey,
        previousScreenTimeoutKey,
        maximumTimeoutKey,
        isTileAddedKey,
        openCountKey,
        isNotificationPermissionDeniedPermanentlyKey
    )

    suspend fun <T> save(preferencesKey: Preferences.Key<T>, value: T) {
        checkPreferencesKey(preferencesKey)
        try {
            dataStore.edit { preferences ->
                preferences[preferencesKey] = value
            }
        } catch (e: IOException) {
            Log.e("PreferencesDataStoreSource","Error saving property " + preferencesKey.name)
        }
    }

    fun <T> getFlow(preferencesKey: Preferences.Key<T>): Flow<T?> {
        checkPreferencesKey(preferencesKey)
        return dataStore.data.map { preferences ->
            preferences[preferencesKey]
        }
    }

    private fun checkPreferencesKey(preferencesKey: Preferences.Key<*>) {
        if (!preferencesKeys.contains(preferencesKey)) {
            throw UnknownPreferencesKeyException("Preferences key " + preferencesKey.name + " is not known")
        }
    }
}
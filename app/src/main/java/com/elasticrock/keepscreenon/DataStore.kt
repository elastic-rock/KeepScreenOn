package com.elasticrock.keepscreenon

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


class DataStore(private val dataStore: DataStore<Preferences>) {

    private val tag = "UserPreferencesRepository"
    private val batteryLowKey = booleanPreferencesKey("listen_for_battery_low")
    private val screenOffKey = booleanPreferencesKey("listen_for_screen_off")
    private val previousScreenTimeoutKey = intPreferencesKey("previous_screen_timeout")

    suspend fun saveListenForBatteryLow(listenForBatteryLow: Boolean) {
        try {
            dataStore.edit { preferences ->
                preferences[batteryLowKey] = listenForBatteryLow
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing listenForBatteryLow property")
        }
    }

    suspend fun readListenForBatteryLow() : Boolean {
        val listenForBatteryLow: Flow<Boolean> = dataStore.data
            .map { preferences ->
                preferences[batteryLowKey] ?: false
            }
        Log.d(tag, "listenForBatteryLow == ${listenForBatteryLow.first()}")
        return listenForBatteryLow.first()
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

    suspend fun readListenForScreenOff() : Boolean {
        val listenForScreenOff: Flow<Boolean> = dataStore.data
            .map { preferences ->
                preferences[screenOffKey] ?: false
            }
        Log.d(tag, "listenForScreenOff == ${listenForScreenOff.first()}")
        return listenForScreenOff.first()
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

    suspend fun readPreviousScreenTimeout() : Int {
        val previousScreenTimeout: Flow<Int> = dataStore.data
            .map { preferences ->
                preferences[previousScreenTimeoutKey] ?: 120000
            }
        return previousScreenTimeout.first()
    }
}
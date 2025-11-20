package com.elasticrock.keepscreenon.data.repository

import com.elasticrock.keepscreenon.data.source.PreferencesDataStoreSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferencesRepository @Inject constructor(private val preferencesDataStoreSource: PreferencesDataStoreSource) {
    suspend fun saveListenForBatteryLow(value: Boolean) {
        preferencesDataStoreSource.save(preferencesDataStoreSource.batteryLowKey, value)
    }

    val listenForBatteryLow = preferencesDataStoreSource.getFlow(preferencesDataStoreSource.batteryLowKey).map { value ->
        value ?: false
    }

    suspend fun saveListenForScreenOff(value: Boolean) {
        preferencesDataStoreSource.save(preferencesDataStoreSource.screenOffKey, value)
    }

    val listenForScreenOff = preferencesDataStoreSource.getFlow(preferencesDataStoreSource.screenOffKey).map { value ->
        value ?: false
    }

    suspend fun saveIsTileAdded(value: Boolean) {
        preferencesDataStoreSource.save(preferencesDataStoreSource.isTileAddedKey, value)
    }

    val isTileAdded = preferencesDataStoreSource.getFlow(preferencesDataStoreSource.isTileAddedKey).map { value ->
        value ?: false
    }

    val openCount: Flow<Int> = preferencesDataStoreSource.getFlow(preferencesDataStoreSource.openCountKey).map { value ->
        value ?: 0
    }

    suspend fun incrementOpenCount() {
        val previousCount = openCount.first()
        preferencesDataStoreSource.save(preferencesDataStoreSource.openCountKey, previousCount + 1)
    }

    suspend fun saveMaximumTimeout(value: Int) {
        preferencesDataStoreSource.save(preferencesDataStoreSource.maximumTimeoutKey, value)
    }

    val maximumTimeout = preferencesDataStoreSource.getFlow(preferencesDataStoreSource.maximumTimeoutKey).map { value ->
        value ?: 600000
    }
}
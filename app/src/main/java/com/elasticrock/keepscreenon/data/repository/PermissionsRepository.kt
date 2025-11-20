package com.elasticrock.keepscreenon.data.repository

import android.content.Context
import android.provider.Settings
import com.elasticrock.keepscreenon.data.source.PreferencesDataStoreSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionsRepository @Inject constructor(private val preferencesDataStoreSource: PreferencesDataStoreSource) {
    private val _canWriteSystemSettings = MutableStateFlow(false)
    val canWriteSystemSettings: StateFlow<Boolean> = _canWriteSystemSettings

    fun updateCanWriteSystemSettings(context: Context) {
        _canWriteSystemSettings.value = Settings.System.canWrite(context)
    }

    private val _isIgnoringBatteryOptimizations = MutableStateFlow(false)
    val isIgnoringBatteryOptimizations: StateFlow<Boolean> = _isIgnoringBatteryOptimizations

    fun updateIsIgnoringBatteryOptimizations(value: Boolean) {
        _isIgnoringBatteryOptimizations.value = value
    }

    private val _isNotificationPermissionGranted = MutableStateFlow(false)
    val isNotificationPermissionGranted: StateFlow<Boolean> = _isNotificationPermissionGranted

    fun updateIsNotificationPermissionGranted(value: Boolean) {
        _isNotificationPermissionGranted.value = value
    }

    suspend fun saveIsNotificationPermissionDeniedPermanently(value: Boolean) {
        preferencesDataStoreSource.save(preferencesDataStoreSource.isNotificationPermissionDeniedPermanentlyKey, value)
    }

    val isNotificationPermissionDeniedPermanently = preferencesDataStoreSource.getFlow(preferencesDataStoreSource.isNotificationPermissionDeniedPermanentlyKey).map { value ->
        value ?: false
    }
}
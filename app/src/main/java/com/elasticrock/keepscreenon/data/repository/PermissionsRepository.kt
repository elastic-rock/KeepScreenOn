package com.elasticrock.keepscreenon.data.repository

import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.elasticrock.keepscreenon.data.source.PreferencesDataStoreSource
import com.elasticrock.keepscreenon.util.notificationPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionsRepository @Inject constructor(
    private val preferencesDataStoreSource: PreferencesDataStoreSource,
    @ApplicationContext context: Context
) {
    val canWriteSystemSettings = flow {
        while (true) {
            emit(Settings.System.canWrite(context))
        }
    }

    val isIgnoringBatteryOptimizations = flow {
        val pm = context.getSystemService(POWER_SERVICE) as PowerManager
        while (true) {
            emit(pm.isIgnoringBatteryOptimizations(context.packageName))
        }
    }

    val isNotificationPermissionGranted = flow {
        while (true) {
            emit(ContextCompat.checkSelfPermission(context, notificationPermission) == PERMISSION_GRANTED)
        }
    }

    suspend fun saveIsNotificationPermissionDeniedPermanently(value: Boolean) {
        preferencesDataStoreSource.save(preferencesDataStoreSource.isNotificationPermissionDeniedPermanentlyKey, value)
    }

    val isNotificationPermissionDeniedPermanently = preferencesDataStoreSource.getFlow(preferencesDataStoreSource.isNotificationPermissionDeniedPermanentlyKey).map { value ->
        value ?: false
    }
}
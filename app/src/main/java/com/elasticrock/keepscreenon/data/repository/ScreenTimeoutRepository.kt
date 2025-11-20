package com.elasticrock.keepscreenon.data.repository

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.elasticrock.keepscreenon.service.BroadcastReceiverService
import com.elasticrock.keepscreenon.data.source.PreferencesDataStoreSource
import com.elasticrock.keepscreenon.util.monitorBatteryLowAction
import com.elasticrock.keepscreenon.util.monitorScreenOffAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenTimeoutRepository @Inject constructor(
    private val preferencesDataStoreSource: PreferencesDataStoreSource,
    private val preferencesRepository: PreferencesRepository
) {
    private suspend fun savePreviousScreenTimeout(value: Int) {
        preferencesDataStoreSource.save(preferencesDataStoreSource.previousScreenTimeoutKey, value)
    }

    val previousScreenTimeout = preferencesDataStoreSource.getFlow(preferencesDataStoreSource.previousScreenTimeoutKey).map { value ->
        value ?: 120000
    }

    private val _currentScreenTimeout = MutableStateFlow(0)
    val currentScreenTimeout: StateFlow<Int> = _currentScreenTimeout

    private fun updateCurrentScreenTimeout(value: Int) {
        _currentScreenTimeout.value = value
    }

    fun updateCurrentScreenTimeout(contentResolver: ContentResolver) {
        updateCurrentScreenTimeout(Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT))
    }

    private fun setScreenTimeout(contentResolver: ContentResolver, screenTimeout: Int) {
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, screenTimeout)
        updateCurrentScreenTimeout(screenTimeout)
    }

    suspend fun enableKeepScreenOn(context: Context) {
        savePreviousScreenTimeout(currentScreenTimeout.first())
        setScreenTimeout(context.contentResolver, preferencesRepository.maximumTimeout.first())
        startBroadcastReceiverService(context)

        // Re-read as it cannot be assumed that the value will actually correspond to the value set earlier, since some devices like Xiaomi tamper with it
        updateCurrentScreenTimeout(context.contentResolver)
    }

    suspend fun disableKeepScreenOn(context: Context) {
        setScreenTimeout(context.contentResolver, previousScreenTimeout.first())
        context.stopService(Intent(context, BroadcastReceiverService::class.java))

        // Re-read as it cannot be assumed that the value will actually correspond to the value set earlier, since some devices like Xiaomi tamper with it
        updateCurrentScreenTimeout(context.contentResolver)
    }

    private suspend fun startBroadcastReceiverService(context: Context) {
        fun startService(intent: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                startService(intent)
            }
        }

        if (preferencesRepository.listenForBatteryLow.first()) {
            val intent = Intent()
                .setClass(context, BroadcastReceiverService::class.java)
                .setAction(monitorBatteryLowAction)
            startService(intent)
        }
        if (preferencesRepository.listenForScreenOff.first()) {
            val intent = Intent()
                .setClass(context, BroadcastReceiverService::class.java)
                .setAction(monitorScreenOffAction)
            startService(intent)
        }
    }
}
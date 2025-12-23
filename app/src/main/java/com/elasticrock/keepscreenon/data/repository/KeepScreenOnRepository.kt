package com.elasticrock.keepscreenon.data.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import com.elasticrock.keepscreenon.data.model.KeepScreenOnState
import com.elasticrock.keepscreenon.service.BroadcastReceiverService
import com.elasticrock.keepscreenon.util.monitorBatteryLowAction
import com.elasticrock.keepscreenon.util.monitorScreenOffAction
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeepScreenOnRepository @Inject constructor(
    private val screenTimeoutRepository: ScreenTimeoutRepository,
    private val preferencesRepository: PreferencesRepository,
    permissionsRepository: PermissionsRepository
) {
    val keepScreenOnState = combine(
        permissionsRepository.canWriteSystemSettings,
        screenTimeoutRepository.currentScreenTimeout,
        preferencesRepository.maximumTimeout,
        screenTimeoutRepository.previousScreenTimeout
    ) { canWriteSystemSettings, currentScreenTimeout, maximumTimeout, previousScreenTimeout ->
        if (!canWriteSystemSettings) {
            KeepScreenOnState.PermissionNotGranted
        } else {
            if (currentScreenTimeout == maximumTimeout) {
                KeepScreenOnState.Enabled(currentTimeout = currentScreenTimeout, previousTimeout = previousScreenTimeout)
            } else {
                KeepScreenOnState.Disabled(currentTimeout = currentScreenTimeout, maximumTimeout = maximumTimeout)
            }
        }
    }

    suspend fun enableKeepScreenOn(context: Context) {
        screenTimeoutRepository.savePreviousScreenTimeout(screenTimeoutRepository.currentScreenTimeout.first())
        screenTimeoutRepository.setScreenTimeout(preferencesRepository.maximumTimeout.first())
        startBroadcastReceiverService(context)
    }

    suspend fun disableKeepScreenOn(context: Context) {
        screenTimeoutRepository.setScreenTimeout(screenTimeoutRepository.previousScreenTimeout.first())
        context.stopService(Intent(context, BroadcastReceiverService::class.java))
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
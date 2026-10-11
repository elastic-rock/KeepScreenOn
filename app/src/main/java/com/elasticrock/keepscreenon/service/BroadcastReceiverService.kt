package com.elasticrock.keepscreenon.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BATTERY_LOW
import android.content.Intent.ACTION_SCREEN_OFF
import android.content.Intent.ACTION_SHUTDOWN
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.elasticrock.keepscreenon.R
import com.elasticrock.keepscreenon.data.repository.KeepScreenOnRepository
import com.elasticrock.keepscreenon.data.repository.PreferencesRepository
import com.elasticrock.keepscreenon.util.NotificationHelper
import com.elasticrock.keepscreenon.util.monitorBatteryLowAction
import com.elasticrock.keepscreenon.util.monitorScreenOffAction
import com.elasticrock.keepscreenon.util.monitorShutdownAction
import com.elasticrock.keepscreenon.util.stopMonitorAction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class BroadcastReceiverService : LifecycleService() {
    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var keepScreenOnRepository: KeepScreenOnRepository

    private val batteryLowReceiver = BatteryLowReceiver()
    private val screenOffReceiver = ScreenOffReceiver()
    private val shutdownReceiver = ShutdownReceiver()

    private var monitorBatteryLow = false
    private var monitorScreenOff = false
    private var monitorShutdown = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == monitorBatteryLowAction) {
            registerBatteryLowReceiver()
        }
        if (intent?.action == monitorScreenOffAction) {
            registerScreenOffReceiver()
        }
        if (intent?.action == monitorShutdownAction) {
            registerShutdownReceiver()
        }
        if (intent?.action == stopMonitorAction) {
            restoreScreenTimeout()
        }

        val notification = NotificationHelper().buildKeepScreenOnEnabledNotification(this)
        startForeground(1, notification)

        return super.onStartCommand(intent, flags, startId)
    }

    private inner class BatteryLowReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_BATTERY_LOW) {
                restoreScreenTimeout()
            }
        }
    }

    private inner class ScreenOffReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_SCREEN_OFF) {
                restoreScreenTimeout()
            }
        }
    }

    private inner class ShutdownReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_SHUTDOWN) {
                restoreScreenTimeout()
            }
        }
    }

    private fun registerBatteryLowReceiver() {
        ContextCompat.registerReceiver(this, batteryLowReceiver, IntentFilter(ACTION_BATTERY_LOW), ContextCompat.RECEIVER_NOT_EXPORTED)
        monitorBatteryLow = true
    }

    private fun registerScreenOffReceiver() {
        ContextCompat.registerReceiver(this, screenOffReceiver, IntentFilter(ACTION_SCREEN_OFF), ContextCompat.RECEIVER_NOT_EXPORTED)
        monitorScreenOff = true
    }

    private fun registerShutdownReceiver() {
        ContextCompat.registerReceiver(this, shutdownReceiver, IntentFilter(ACTION_SHUTDOWN), ContextCompat.RECEIVER_NOT_EXPORTED)
        monitorShutdown = true
    }

    private fun restoreScreenTimeout() {
        runBlocking {
            keepScreenOnRepository.disableKeepScreenOn()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (monitorBatteryLow) {
            unregisterReceiver(batteryLowReceiver)
        }
        if (monitorScreenOff) {
            unregisterReceiver(screenOffReceiver)
        }
        if (monitorShutdown) {
            unregisterReceiver(shutdownReceiver)
        }
    }
}
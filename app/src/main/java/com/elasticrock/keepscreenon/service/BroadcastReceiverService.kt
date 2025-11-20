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
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.elasticrock.keepscreenon.R
import com.elasticrock.keepscreenon.data.repository.PreferencesRepository
import com.elasticrock.keepscreenon.data.repository.ScreenTimeoutRepository
import com.elasticrock.keepscreenon.util.monitorBatteryLowAction
import com.elasticrock.keepscreenon.util.monitorScreenOffAction
import com.elasticrock.keepscreenon.util.stopMonitorAcion
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class BroadcastReceiverService : LifecycleService() {
    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var screenTimeoutRepository: ScreenTimeoutRepository

    private val batteryLowReceiver = BatteryLowReceiver()
    private val screenOffReceiver = ScreenOffReceiver()

    private var monitorBatteryLow = false
    private var monitorScreenOff = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == monitorBatteryLowAction) {
            registerBatteryLowReceiver()
        }
        if (intent?.action == monitorScreenOffAction) {
            registerScreenOffReceiver()
        }
        if (intent?.action == stopMonitorAcion) {
            restoreScreenTimeout()
        }

        val importance = NotificationManager.IMPORTANCE_LOW
        val name = if (monitorBatteryLow && monitorScreenOff)
        {
            getString(R.string.listening_for_battery_low_and_screen_off_actions)
        } else if (monitorBatteryLow)
        {
            getString(R.string.listening_for_battery_low_action)
        } else
        {
            getString(R.string.listening_for_screen_off_action)
        }
        val stopPendingIntent = Intent(this, BroadcastReceiverService::class.java)
            .apply { action = stopMonitorAcion }
            .let { PendingIntent.getService(this, 1, it, FLAG_IMMUTABLE) }
        val action = Notification.Action.Builder(Icon.createWithResource(this, R.drawable.outline_close_24), getString(
            R.string.stop), stopPendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            //Delete old notification channels
            notificationManager.deleteNotificationChannel("battery_low_and_screen_off_monitor")
            notificationManager.deleteNotificationChannel("battery_low_monitor")
            notificationManager.deleteNotificationChannel("screen_off_monitor")
            notificationManager.deleteNotificationChannelGroup("background_service")

            val channelId = "background_service"
            val channelName = getString(R.string.background_service)
            val mChannel = NotificationChannel(channelId, channelName, importance)

            notificationManager.createNotificationChannel(mChannel)
            val notification: Notification = Notification.Builder(this, channelId)
                .setContentTitle(name)
                .setSmallIcon(R.drawable.outline_lock_clock_qs)
                .addAction(action)
                .build()
            startForeground(1, notification)
        } else {
            @Suppress("DEPRECATION") val notification: Notification = Notification.Builder(this)
                .setContentTitle(name)
                .setSmallIcon(R.drawable.outline_lock_clock_qs)
                .addAction(action)
                .build()
            startForeground(1, notification)
        }

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

    private fun registerBatteryLowReceiver() {
        ContextCompat.registerReceiver(this, batteryLowReceiver, IntentFilter(ACTION_BATTERY_LOW), ContextCompat.RECEIVER_EXPORTED)
        monitorBatteryLow = true
    }

    private fun registerScreenOffReceiver() {
        ContextCompat.registerReceiver(this, screenOffReceiver, IntentFilter(ACTION_SCREEN_OFF), ContextCompat.RECEIVER_EXPORTED)
        monitorScreenOff = true
    }

    private fun restoreScreenTimeout() {
        runBlocking {
            screenTimeoutRepository.disableKeepScreenOn(this@BroadcastReceiverService)
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
    }
}
package com.elasticrock.keepscreenon

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
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class BroadcastReceiverService : LifecycleService() {

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
            stopForegroundService()
        }

        val importance = NotificationManager.IMPORTANCE_LOW
        val name = getString(R.string.listening_for_battery_low_and_or_screen_off_actions)
        val stopPendingIntent = Intent(this, BroadcastReceiverService::class.java)
            .apply { action = stopMonitorAcion }
            .let { PendingIntent.getService(this, 1, it, FLAG_IMMUTABLE) }

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
                .addAction(R.drawable.outline_lock_clock_qs, getString(R.string.stop), stopPendingIntent)
                .build()
            startForeground(1, notification)
        } else {
            @Suppress("DEPRECATION") val notification: Notification = Notification.Builder(this)
                .setContentTitle(name)
                .setSmallIcon(R.drawable.outline_lock_clock_qs)
                .build()
            startForeground(1, notification)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private inner class BatteryLowReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_BATTERY_LOW) {
                restoreScreenTimeout()
                stopForegroundService()
            }
        }
    }

    private inner class ScreenOffReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_SCREEN_OFF) {
                restoreScreenTimeout()
                stopForegroundService()
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
            val previousScreenTimeout = DataStoreRepository(dataStore).readPreviousScreenTimeout()
            launch { CommonUtils().setScreenTimeout(contentResolver, previousScreenTimeout) }
        }
    }

    private fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
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
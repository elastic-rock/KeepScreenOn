package com.elasticrock.keepscreenon

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
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

    private val tag = "BroadcastReceiverService"

    private var monitorBatteryLow = false
    private var monitorScreenOff = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d(tag,"onStartCommand")

        if (intent?.action == "com.elasticrock.keepscreenon.ACTION_MONITOR_BATTERY_LOW") {
            Log.d(tag, "intent.action == com.elasticrock.keepscreenon.ACTION_MONITOR_BATTERY_LOW")
            registerBatteryLowReceiver()
        }
        if (intent?.action == "com.elasticrock.keepscreenon.ACTION_MONITOR_SCREEN_OFF") {
            Log.d(tag, "intent.action == com.elasticrock.keepscreenon.ACTION_MONITOR_SCREEN_OFF")
            registerScreenOffReceiver()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val groupId = "background_service"
            val groupName = getString(R.string.background_service)
            notificationManager.createNotificationChannelGroup(NotificationChannelGroup(groupId, groupName))
            notificationManager.deleteNotificationChannel("foreground_service") // Delete old notification channel
            if (monitorBatteryLow && monitorScreenOff) {
                val name = getString(R.string.listening_for_battery_low_and_screen_off_actions)
                val mChannel = NotificationChannel("battery_low_and_screen_off_monitor", name, importance)
                mChannel.group = groupId
                notificationManager.createNotificationChannel(mChannel)
                val notification: Notification = Notification.Builder(this, "battery_low_and_screen_off_monitor")
                    .setContentTitle(name)
                    .setSmallIcon(R.drawable.outline_lock_clock_qs)
                    .build()
                startForeground(1, notification)
            } else if (monitorBatteryLow){
                val name = getString(R.string.listening_for_battery_low_action)
                val mChannel = NotificationChannel("battery_low_monitor", name, importance)
                mChannel.group = groupId
                notificationManager.createNotificationChannel(mChannel)
                val notification: Notification = Notification.Builder(this, "battery_low_monitor")
                    .setContentTitle(name)
                    .setSmallIcon(R.drawable.outline_lock_clock_qs)
                    .build()
                startForeground(1, notification)
            } else if (monitorScreenOff) {
                val name = getString(R.string.listening_for_screen_off_action)
                val mChannel = NotificationChannel("screen_off_monitor", name, importance)
                mChannel.group = groupId
                notificationManager.createNotificationChannel(mChannel)
                val notification: Notification = Notification.Builder(this, "screen_off_monitor")
                    .setContentTitle(name)
                    .setSmallIcon(R.drawable.outline_lock_clock_qs)
                    .build()
                startForeground(1, notification)

            }
        } else {
            @Suppress("DEPRECATION")
            if (monitorBatteryLow && monitorScreenOff) {
                val name = getString(R.string.listening_for_battery_low_and_screen_off_actions)
                val notification: Notification = Notification.Builder(this)
                    .setContentTitle(name)
                    .setSmallIcon(R.drawable.outline_lock_clock_qs)
                    .build()
                startForeground(1, notification)
            } else if (monitorBatteryLow){
                val name = getString(R.string.listening_for_battery_low_action)
                val notification: Notification = Notification.Builder(this)
                    .setContentTitle(name)
                    .setSmallIcon(R.drawable.outline_lock_clock_qs)
                    .build()
                startForeground(1, notification)
            } else if (monitorScreenOff) {
                val name = getString(R.string.listening_for_screen_off_action)
                val notification: Notification = Notification.Builder(this)
                    .setContentTitle(name)
                    .setSmallIcon(R.drawable.outline_lock_clock_qs)
                    .build()
                startForeground(1, notification)

            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private inner class BatteryLowReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_BATTERY_LOW) {
                Log.d(tag,"ACTION_BATTERY_LOW")
                restoreScreenTimeout()
                stopForegroundService()
            }
        }
    }

    private inner class ScreenOffReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_SCREEN_OFF) {
                Log.d(tag,"ACTION_SCREEN_OFF")
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
            val previousScreenTimeout = DataStore(dataStore).readPreviousScreenTimeout()
            launch { CommonUtils().setScreenTimeout(contentResolver, previousScreenTimeout) }
        }    }

    private fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag,"onDestroy")
        if (monitorBatteryLow) {
            unregisterReceiver(batteryLowReceiver)
        }
        if (monitorScreenOff) {
            unregisterReceiver(screenOffReceiver)
        }
    }
}
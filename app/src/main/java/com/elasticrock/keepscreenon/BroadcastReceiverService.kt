package com.elasticrock.keepscreenon

import android.app.Notification
import android.app.NotificationChannel
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

class BroadcastReceiverService : LifecycleService() {

    private val batteryLowReceiver = BatteryLowReceiver()
    private val screenOffReceiver = ScreenOffReceiver()

    private val tag = "BroadcastReceiverService"

    private var monitorBatteryLow = false
    private var monitorScreenOff = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d(tag,"onStartCommand")

        if (intent?.action == "com.elasticrock.keepscreenon.ACTION_MONITOR_BATTERY_LOW_AND_SCREEN_OFF") {
            Log.d(tag, "intent.action == com.elasticrock.keepscreenon.ACTION_MONITOR_BATTERY_LOW_AND_SCREEN_OFF")
            monitorBatteryLow = true
            monitorScreenOff = true
            ContextCompat.registerReceiver(this, batteryLowReceiver, IntentFilter(ACTION_BATTERY_LOW), ContextCompat.RECEIVER_EXPORTED)
            ContextCompat.registerReceiver(this, screenOffReceiver, IntentFilter(ACTION_SCREEN_OFF), ContextCompat.RECEIVER_EXPORTED)
        }
        if (intent?.action == "com.elasticrock.keepscreenon.ACTION_MONITOR_BATTERY_LOW") {
            Log.d(tag, "intent.action == com.elasticrock.keepscreenon.ACTION_MONITOR_BATTERY_LOW")
            monitorBatteryLow = true
            ContextCompat.registerReceiver(this, batteryLowReceiver, IntentFilter(ACTION_BATTERY_LOW), ContextCompat.RECEIVER_EXPORTED)
        }
        if (intent?.action == "com.elasticrock.keepscreenon.ACTION_MONITOR_SCREEN_OFF") {
            Log.d(tag, "intent.action == com.elasticrock.keepscreenon.ACTION_MONITOR_SCREEN_OFF")
            monitorScreenOff = true
            ContextCompat.registerReceiver(this, screenOffReceiver, IntentFilter(ACTION_SCREEN_OFF), ContextCompat.RECEIVER_EXPORTED)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.foreground_service)
            val descriptionText = getString(R.string.foreground_service_channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel = NotificationChannel("foreground_service", name, importance)
            mChannel.description = descriptionText
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)

            val notification: Notification = Notification.Builder(this, "foreground_service")
                .setContentTitle(
                    if (monitorBatteryLow && monitorScreenOff) {
                        getString(R.string.listening_for_battery_low_and_screen_off_actions)
                    } else if (monitorBatteryLow){
                        getString(R.string.listening_for_battery_low_action)
                    } else {
                        getString(R.string.listening_for_screen_off_action)
                    }
                )
                .setSmallIcon(R.drawable.outline_lock_clock_qs)
                .build()
            startForeground(1, notification)
        } else {
            @Suppress("DEPRECATION") val notification: Notification = Notification.Builder(this)
                .setContentTitle(getString(R.string.listening_for_battery_low_action))
                .setSmallIcon(R.drawable.outline_lock_clock_qs)
                .build()
            startForeground(1, notification)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private inner class BatteryLowReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_BATTERY_LOW) {
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

    private fun restoreScreenTimeout() {
        ScreenTimeoutUtils().restoreScreenTimeout(applicationContext, contentResolver)
    }

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
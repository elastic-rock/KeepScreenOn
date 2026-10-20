package com.elasticrock.keepscreenon.util

import android.app.Notification
import android.app.Notification.CATEGORY_SERVICE
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import com.elasticrock.keepscreenon.R
import com.elasticrock.keepscreenon.service.BroadcastReceiverService

class NotificationHelper {

    fun buildKeepScreenOnEnabledNotification(context: Context): Notification {
        val importance = NotificationManager.IMPORTANCE_LOW
        val name = context.getString(R.string.keep_screen_on_enabled)
        val stopPendingIntent = Intent(context, BroadcastReceiverService::class.java)
            .apply { action = stopMonitorAction }
            .let { PendingIntent.getService(context, 1, it, FLAG_IMMUTABLE) }
        val action = Notification.Action.Builder(Icon.createWithResource(context, R.drawable.outline_close_24), context.getString(
            R.string.disable), stopPendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val channelId = "keep_screen_on_enabled"
            val channelName = context.getString(R.string.keep_screen_on_enabled)
            val mChannel = NotificationChannel(channelId, channelName, importance)

            notificationManager.createNotificationChannel(mChannel)
            return Notification.Builder(context, channelId)
                .setContentTitle(name)
                .setSmallIcon(R.drawable.outline_lock_clock_qs)
                .setCategory(CATEGORY_SERVICE)
                .addAction(action)
                .build()
        } else {
            @Suppress("DEPRECATION") return Notification.Builder(context)
                .setContentTitle(name)
                .setSmallIcon(R.drawable.outline_lock_clock_qs)
                .setCategory(CATEGORY_SERVICE)
                .addAction(action)
                .build()
        }
    }

    fun deleteOldNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.deleteNotificationChannel("battery_low_and_screen_off_monitor")
            notificationManager.deleteNotificationChannel("battery_low_monitor")
            notificationManager.deleteNotificationChannel("screen_off_monitor")
            notificationManager.deleteNotificationChannel("background_service")
            notificationManager.deleteNotificationChannelGroup("background_service")
        }
    }
}
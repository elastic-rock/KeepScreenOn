package com.elasticrock.keepscreenon

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BATTERY_LOW
import android.content.Intent.ACTION_SCREEN_OFF
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService

class BroadcastReceiverService : LifecycleService() {

    override fun onCreate() {
        super.onCreate()
        Log.d("BroadcastReceiverService","onCreate")
        ContextCompat.registerReceiver(applicationContext, BatteryLowReceiver(), IntentFilter(ACTION_BATTERY_LOW), ContextCompat.RECEIVER_EXPORTED)
        ContextCompat.registerReceiver(applicationContext, ScreenOffReceiver(), IntentFilter(ACTION_SCREEN_OFF), ContextCompat.RECEIVER_EXPORTED)

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(BatteryLowReceiver())
        unregisterReceiver(ScreenOffReceiver())
    }
    private inner class BatteryLowReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_BATTERY_LOW) {
                Log.d("BroadcastReceiverService","ACTION_BATTERY_LOW")
            }
        }
    }

    private inner class ScreenOffReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == ACTION_SCREEN_OFF) {
                Log.d("BroadcastReceiverService","ACTION_SCREEN_OFF")
            }
        }
    }
}
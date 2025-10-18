package com.elasticrock.keepscreenon.util

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.elasticrock.keepscreenon.BroadcastReceiverService
import com.elasticrock.keepscreenon.data.preferences.PreferencesRepository
import com.elasticrock.keepscreenon.di.dataStore
import kotlinx.coroutines.flow.first

class CommonUtils {
    
    fun readScreenTimeout(contentResolver: ContentResolver) : Int {
        return Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
    }

    fun setScreenTimeout(contentResolver: ContentResolver, screenTimeout: Int) {
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, screenTimeout)
    }

    suspend fun startBroadcastReceiverService(context: Context) {
        val listenForBatteryLow = PreferencesRepository(context.dataStore).listenForBatteryLow.first()
        val listenForScreenOff = PreferencesRepository(context.dataStore).listenForScreenOff.first()

        fun startService(intent: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                startService(intent)
            }
        }

        if (listenForBatteryLow) {
            val intent = Intent()
                .setClass(context, BroadcastReceiverService::class.java)
                .setAction(monitorBatteryLowAction)
            startService(intent)
        }
        if (listenForScreenOff) {
            val intent = Intent()
                .setClass(context, BroadcastReceiverService::class.java)
                .setAction(monitorScreenOffAction)
            startService(intent)
        }
    }
}
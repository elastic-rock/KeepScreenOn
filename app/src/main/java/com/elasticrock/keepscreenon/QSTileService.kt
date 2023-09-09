package com.elasticrock.keepscreenon

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class QSTileService : TileService() {

    private val tag = "QSTileService"

    override fun onTileAdded() {
        super.onTileAdded()
        Log.d(tag,"Lifecycle: onTileAdded")
        isTileAddedState.value = true
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        Log.d(tag,"Lifecycle: onTileRemoved")
        isTileAddedState.value = false
    }
    override fun onStartListening() {
        super.onStartListening()
        Log.d(tag,"Lifecycle: onStartListening")
        val screenTimeout = ScreenTimeoutUtils().readScreenTimeout(contentResolver)
        qsTile.label = getString(R.string.keep_screen_on)
        if (!Settings.System.canWrite(applicationContext)) {
            qsTile.state = Tile.STATE_INACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                qsTile.subtitle = getString(R.string.grant_permission)
            }
            qsTile.updateTile()
        } else if (screenTimeout == 2147483647) {
            activeState()
        } else {
            inactiveState(screenTimeout)
        }
        isTileAddedState.value = true
    }


    override fun onClick() {
        super.onClick()
        Log.d(tag,"onClick")
        val screenTimeout = ScreenTimeoutUtils().readScreenTimeout(contentResolver)
        if (!Settings.System.canWrite(applicationContext)) {
            val grantPermissionIntent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startActivityAndCollapse(PendingIntent.getActivity(applicationContext, 1, grantPermissionIntent, FLAG_IMMUTABLE + FLAG_UPDATE_CURRENT))
            } else {
                grantPermissionIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                grantPermissionIntent.addFlags(FLAG_ACTIVITY_SINGLE_TOP)
                @Suppress("DEPRECATION", "StartActivityAndCollapseDeprecated")
                startActivityAndCollapse(grantPermissionIntent)
            }
            qsTile.updateTile()
        } else if (screenTimeout == 2147483647) {
            val previousScreenTimeout = ScreenTimeoutUtils().readPreviousScreenTimeout(applicationContext)
            runBlocking {
                launch { ScreenTimeoutUtils().setScreenTimeout(contentResolver, previousScreenTimeout) }
                launch { inactiveState(previousScreenTimeout) }
            }
            stopService(Intent(this, BroadcastReceiverService::class.java))
        } else {
            runBlocking {
                launch { activeState() }
                launch { ScreenTimeoutUtils().disableScreenTimeout(applicationContext, contentResolver) }
            }
            val listenForBatteryLow = UserPreferencesRepository().readListenForBatteryLow(applicationContext)
            val listenForScreenOff = UserPreferencesRepository().readListenForScreenOff(applicationContext)
            if (listenForBatteryLow && listenForScreenOff) {
                Log.d(tag,"listenForBatteryLow && listenForScreenOff")
                val intent = Intent()
                    .setClass(this, BroadcastReceiverService::class.java)
                    .setAction("com.elasticrock.keepscreenon.ACTION_MONITOR_BATTERY_LOW_AND_SCREEN_OFF")
                startBroadcastReceiverService(intent)
            } else if (listenForBatteryLow) {
                Log.d(tag,"listenForBatteryLow")
                val intent = Intent()
                    .setClass(this, BroadcastReceiverService::class.java)
                    .setAction("com.elasticrock.keepscreenon.ACTION_MONITOR_BATTERY_LOW")
                startBroadcastReceiverService(intent)
            } else if (listenForScreenOff) {
                Log.d(tag,"listenForScreenOff")
                val intent = Intent()
                    .setClass(this, BroadcastReceiverService::class.java)
                    .setAction("com.elasticrock.keepscreenon.ACTION_MONITOR_SCREEN_OFF")
                startBroadcastReceiverService(intent)
            }
        }
    }

    private fun inactiveState(screenTimeout: Int) {
        qsTile.state = Tile.STATE_INACTIVE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (screenTimeout < 60000) {
                qsTile.subtitle = resources.getQuantityString(R.plurals.second, screenTimeout/1000, screenTimeout/1000)
            } else {
                qsTile.subtitle = resources.getQuantityString(R.plurals.minute, screenTimeout/60000, screenTimeout/60000)
            }
        }
        qsTile.updateTile()
    }

    private fun activeState() {
        qsTile.state = Tile.STATE_ACTIVE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            qsTile.subtitle = getString(R.string.always)
        }
        qsTile.updateTile()
    }

    private fun startBroadcastReceiverService(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}

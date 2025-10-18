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
import androidx.core.net.toUri
import com.elasticrock.keepscreenon.data.preferences.PreferencesRepository
import com.elasticrock.keepscreenon.di.dataStore
import com.elasticrock.keepscreenon.util.CommonUtils
import com.elasticrock.keepscreenon.util.monitorBatteryLowAction
import com.elasticrock.keepscreenon.util.monitorScreenOffAction
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class QSTileService : TileService() {

    override fun onTileAdded() {
        super.onTileAdded()
        runBlocking { PreferencesRepository(dataStore).saveIsTileAdded(true) }
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        runBlocking { PreferencesRepository(dataStore).saveIsTileAdded(false) }
    }

    override fun onStartListening() {
        super.onStartListening()
        runBlocking {
            val canWrite = async { Settings.System.canWrite(applicationContext) }
            val screenTimeout = async { CommonUtils().readScreenTimeout(contentResolver) }
            val maxTimeout = async { PreferencesRepository(dataStore).maximumTimeout.first() }

            qsTile.label = getString(R.string.keep_screen_on)
            if (!canWrite.await()) {
                qsTile.state = Tile.STATE_INACTIVE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    qsTile.subtitle = getString(R.string.grant_permission)
                }
                qsTile.updateTile()
            } else if (screenTimeout.await() == maxTimeout.await()) {
                activeState(maxTimeout.await())
            } else {
                inactiveState(screenTimeout.await())
            }

            PreferencesRepository(dataStore).saveIsTileAdded(true)
            screenTimeoutState.value = screenTimeout.await()
        }
    }

    override fun onClick() {
        super.onClick()
        val context = this
        runBlocking {
            val canWrite = async { Settings.System.canWrite(applicationContext) }
            val screenTimeout = async { CommonUtils().readScreenTimeout(contentResolver) }
            val maximumTimeout = async { PreferencesRepository(dataStore).maximumTimeout.first() }
            val previousScreenTimeout = async { PreferencesRepository(dataStore).previousScreenTimeout.first() }

            if (!canWrite.await()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startActivityAndCollapse(PendingIntent.getActivity(applicationContext, 1, Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply { data = ("package:" + applicationContext.packageName).toUri() }, FLAG_IMMUTABLE + FLAG_UPDATE_CURRENT))
                } else {
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply { data = ("package:" + applicationContext.packageName).toUri() }
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(FLAG_ACTIVITY_SINGLE_TOP)
                    @Suppress("DEPRECATION", "StartActivityAndCollapseDeprecated")
                    startActivityAndCollapse(intent)
                }
                qsTile.updateTile()
            } else if (screenTimeout.await() == maximumTimeout.await()) {
                inactiveState(previousScreenTimeout.await())
                CommonUtils().setScreenTimeout(contentResolver, previousScreenTimeout.await())
                stopService(Intent(context, BroadcastReceiverService::class.java))
            } else {
                activeState(maximumTimeout.await())
                CommonUtils().setScreenTimeout(contentResolver, maximumTimeout.await())
                PreferencesRepository(dataStore).savePreviousScreenTimeout(screenTimeout.await())
                CommonUtils().startBroadcastReceiverService(context)
            }

            PreferencesRepository(dataStore).saveIsTileAdded(true)
            // Re-read as it cannot be assumed that the value will actually correspond to the value set earlier, since some devices like Xiaomi tamper with it
            screenTimeoutState.value = CommonUtils().readScreenTimeout(contentResolver)
        }
    }

    private fun inactiveState(screenTimeout: Int) {
        qsTile.state = Tile.STATE_INACTIVE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (screenTimeout < 60000) {
                qsTile.subtitle = resources.getQuantityString(R.plurals.second, screenTimeout/1000, screenTimeout/1000)
            } else if (screenTimeout < 3600000) {
                qsTile.subtitle = resources.getQuantityString(R.plurals.minute, screenTimeout/60000, screenTimeout/60000)
            } else if (screenTimeout < 86400000) {
                qsTile.subtitle = resources.getQuantityString(R.plurals.hour, screenTimeout/3600000, screenTimeout/3600000)
            } else {
                qsTile.subtitle = resources.getQuantityString(R.plurals.day, screenTimeout/86400000, screenTimeout/86400000)
            }
        }
        qsTile.updateTile()
    }

    private fun activeState(screenTimeout: Int) {
        qsTile.state = Tile.STATE_ACTIVE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (screenTimeout < 60000) {
                qsTile.subtitle = resources.getQuantityString(R.plurals.second, screenTimeout/1000, screenTimeout/1000)
            } else if (screenTimeout < 3600000) {
                qsTile.subtitle = resources.getQuantityString(R.plurals.minute, screenTimeout/60000, screenTimeout/60000)
            } else if (screenTimeout < 86400000) {
                qsTile.subtitle = resources.getQuantityString(R.plurals.hour, screenTimeout/3600000, screenTimeout/3600000)
            } else if (screenTimeout == Int.MAX_VALUE) {
                qsTile.subtitle = getString(R.string.always_on)
            } else {
                qsTile.subtitle = resources.getQuantityString(R.plurals.day, screenTimeout/86400000, screenTimeout/86400000)
            }
        }
        qsTile.updateTile()
    }
}

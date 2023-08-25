package com.elasticrock.keepscreenon

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "screen_timeout")

class QSTileService : TileService() {

    private val previousScreenTimeout = intPreferencesKey("previous_screen_timeout")

    override fun onStartListening() {
        super.onStartListening()
        qsTile.label = getString(R.string.keep_screen_on)
        if (!Settings.System.canWrite(applicationContext)) {
            qsTile.state = Tile.STATE_INACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                qsTile.subtitle = getString(R.string.grant_permission)
            }
        } else if (Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT) == 2147483647) {
            enabled()
        } else {
            disabled()
        }
        qsTile.updateTile()
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        if (!Settings.System.canWrite(applicationContext)) {
            val grantPermissionIntent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startActivityAndCollapse(PendingIntent.getActivity(applicationContext, 1, grantPermissionIntent, FLAG_IMMUTABLE + FLAG_UPDATE_CURRENT))
            } else {
                grantPermissionIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                grantPermissionIntent.addFlags(FLAG_ACTIVITY_SINGLE_TOP)
                @Suppress("DEPRECATION")
                startActivityAndCollapse(grantPermissionIntent)
            }
        } else if (Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT) == 2147483647) {
            restoreScreenTimeout()
            disabled()
        } else {
            setScreenTimeoutToNever()
            enabled()
        }
        qsTile.updateTile()
    }

    private fun disabled() {
        qsTile.state = Tile.STATE_INACTIVE
        val screenTimeout = Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (screenTimeout < 60000) {
                qsTile.subtitle = resources.getQuantityString(R.plurals.second, screenTimeout/1000, screenTimeout/1000)
            } else {
                qsTile.subtitle = resources.getQuantityString(R.plurals.minute, screenTimeout/60000, screenTimeout/60000)
            }
        }
    }

    private fun enabled() {
        qsTile.state = Tile.STATE_ACTIVE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            qsTile.subtitle = getString(R.string.always)
        }
    }

    private fun setScreenTimeoutToNever() {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[previousScreenTimeout] = Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
            }
        }
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 2147483647)
    }

    private fun restoreScreenTimeout() {
        runBlocking {
            val restoredScreenTimeout: Flow<Int> = dataStore.data
                .map { preferences ->
                    preferences[previousScreenTimeout] ?: 120000
                }
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, restoredScreenTimeout.first())
        }
    }
}

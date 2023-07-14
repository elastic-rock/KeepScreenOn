package com.elasticrock.keepscreenon

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "screen_timeout")

class QSTileService : TileService() {

    object PreferencesKeys {
        val PREVIOUS_SCREEN_TIMEOUT = intPreferencesKey("previous_screen_timeout")
    }

    private lateinit var dataStore: DataStore<Preferences>

    private val screenTimeoutPreferenceFlow by lazy { dataStore.data.map { preferences -> preferences[PreferencesKeys.PREVIOUS_SCREEN_TIMEOUT] ?: 120000 } }

    override fun onCreate() {
        super.onCreate()
        dataStore = applicationContext.dataStore
    }

    private var isPermissionGranted = false
    private var screenTimeout = 0

    override fun onStartListening() {
        super.onStartListening()
        isPermissionGranted = false
        qsTile.label = getString(R.string.keep_screen_on)
        if (Settings.System.canWrite(applicationContext)) {
            isPermissionGranted = true
            screenTimeout = Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
        }
        if (!isPermissionGranted) {
            qsTile.state = Tile.STATE_INACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                qsTile.subtitle = getString(R.string.grant_permission)
            }
        } else if (screenTimeout == 2147483647) {
            enabled()
        } else {
            disabled()
        }
        qsTile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        if (!isPermissionGranted) {
            val grantPermission = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            grantPermission.addFlags(FLAG_ACTIVITY_NEW_TASK)
            grantPermission.addFlags(FLAG_ACTIVITY_SINGLE_TOP)
            startActivityAndCollapse(grantPermission)
        } else if (qsTile.state == Tile.STATE_INACTIVE) {
            disableScreenTimeout()
            enabled()
        } else {
            enableScreenTimeout()
            disabled()
        }
        qsTile.updateTile()
    }

    private fun disabled() {
        qsTile.state = Tile.STATE_INACTIVE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            qsTile.subtitle = getString(androidx.compose.ui.R.string.off)
        }
    }

    private fun enabled() {
        qsTile.state = Tile.STATE_ACTIVE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            qsTile.subtitle = getString(androidx.compose.ui.R.string.on)
        }
    }

    private fun disableScreenTimeout() {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.PREVIOUS_SCREEN_TIMEOUT] = screenTimeout
            }
        }
        Settings.System.putInt(
            contentResolver,
            Settings.System.SCREEN_OFF_TIMEOUT,
            2147483647
        )
    }

    private fun enableScreenTimeout() {
        runBlocking {
            val previousScreenTimeout = screenTimeoutPreferenceFlow.first()
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, previousScreenTimeout)
            screenTimeout = previousScreenTimeout
        }
    }
}

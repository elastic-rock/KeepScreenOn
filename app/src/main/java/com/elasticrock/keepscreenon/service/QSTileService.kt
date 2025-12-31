package com.elasticrock.keepscreenon.service

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
import com.elasticrock.keepscreenon.R
import com.elasticrock.keepscreenon.data.model.KeepScreenOnState
import com.elasticrock.keepscreenon.data.repository.KeepScreenOnRepository
import com.elasticrock.keepscreenon.data.repository.PreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class QSTileService : TileService() {

    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var keepScreenOnRepository: KeepScreenOnRepository
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var uiUpdateJob: Job = Job()


    override fun onTileAdded() {
        super.onTileAdded()
        serviceScope.launch { preferencesRepository.saveIsTileAdded(true) }
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        serviceScope.launch { preferencesRepository.saveIsTileAdded(false) }
    }

    override fun onStartListening() {
        super.onStartListening()
        serviceScope.launch {
            val keepScreenOnState = keepScreenOnRepository.keepScreenOnState.first()

            uiUpdateJob.cancelAndJoin()
            uiUpdateJob = launch(Dispatchers.Main) {
                qsTile.label = getString(R.string.keep_screen_on)
                when (keepScreenOnState) {
                    is KeepScreenOnState.PermissionNotGranted -> {
                        qsTile.state = Tile.STATE_INACTIVE
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            qsTile.subtitle = getString(R.string.grant_permission)
                        }
                        qsTile.updateTile()
                    }

                    is KeepScreenOnState.Enabled -> {
                        activeState(keepScreenOnState.currentTimeout)
                    }

                    is KeepScreenOnState.Disabled -> {
                        inactiveState(keepScreenOnState.currentTimeout)
                    }
                }
            }

            preferencesRepository.saveIsTileAdded(true)
        }
    }

    override fun onClick() {
        super.onClick()
        serviceScope.launch {
            when (val keepScreenOnState = keepScreenOnRepository.keepScreenOnState.first()) {
                is KeepScreenOnState.PermissionNotGranted -> {
                    withContext(Dispatchers.Main.immediate) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            startActivityAndCollapse(
                                PendingIntent.getActivity(
                                    applicationContext,
                                    1,
                                    Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                                        data = ("package:" + applicationContext.packageName).toUri()
                                    },
                                    FLAG_IMMUTABLE + FLAG_UPDATE_CURRENT
                                )
                            )
                        } else {
                            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                                data = ("package:" + applicationContext.packageName).toUri()
                            }
                            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                            intent.addFlags(FLAG_ACTIVITY_SINGLE_TOP)
                            @Suppress("DEPRECATION", "StartActivityAndCollapseDeprecated")
                            startActivityAndCollapse(intent)
                        }
                    }
                }

                is KeepScreenOnState.Enabled -> {
                    uiUpdateJob.cancelAndJoin()
                    uiUpdateJob = launch(Dispatchers.Main) {
                        inactiveState(keepScreenOnState.previousTimeout)
                    }
                    keepScreenOnRepository.disableKeepScreenOn()
                }

               is KeepScreenOnState.Disabled -> {
                    uiUpdateJob.cancelAndJoin()
                    uiUpdateJob = launch(Dispatchers.Main) {
                        activeState(keepScreenOnState.maximumTimeout)
                    }
                    keepScreenOnRepository.enableKeepScreenOn()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        serviceScope.launch {
            uiUpdateJob.cancelAndJoin()
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

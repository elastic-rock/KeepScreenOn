package com.elasticrock.keepscreenon

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.MutableLiveData
import com.elasticrock.keepscreenon.ui.theme.KeepScreenOnTheme

const val tag = "MainActivity"
const val notificationPermission = "android.permission.POST_NOTIFICATIONS"
val canWriteSettingsState = MutableLiveData(false)
val isIgnoringBatteryOptimizationState = MutableLiveData(false)
val isTileAddedState = MutableLiveData(false)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(tag, "Lifecycle: onCreate()")
        super.onCreate(savedInstanceState)
        setContent {
            KeepScreenOnTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    KeepScreenOnApp()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(tag, "Lifecycle: onStart()")
        val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        canWriteSettingsState.value = Settings.System.canWrite(applicationContext)
        isIgnoringBatteryOptimizationState.value = pm.isIgnoringBatteryOptimizations(applicationContext.packageName)
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(tag, "Lifecycle: onRestart()")
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "Lifecycle: onResume()")
    }

    override fun onStop() {
        super.onStop()
        Log.d(tag, "Lifecycle: onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "Lifecycle: onDestroy()")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeepScreenOnApp() {
    val context = LocalContext.current

    var showInfoDialog by remember { mutableStateOf(false) }
    if (showInfoDialog) {
        InfoDialog(
            onDismiss = { showInfoDialog = false }
        )
    }

    Scaffold(Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.keep_screen_on),
                        style = MaterialTheme.typography.titleLarge)
                        },
                actions = {
                    IconButton(
                        onClick = { showInfoDialog = true })
                    { Icon(imageVector = Icons.Filled.Info, contentDescription = null)}}
            )
        }, content = {
            LazyColumn(Modifier.padding(it)) {

                item {
                    PreferenceSubtitle(text = stringResource(id = R.string.qs_tile))
                }

                item {
                    val isTileAdded by isTileAddedState.observeAsState(false)
                    if (isTileAdded) {
                        PreferencesHintCard(
                            title = stringResource(id = (R.string.tile_already_added)),
                            description = stringResource(id = R.string.qs_tile_hidden),
                            enabled = false
                        )
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            PreferencesHintCard(
                                title = stringResource(id = (R.string.add_qs_tile)),
                                description = stringResource(id = R.string.add_qs_tile_alternate),
                                icon = Icons.Filled.Add,
                                onClick = {
                                    val statusBarService = context.getSystemService(StatusBarManager::class.java)
                                    statusBarService.requestAddTileService(
                                        ComponentName(context, QSTileService::class.java.name),
                                        context.getString(R.string.keep_screen_on),
                                        Icon.createWithResource(context,R.drawable.outline_lock_clock_qs),
                                        {}) {}
                                }
                            )
                        } else {
                            PreferencesHintCard(
                                title = stringResource(id = (R.string.add_qs_tile)),
                                description = stringResource(R.string.add_tile_instructions),
                                enabled = false
                            )
                        }
                    }
                }

                item {
                    PreferenceSubtitle(text = stringResource(id = R.string.permissions))
                }
                
                item {
                    val canWriteSettings by canWriteSettingsState.observeAsState(false)
                    if (canWriteSettings) {
                        PreferenceItem(
                            title = stringResource(id = R.string.modify_system_settings),
                            description = stringResource(id = R.string.permission_granted),
                            enabled = false,
                            icon = Icons.Filled.Settings
                        )
                    } else {
                        PreferenceItem(
                            title = stringResource(id = R.string.modify_system_settings),
                            description = stringResource(id = R.string.this_permission_is_required),
                            enabled = true,
                            icon = Icons.Filled.Settings,
                            onClick = { context.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply { data = Uri.parse("package:${context.packageName}") } ) }
                        )
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    item {
                        var isPermissionGranted by remember { mutableIntStateOf(checkSelfPermission(context, notificationPermission)) }
                        val requestPermissionLauncher = rememberLauncherForActivityResult(
                            ActivityResultContracts.RequestPermission()
                        ) { isGranted: Boolean ->
                            isPermissionGranted = if (isGranted) {
                                PERMISSION_GRANTED

                            } else {
                                PERMISSION_DENIED
                            }
                        }
                        if (isPermissionGranted == PERMISSION_GRANTED) {
                            PreferenceItem(
                                title = stringResource(id = R.string.notifications),
                                description = stringResource(id = R.string.permission_granted),
                                enabled = false,
                                icon = Icons.Filled.Notifications
                            )
                        } else {
                            PreferenceItem(
                                title = stringResource(id = R.string.notifications),
                                description = stringResource(id = R.string.posted_when_keep_screen_on_is_active),
                                enabled = true,
                                icon = Icons.Filled.Notifications,
                                onClick = { requestPermissionLauncher.launch(notificationPermission) }
                            )
                        }
                    }
                }

                item {
                    val isIgnoringBatteryOptimization by isIgnoringBatteryOptimizationState.observeAsState(false)
                    if (isIgnoringBatteryOptimization) {
                        PreferenceItem(
                            title = stringResource(id = R.string.ignore_battery_optimizations),
                            description = stringResource(id = R.string.permission_granted),
                            enabled = false,
                            icon = Icons.Filled.EnergySavingsLeaf
                        )
                    } else {
                        PreferenceItem(
                            title = stringResource(id = R.string.ignore_battery_optimizations),
                            description = stringResource(id = R.string.allow_if_you_encounter_issues),
                            enabled = true,
                            icon = Icons.Filled.EnergySavingsLeaf,
                            onClick = { context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
                        )
                    }
                }
                
                item { 
                    PreferenceSubtitle(text = stringResource(id = R.string.options))
                }

                item {
                    var checked by remember { mutableStateOf(UserPreferencesRepository().readListenForBatteryLow(context)) }
                    PreferenceSwitch(
                        title = stringResource(id = (R.string.restrore_timeout_when_battery_low)),
                        icon = Icons.Filled.BatteryAlert,
                        isChecked = checked,
                        onClick = {
                            checked = !checked
                            UserPreferencesRepository().saveListenForBatteryLow(context, checked)
                        }
                    )
                }

                item {
                    var checked by remember { mutableStateOf(UserPreferencesRepository().readListenForScreenOff(context)) }
                    PreferenceSwitch(
                        title = stringResource(id = (R.string.restore_timeout_when_screen_is_turned_off)),
                        icon = Icons.Filled.Lock,
                        isChecked = checked,
                        onClick = {
                            checked = !checked
                            UserPreferencesRepository().saveListenForScreenOff(context, checked)
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun InfoDialog(onDismiss: () -> Unit) {
    /*TODO*/
    AlertDialog(onDismissRequest = { onDismiss() },
        title = { Text(text = stringResource(R.string.about)) },
        text = { Text(text = stringResource(R.string.about_dialog_description))},
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = stringResource(R.string.exit))
            }
        }
    )
}
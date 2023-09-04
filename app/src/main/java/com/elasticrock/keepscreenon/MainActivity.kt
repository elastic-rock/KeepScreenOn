package com.elasticrock.keepscreenon

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat.checkSelfPermission
import com.elasticrock.keepscreenon.ui.theme.KeepScreenOnTheme

const val tag = "MainActivity"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeepScreenOnApp() {
    val context = LocalContext.current
    Scaffold(Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.keep_screen_on), style = MaterialTheme.typography.titleLarge) },
                actions = { IconButton(onClick = { /*TODO*/ }) { Icon(imageVector = Icons.Filled.Info, contentDescription = null)}}
            )
        }, content = {
            LazyColumn(Modifier.padding(it)) {

                item {
                    PreferenceSubtitle(text = stringResource(id = R.string.permissions))
                }
                
                item {
                    if (!Settings.System.canWrite(context)) {
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
                            onClick = { context.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)) }
                        )
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    item {
                        if (checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS") == PERMISSION_GRANTED) {
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
                                onClick = { /*TODO*/ }
                            )
                        }
                    }
                }

                item {
                    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                    if (pm.isIgnoringBatteryOptimizations(context.packageName)) {
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
                            onClick = {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply { data = Uri.parse("package:${context.packageName}") }
                                context.startActivity(intent)
                                /*TODO*/ //Darken background
                            }
                        )
                    }
                }

                item {
                    PreferenceSubtitle(text = stringResource(id = R.string.qs_tile))
                }

                item {
                    if (UserPreferencesRepository().readIsTileAdded(context)) {
                        Text(text = stringResource(id = (R.string.tile_already_added)))
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Button(
                                onClick = {
                                    val statusBarService = context.getSystemService(StatusBarManager::class.java)
                                    statusBarService.requestAddTileService(
                                    ComponentName(context, QSTileService::class.java.name),
                                    context.getString(R.string.keep_screen_on),
                                    Icon.createWithResource(context,R.drawable.outline_lock_clock_qs),
                                    {}) {}
                                }
                            ) {
                                Text(text = stringResource(id = (R.string.add_qs_tile)))
                            }
                        } else {
                            Text(text = stringResource(R.string.add_tile_instructions),
                                textAlign = TextAlign.Center)
                        }
                    }
                }
                
                item { 
                    PreferenceSubtitle(text = stringResource(id = R.string.options))
                }

                item {
                    var batteryChecked by remember { mutableStateOf(UserPreferencesRepository().readListenForBatteryLow(context)) }
                    PreferenceSwitch(
                        title = stringResource(id = (R.string.restrore_timeout_when_battery_low)),
                        icon = Icons.Filled.BatteryAlert,
                        isChecked = batteryChecked,
                        onClick = {
                            batteryChecked = !batteryChecked
                            UserPreferencesRepository().saveListenForBatteryLow(context, batteryChecked)
                        }
                    )
                }

                item {
                    var screenChecked by remember { mutableStateOf(UserPreferencesRepository().readListenForScreenOff(context)) }
                    PreferenceSwitch(
                        title = stringResource(id = (R.string.restore_timeout_when_screen_is_turned_off)),
                        icon = Icons.Filled.Lock,
                        isChecked = screenChecked,
                        onClick = {
                            screenChecked = !screenChecked
                            UserPreferencesRepository().saveListenForBatteryLow(context, screenChecked)
                        }
                    )
                }
            }
        }
    )
}

@Preview
@Composable
fun KeepScreenOnAppPreview() {
    KeepScreenOnApp()
}
package com.elasticrock.keepscreenon.ui.main

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.drawable.Icon
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elasticrock.keepscreenon.QSTileService
import com.elasticrock.keepscreenon.R
import com.elasticrock.keepscreenon.canWriteSettingsState
import com.elasticrock.keepscreenon.isIgnoringBatteryOptimizationState
import com.elasticrock.keepscreenon.screenTimeoutState
import com.elasticrock.keepscreenon.ui.components.IgnoreBatteryOptimizationsDialog
import com.elasticrock.keepscreenon.ui.components.PreferenceItem
import com.elasticrock.keepscreenon.ui.components.PreferenceSubtitle
import com.elasticrock.keepscreenon.ui.components.PreferenceSwitch
import com.elasticrock.keepscreenon.ui.components.PreferencesHintCard
import com.elasticrock.keepscreenon.util.CommonUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onInfoButtonClick: () -> Unit,
    viewModel: MainScreenViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    val notificationPermission = "android.permission.POST_NOTIFICATIONS"
    val context = LocalContext.current
    val isIgnoringBatteryOptimization by isIgnoringBatteryOptimizationState.observeAsState(false)

    val layoutDirection = LocalLayoutDirection.current
    val displayCutout = WindowInsets.displayCutout.asPaddingValues()
    val startPadding = displayCutout.calculateStartPadding(layoutDirection)
    val endPadding = displayCutout.calculateEndPadding(layoutDirection)

    Scaffold(
        Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.keep_screen_on),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                modifier = Modifier.padding(start = startPadding, end = endPadding),
                actions = { IconButton(onClick = onInfoButtonClick) { Icon(Icons.Filled.Info, contentDescription = stringResource(id = R.string.about)) } }
            )
        }, content = { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier.padding(start = startPadding, end = endPadding)
            ) {

                item {
                    PreferenceSubtitle(text = stringResource(id = R.string.qs_tile))
                }

                item {
                    if (state.value.isTileAdded) {
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
                                        Icon.createWithResource(context, R.drawable.outline_lock_clock_qs),
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
                            onClick = { context.startActivity(CommonUtils().modifySystemSettingsIntent) }
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
                    PreferenceSubtitle(text = stringResource(id = R.string.options))
                }

                item {
                    var openDialog by remember { mutableStateOf(false) }
                    val checked = state.value.isRestoreWhenBatteryLowEnabled && isIgnoringBatteryOptimization
                    PreferenceSwitch(
                        title = stringResource(id = (R.string.restore_timeout_when_battery_low)),
                        icon = Icons.Filled.BatteryAlert,
                        isChecked = checked,
                        onClick = {
                            if (!isIgnoringBatteryOptimization) {
                                openDialog = true
                            } else {
                                viewModel.onRestoreWhenBatteryLowChange(!checked)
                            }
                        }
                    )

                    if (openDialog) {
                        IgnoreBatteryOptimizationsDialog(onDismissRequest = { openDialog = false }, context)
                    }
                }

                item {
                    var openDialog by remember { mutableStateOf(false) }
                    val checked = state.value.isRestoreWhenScreenOffEnabled && isIgnoringBatteryOptimization
                    PreferenceSwitch(
                        title = stringResource(id = (R.string.restore_timeout_when_screen_is_turned_off)),
                        icon = Icons.Filled.Lock,
                        isChecked = checked,
                        onClick = {
                            if (!isIgnoringBatteryOptimization) {
                                openDialog = true
                            } else {
                                viewModel.onRestoreWhenScreenOffChange(!checked)
                            }
                        }
                    )

                    if (openDialog) {
                        IgnoreBatteryOptimizationsDialog(onDismissRequest = { openDialog = false }, context)
                    }
                }

                item {
                    PreferenceSubtitle(text = stringResource(id = R.string.advanced))
                }

                item {
                    var openDialog by remember { mutableStateOf(false) }
                    var currentMaxTimeout by remember { mutableStateOf(state.value.maxTimeout.toString()) }
                    PreferenceItem(
                        title = stringResource(id = R.string.maximum_timeout_value),
                        description = stringResource(id = R.string.maximum_description),
                        icon = Icons.Filled.Build,
                        onClick = { openDialog = true }
                    )

                    if (openDialog) {
                        BasicAlertDialog(
                            onDismissRequest = {
                                openDialog = false
                                currentMaxTimeout = state.value.maxTimeout.toString()
                            }
                        ) {
                            Surface(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .wrapContentHeight(),
                                shape = MaterialTheme.shapes.large,
                                tonalElevation = AlertDialogDefaults.TonalElevation
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Text(text = stringResource(id = R.string.maximum_timeout_value), style = MaterialTheme.typography.titleLarge)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(text = stringResource(id = R.string.maximum_explenation), style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    OutlinedTextField(
                                        value = currentMaxTimeout,
                                        onValueChange = { currentMaxTimeout = it },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        isError = if (currentMaxTimeout.toLongOrNull() == null) { true } else currentMaxTimeout.toLong() > Int.MAX_VALUE,
                                        supportingText = {
                                            if (currentMaxTimeout.toLongOrNull() == null) {
                                                @Suppress("UNUSED_EXPRESSION")
                                                null
                                            } else if (currentMaxTimeout.toLong() > Int.MAX_VALUE) {
                                                Text(text = stringResource(id = R.string.int_max_value_warning))
                                            } else {
                                                @Suppress("UNUSED_EXPRESSION")
                                                null
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        TextButton(
                                            onClick = { currentMaxTimeout = Int.MAX_VALUE.toString() }
                                        ) {
                                            Text(text = stringResource(id = R.string.reset))
                                        }
                                        TextButton(
                                            onClick = {
                                                openDialog = false
                                                viewModel.onMaxTimeoutChange(currentMaxTimeout.toInt())
                                            },
                                            enabled = if (currentMaxTimeout.toLongOrNull() == null) { false } else currentMaxTimeout.toLong() <= Int.MAX_VALUE
                                        ) {
                                            Text(text = stringResource(id = R.string.confirm))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    val screenTimeout by screenTimeoutState.observeAsState(0)
                    Text(
                        text = stringResource(R.string.current_screen_timeout, screenTimeout),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 18.dp, top = 24.dp, bottom = 12.dp),
                    )
                }
            }

            val density = LocalDensity.current
            val tappableElement = WindowInsets.tappableElement
            val bottomPixels = tappableElement.getBottom(density)
            val usingTappableBars = remember(bottomPixels) {
                bottomPixels != 0
            }
            val barHeight = remember(bottomPixels) {
                tappableElement.asPaddingValues(density).calculateBottomPadding()
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom
            ) {
                if (usingTappableBars) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxWidth()
                            .height(barHeight)
                    )
                }
            }
        }
    )
}
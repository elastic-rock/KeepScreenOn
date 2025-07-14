package com.elasticrock.keepscreenon.ui.main

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elasticrock.keepscreenon.BuildConfig
import com.elasticrock.keepscreenon.QSTileService
import com.elasticrock.keepscreenon.R
import com.elasticrock.keepscreenon.canWriteSettingsState
import com.elasticrock.keepscreenon.isIgnoringBatteryOptimizationState
import com.elasticrock.keepscreenon.isNotificationPermissionGranted
import com.elasticrock.keepscreenon.screenTimeoutState
import com.elasticrock.keepscreenon.ui.components.PreferenceItem
import com.elasticrock.keepscreenon.ui.components.PreferenceSubtitle
import com.elasticrock.keepscreenon.ui.components.PreferenceSwitch
import com.elasticrock.keepscreenon.ui.components.PreferencesHintCard
import com.elasticrock.keepscreenon.util.notificationPermission
import com.elasticrock.keepscreenon.util.reviewPrompt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onInfoButtonClick: () -> Unit,
    onDonateButtonClick: () -> Unit,
    viewModel: MainScreenViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current
    val isIgnoringBatteryOptimization by isIgnoringBatteryOptimizationState.observeAsState(false)

    val layoutDirection = LocalLayoutDirection.current
    val displayCutout = WindowInsets.displayCutout.asPaddingValues()
    val startPadding = displayCutout.calculateStartPadding(layoutDirection)
    val endPadding = displayCutout.calculateEndPadding(layoutDirection)

    LaunchedEffect(state.value.displayReviewPrompt) {
        @Suppress("KotlinConstantConditions")
        if (BuildConfig.FLAVOR == "play" && state.value.displayReviewPrompt) {
            reviewPrompt(context, activity!!)
        }
    }

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
                actions = {
                    IconButton(onClick = onDonateButtonClick) {
                        Icon(Icons.Filled.VolunteerActivism, contentDescription = stringResource(id = R.string.donate))
                    }
                    IconButton(onClick = onInfoButtonClick) {
                        Icon(Icons.Filled.Info, contentDescription = stringResource(id = R.string.about))
                    }
                }
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
                            onClick = { context.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply { data = ("package:" + context.packageName).toUri() } ) }
                        )
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    item {
                        val isPermissionGranted by isNotificationPermissionGranted.observeAsState(false)
                        var shouldShowRequestPermissionRationale by remember { mutableStateOf(false) }
                        val requestPermissionLauncher = rememberLauncherForActivityResult(
                            ActivityResultContracts.RequestPermission()
                        ) { isGranted: Boolean ->
                            isNotificationPermissionGranted.value = isGranted
                            if (!isGranted && shouldShowRequestPermissionRationale) {
                                viewModel.onNotificationDeniedPermanentlyChange(true)
                            }
                        }
                        if (isPermissionGranted) {
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
                                onClick = {
                                    if (shouldShowRequestPermissionRationale(activity!!, notificationPermission)) {
                                        shouldShowRequestPermissionRationale = true
                                    }
                                    if (viewModel.state.value.isNotificationPermissionDeniedPermanently) {
                                        context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply { putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName) })
                                    } else {
                                        requestPermissionLauncher.launch(notificationPermission)
                                    }
                                }
                            )
                        }
                    }
                }

                item {
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
                            description = stringResource(id = R.string.needed_to_restore_timeout_automatically),
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
                    val checked = state.value.isRestoreWhenBatteryLowEnabled && isIgnoringBatteryOptimization
                    PreferenceSwitch(
                        title = stringResource(id = (R.string.restore_timeout_when_battery_low)),
                        description = if (!isIgnoringBatteryOptimization) stringResource(R.string.the_app_must_be_exempt_from_battery_optimizations) else null,
                        icon = Icons.Filled.BatteryAlert,
                        isChecked = checked,
                        enabled = isIgnoringBatteryOptimization,
                        onClick = {
                            viewModel.onRestoreWhenBatteryLowChange(!checked)
                        }
                    )
                }

                item {
                    val checked = state.value.isRestoreWhenScreenOffEnabled && isIgnoringBatteryOptimization
                    PreferenceSwitch(
                        title = stringResource(id = (R.string.restore_timeout_when_screen_is_turned_off)),
                        description = if (!isIgnoringBatteryOptimization) stringResource(R.string.the_app_must_be_exempt_from_battery_optimizations) else null,
                        icon = Icons.Filled.Lock,
                        isChecked = checked,
                        enabled = isIgnoringBatteryOptimization,
                        onClick = {
                            viewModel.onRestoreWhenScreenOffChange(!checked)
                        }
                    )
                }

                item {
                    var expanded by remember { mutableStateOf(false) }

                    val timeoutOptions = mapOf(
                        60000 to pluralStringResource(R.plurals.minute, 1, 1),
                        120000 to pluralStringResource(R.plurals.minute, 2, 2),
                        300000 to pluralStringResource(R.plurals.minute, 5, 5),
                        600000 to pluralStringResource(R.plurals.minute, 10, 10),
                        1800000 to pluralStringResource(R.plurals.minute, 30, 30),
                        Int.MAX_VALUE to stringResource(R.string.always_on)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = timeoutOptions[state.value.maxTimeout]!!,
                                onValueChange = {},
                                label = { Text(stringResource(id = R.string.set_timeout_value)) },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                            ) {
                                timeoutOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(text = option.value, style = MaterialTheme.typography.bodyLarge) },
                                        onClick = {
                                            viewModel.onMaxTimeoutChange(option.key)
                                            expanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = state.value.maxTimeout > 600000
                        ) {
                            Text(
                                text = stringResource(R.string.screen_timeout_warning),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp)
                            )
                        }
                    }
                }

                item {
                    val screenTimeout by screenTimeoutState.observeAsState(0)
                    Text(
                        text = if (screenTimeout < 60000) {
                            stringResource(R.string.current_screen_timeout) + pluralStringResource(R.plurals.second, screenTimeout/1000, screenTimeout/1000)
                        } else if (screenTimeout < 3600000) {
                            stringResource(R.string.current_screen_timeout) + pluralStringResource(R.plurals.minute, screenTimeout/60000, screenTimeout/60000)
                        } else if (screenTimeout < 86400000) {
                            stringResource(R.string.current_screen_timeout) + pluralStringResource(R.plurals.hour, screenTimeout/3600000, screenTimeout/3600000)
                        } else if (screenTimeout == Int.MAX_VALUE) {
                            stringResource(R.string.current_screen_timeout) + stringResource(R.string.always_on)
                        } else {
                            stringResource(R.string.current_screen_timeout) + pluralStringResource(R.plurals.day, screenTimeout/86400000, screenTimeout/86400000)
                        },
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
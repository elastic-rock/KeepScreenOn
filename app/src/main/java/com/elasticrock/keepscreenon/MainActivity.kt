package com.elasticrock.keepscreenon

import android.app.StatusBarManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.WindowCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.elasticrock.keepscreenon.ui.theme.KeepScreenOnTheme
import com.elasticrock.keepscreenon.ui.theme.applyOpacity
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

val canWriteSettingsState = MutableLiveData(false)
val isIgnoringBatteryOptimizationState = MutableLiveData(false)
val isTileAddedState = MutableLiveData(false)
val screenTimeoutState = MutableLiveData(0)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            KeepScreenOnTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    App(dataStore)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        canWriteSettingsState.value = Settings.System.canWrite(applicationContext)
        isIgnoringBatteryOptimizationState.value = pm.isIgnoringBatteryOptimizations(applicationContext.packageName)
        screenTimeoutState.value = CommonUtils().readScreenTimeout(contentResolver)
    }
}

@Composable
fun App(dataStore: DataStore<Preferences>) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(dataStore, navController) }
        composable("info") { InfoScreen(navController) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(navController: NavHostController) {
    val context = LocalContext.current
    val clipboard = getSystemService(context, ClipboardManager::class.java) as ClipboardManager

    Scaffold(Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.about)) },
                navigationIcon = { IconButton(onClick = {
                        navController.navigate("main")
                        Log.d("MainActivity", "Navigate to Main")
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Go back")
                    }
                }
            )
                 },
        content = { padding ->
            LazyColumn(Modifier.padding(padding)) {
                item {
                    AboutItem(
                        title = stringResource(id = R.string.author),
                        subtitle = stringResource(id = R.string.david_weis)
                    )
                }

                item {
                    val url = "https://github.com/elastic-rock/KeepScreenOn"
                    val intent = Intent(Intent.ACTION_VIEW)
                    AboutItem(
                        title = stringResource(id = R.string.source_code),
                        subtitle = stringResource(id = R.string.github),
                        onClick = {
                            intent.data = Uri.parse(url)
                            context.startActivity(intent)
                        }
                    )
                }

                item {
                    val appId = "com.elasticrock.keepscreenon"
                    AboutItem(
                        title = stringResource(id = R.string.application_id),
                        subtitle = appId,
                        onClick = {
                            val clip: ClipData = ClipData.newPlainText("simple text", appId)
                            clipboard.setPrimaryClip(clip)
                        }
                    )
                }

                item {
                    fun getAppVersion(context: Context): String {
                        return try {
                            val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                            packageInfo.versionName
                        } catch (e: PackageManager.NameNotFoundException) {
                            e.printStackTrace()
                            "Unknown"
                        }
                    }

                    val version = getAppVersion(context)

                    AboutItem(
                        title = stringResource(id = R.string.version),
                        subtitle = version,
                        onClick = {
                            val clip: ClipData = ClipData.newPlainText("simple text", version)
                            clipboard.setPrimaryClip(clip)
                        }
                    )
                }

                item {
                    val url = "https://gnu.org/licenses/gpl-3.0.txt"
                    val intent = Intent(Intent.ACTION_VIEW)
                    AboutItem(
                        title = stringResource(id = R.string.license),
                        subtitle = "GPL-3.0",
                        onClick = {
                            intent.data = Uri.parse(url)
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(dataStore: DataStore<Preferences>, navController: NavHostController) {
    val notificationPermission = "android.permission.POST_NOTIFICATIONS"
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isIgnoringBatteryOptimization by isIgnoringBatteryOptimizationState.observeAsState(false)

    Scaffold(Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.keep_screen_on),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = { IconButton(onClick = {
                    navController.navigate("info")
                    Log.d("MainActivity", "Navigate to Info")
                })  { Icon(Icons.Filled.Info, contentDescription = stringResource(id = R.string.about)) } }
            )
        }, content = { padding ->
            LazyColumn(Modifier.padding(padding)) {

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
                    var checked by remember { mutableStateOf(runBlocking { DataStoreRepository(dataStore).readListenForBatteryLow() } ) }
                    PreferenceSwitch(
                        title = stringResource(id = (R.string.restore_timeout_when_battery_low)),
                        icon = Icons.Filled.BatteryAlert,
                        isChecked = checked,
                        onClick = {
                            if (!isIgnoringBatteryOptimization) {
                                openDialog = true
                            } else {
                                checked = !checked
                                scope.launch { DataStoreRepository(dataStore).saveListenForBatteryLow(checked) }
                            }
                        }
                    )

                    if (openDialog) {
                        IgnoreBatteryOptimizationsDialog(onDismissRequest = { openDialog = false }, context)
                    }
                }

                item {
                    var openDialog by remember { mutableStateOf(false) }
                    var checked by remember { mutableStateOf(runBlocking { DataStoreRepository(dataStore).readListenForScreenOff() }) }
                    PreferenceSwitch(
                        title = stringResource(id = (R.string.restore_timeout_when_screen_is_turned_off)),
                        icon = Icons.Filled.Lock,
                        isChecked = checked,
                        onClick = {
                            if (!isIgnoringBatteryOptimization) {
                                openDialog = true
                            } else {
                                checked = !checked
                                scope.launch { DataStoreRepository(dataStore).saveListenForScreenOff(checked) }
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
                    var currentMaxTimeout by rememberSaveable { mutableStateOf(runBlocking { DataStoreRepository(dataStore).readMaximumTimeout().toString() }) }
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
                                scope.launch { currentMaxTimeout = DataStoreRepository(dataStore).readMaximumTimeout().toString() }
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
                                                scope.launch { DataStoreRepository(dataStore).saveMaximumTimeout(currentMaxTimeout.toInt()) }
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
        }
    )
}

private const val horizontal = 8
private const val vertical = 16

@Composable
fun PreferencesHintCard(
    title: String = "Title ".repeat(2),
    description: String? = "Description text ".repeat(3),
    icon: ImageVector? = Icons.Outlined.Info,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onClick: () -> Unit = {},
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(containerColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp, end = 16.dp)
                    .size(24.dp),
                tint = contentColor
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = if (icon == null) 12.dp else 0.dp, end = 12.dp)
        ) {
            with(MaterialTheme) {
                Text(
                    text = title,
                    maxLines = 1,
                    style = typography.titleLarge.copy(fontSize = 20.sp),
                    color = contentColor
                )
                if (description != null) Text(
                    text = description,
                    color = contentColor,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    style = typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
fun PreferenceItem(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.clickable(
            onClick = onClick,
            enabled = enabled,
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal.dp, vertical.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 16.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.applyOpacity(enabled)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = if (icon == null) 12.dp else 0.dp)
                    .padding(end = 8.dp)
            ) {
                PreferenceItemTitle(text = title, enabled = enabled)
                if (!description.isNullOrEmpty()) PreferenceItemDescription(
                    text = description,
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
fun AboutItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.clickable(
            onClick = onClick
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal.dp, vertical.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .padding(end = 8.dp)
            ) {
                PreferenceItemTitle(text = title)
                if (!subtitle.isNullOrEmpty()) PreferenceItemDescription(
                    text = subtitle
                )
            }
        }
    }
}

@Composable
fun PreferenceSwitch(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isChecked: Boolean = true,
    checkedIcon: ImageVector = Icons.Outlined.Check,
    onClick: (() -> Unit) = {},
) {
    val thumbContent: (@Composable () -> Unit)? = if (isChecked) {
        {
            Icon(
                imageVector = checkedIcon,
                contentDescription = null,
                modifier = Modifier.size(SwitchDefaults.IconSize),
            )
        }
    } else {
        null
    }
    Surface(
        modifier = Modifier.toggleable(value = isChecked,
            enabled = enabled,
            onValueChange = { onClick() })
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal.dp, vertical.dp)
                .padding(start = if (icon == null) 12.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 16.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.applyOpacity(enabled)
                )
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                PreferenceItemTitle(
                    text = title,
                    enabled = enabled
                )
                if (!description.isNullOrEmpty()) PreferenceItemDescription(
                    text = description,
                    enabled = enabled
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = null,
                modifier = Modifier.padding(start = 20.dp, end = 6.dp),
                enabled = enabled,
                thumbContent = thumbContent
            )
        }
    }
}

@Composable
fun PreferenceItemTitle(
    modifier: Modifier = Modifier,
    text: String,
    maxLines: Int = 2,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.onBackground.applyOpacity(enabled),
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    Text(
        modifier = modifier,
        text = text,
        maxLines = maxLines,
        style = style,
        color = color,
        overflow = overflow
    )
}

@Composable
fun PreferenceItemDescription(
    modifier: Modifier = Modifier,
    text: String,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant.applyOpacity(enabled)
) {
    Text(
        modifier = modifier.padding(top = 2.dp),
        text = text,
        maxLines = maxLines,
        style = style,
        color = color,
        overflow = overflow
    )
}

@Composable
fun PreferenceSubtitle(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(start = 18.dp, top = 24.dp, bottom = 12.dp),
    text: String,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        color = color,
        style = MaterialTheme.typography.labelLarge
    )
}

@Composable
fun IgnoreBatteryOptimizationsDialog(
    onDismissRequest: () -> Unit,
    context: Context
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = { TextButton(onClick = {
            context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            onDismissRequest.invoke()
        }) { Text(text = stringResource(id = R.string.confirm)) } },
        title = { Text(text = stringResource(id = R.string.ignore_battery_optimizations)) },
        text = { Text(text = stringResource(id = R.string.ignore_battery_optimizations_dialog_description)) }
    )
}
package com.elasticrock.keepscreenon

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                    if (!Settings.System.canWrite(applicationContext)) {
                        GrantPermissionDialog()
                    }
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
                title = { Text(stringResource(R.string.keep_screen_on)) }
            )
        }
    ) {padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (UserPreferencesRepository().readIsTileAdded(context)) {
                Text(text = stringResource(id = (R.string.tile_already_added)))
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Button(onClick = {
                        val statusBarService = context.getSystemService(StatusBarManager::class.java)
                        statusBarService.requestAddTileService(
                            ComponentName(context, QSTileService::class.java.name),
                            context.getString(R.string.keep_screen_on),
                            Icon.createWithResource(context,R.drawable.outline_lock_clock_qs),
                            {}) {}
                    }) {
                        Text(text = stringResource(id = (R.string.add_qs_tile)))
                    }
                } else {
                    Text(text = stringResource(R.string.add_tile_instructions),
                        textAlign = TextAlign.Center)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                var checked by remember { mutableStateOf(UserPreferencesRepository().readListenForBatteryLow(context)) }
                Text(text = stringResource(id = (R.string.restrore_timeout_when_battery_low)))
                Switch(
                    checked = checked,
                    onCheckedChange = {
                        checked = it
                        UserPreferencesRepository().saveListenForBatteryLow(context, checked)
                    }
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                var checked by remember { mutableStateOf(UserPreferencesRepository().readListenForScreenOff(context)) }
                Text(text = stringResource(id = (R.string.restore_timeout_when_screen_is_turned_off)))
                Switch(
                    checked = checked,
                    onCheckedChange = {
                        checked = it
                        UserPreferencesRepository().saveListenForScreenOff(context, checked)
                    }
                )
            }
        }
    }
}

@Composable
fun GrantPermissionDialog() {
    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(true) }

    if (openDialog.value) {
        AlertDialog(onDismissRequest = { openDialog.value = false },
            title = { Text(text = stringResource(R.string.grant_permission)) },
            dismissButton = {
                TextButton(onClick = { openDialog.value = false }) {
                    Text(text = stringResource(R.string.dismiss))
                } },
            text = { Text(text = stringResource(R.string.permission_description))},
            confirmButton = {
                TextButton(onClick = {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS))
                    openDialog.value = false }) {
                    Text(text = stringResource(R.string.grant_permission))
                }
            }
        )
    }
}

@Preview
@Composable
fun KeepScreenOnAppPreview() {
    KeepScreenOnApp()
}

@Preview
@Composable
fun GrantPermissionDialogPreview() {
    GrantPermissionDialog()
}
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elasticrock.keepscreenon.ui.theme.KeepScreenOnTheme

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

@Composable
fun KeepScreenOnApp() {
    val context = LocalContext.current
    Column(
        Modifier.padding(all = 8.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.app_name),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(text = stringResource(R.string.permission_description),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        if (!Settings.System.canWrite(context)) {
            Button(
                onClick = { context.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS))}
            ) {
                Text(text = stringResource(R.string.grant_permission))
            }
        }
        else {
            Button(onClick = { }, enabled = false) {
                Text(text = stringResource(R.string.permission_granted))
            }
        }
        Spacer(Modifier.height(32.dp))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Button(onClick = {
                val statusBarService = context.getSystemService(StatusBarManager::class.java)
                statusBarService.requestAddTileService(
                    ComponentName(context, QSTileService::class.java.name),
                    context.getString(R.string.screen_timeout),
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
}

@Preview
@Composable
fun KeepScreenOnAppPreview() {
    KeepScreenOnTheme {
        KeepScreenOnApp()
    }
}
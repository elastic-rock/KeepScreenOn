package com.elasticrock.keepscreenon

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.elasticrock.keepscreenon.ui.theme.KeepScreenOnTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KeepScreenOnTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    KeepScreenOnApp(activity = this@MainActivity)
                }
            }
        }
    }

    fun isPermissionGranted(): Boolean {
        return Settings.System.canWrite(applicationContext)
    }

    fun requestPermission() {
        startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS))
        recreate()
    }
}

@Composable
fun KeepScreenOnApp(modifier: Modifier = Modifier, activity: MainActivity) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.permission_description))
        if (!activity.isPermissionGranted()) {
            Button(
                onClick = { activity.requestPermission() }
            ) {
                Text(text = stringResource(R.string.grant_permission))
            }
        }
        else {
            Button(onClick = { }, enabled = false) {
                Text(text = stringResource(R.string.permission_granted))
            }
        }
    }
}

@Preview
@Composable
fun KeepScreenOnAppPreview() {
    KeepScreenOnTheme {
        KeepScreenOnApp(activity = MainActivity())
    }
}
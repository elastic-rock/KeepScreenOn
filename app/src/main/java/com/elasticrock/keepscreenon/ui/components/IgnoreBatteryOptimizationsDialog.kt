package com.elasticrock.keepscreenon.ui.components

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.elasticrock.keepscreenon.R

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
package com.elasticrock.keepscreenon.ui.glance

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.size
import com.elasticrock.keepscreenon.R
import com.elasticrock.keepscreenon.data.model.KeepScreenOnState
import com.elasticrock.keepscreenon.data.repository.KeepScreenOnRepository
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

class Widget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val keepScreenOnRepository = hiltEntryPoint.keepScreenOnRepository()
        provideContent {
            GlanceTheme {
                WidgetContent(keepScreenOnRepository)
            }
        }
    }
}

@Composable
fun WidgetContent(keepScreenOnRepository: KeepScreenOnRepository) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keepScreenOnState = keepScreenOnRepository.keepScreenOnState.collectAsState(KeepScreenOnState.Disabled(0,0))
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable {
                when(keepScreenOnState.value) {
                    KeepScreenOnState.PermissionNotGranted -> {
                        context.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply { data = ("package:" + context.packageName).toUri() }.addFlags(FLAG_ACTIVITY_NEW_TASK) )
                    }
                    is KeepScreenOnState.Disabled -> {
                        coroutineScope.launch {
                            keepScreenOnRepository.enableKeepScreenOn(context)
                        }
                    }
                    is KeepScreenOnState.Enabled -> {
                        coroutineScope.launch {
                            keepScreenOnRepository.disableKeepScreenOn(context)
                        }
                    }
                }
            }
            .background(
                when (keepScreenOnState.value) {
                    is KeepScreenOnState.Disabled -> GlanceTheme.colors.background
                    is KeepScreenOnState.Enabled -> GlanceTheme.colors.primary
                    KeepScreenOnState.PermissionNotGranted -> GlanceTheme.colors.errorContainer
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            provider = when (keepScreenOnState.value) {
                is KeepScreenOnState.Disabled -> ImageProvider(R.drawable.outline_lock_clock_qs)
                is KeepScreenOnState.Enabled -> ImageProvider(R.drawable.outline_lock_clock_crossed)
                KeepScreenOnState.PermissionNotGranted -> ImageProvider(R.drawable.outline_error_outline_24)
            },
            contentDescription = "Keep Screen On",
            modifier = GlanceModifier.size(40.dp),
            colorFilter = ColorFilter.tint(
                when (keepScreenOnState.value) {
                    is KeepScreenOnState.Disabled -> GlanceTheme.colors.onBackground
                    is KeepScreenOnState.Enabled -> GlanceTheme.colors.onPrimary
                    KeepScreenOnState.PermissionNotGranted -> GlanceTheme.colors.onErrorContainer
                }
            )
        )
    }
}
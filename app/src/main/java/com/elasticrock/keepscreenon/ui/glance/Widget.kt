//package com.elasticrock.keepscreenon.glance
//
//import android.content.Context
//import android.content.Intent
//import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
//import android.provider.Settings
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.unit.dp
//import androidx.core.net.toUri
//import androidx.glance.ColorFilter
//import androidx.glance.GlanceId
//import androidx.glance.GlanceModifier
//import androidx.glance.GlanceTheme
//import androidx.glance.Image
//import androidx.glance.ImageProvider
//import androidx.glance.action.clickable
//import androidx.glance.appwidget.GlanceAppWidget
//import androidx.glance.appwidget.provideContent
//import androidx.glance.background
//import androidx.glance.layout.Alignment
//import androidx.glance.layout.Column
//import androidx.glance.layout.fillMaxSize
//import androidx.glance.layout.size
//import com.elasticrock.keepscreenon.service.BroadcastReceiverService
//import com.elasticrock.keepscreenon.R
//import com.elasticrock.keepscreenon.data.preferences.PreferencesRepository
//import com.elasticrock.keepscreenon.di.dataStore
//import com.elasticrock.keepscreenon.screenTimeoutState
//import com.elasticrock.keepscreenon.util.CommonUtils
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.launch
//
//class Widget : GlanceAppWidget() {
//    override suspend fun provideGlance(context: Context, id: GlanceId) {
//        val canWrite = Settings.System.canWrite(context)
//        val maximumTimeout = PreferencesRepository(context.dataStore).maximumTimeout.first()
//        provideContent {
//            val coroutineScope = rememberCoroutineScope()
//            val screenTimeout = screenTimeoutState.value!!
//            GlanceTheme {
//                var enabled by remember { mutableStateOf(maximumTimeout == screenTimeoutState.value) }
//                Column(
//                    modifier = GlanceModifier
//                        .fillMaxSize()
//                        .clickable {
//                            if (!canWrite) {
//                                context.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply { data = ("package:" + context.packageName).toUri() }.addFlags(FLAG_ACTIVITY_NEW_TASK) )
//                            } else if (enabled) {
//                                coroutineScope.launch {
//                                    val previousScreenTimeout = PreferencesRepository(context.dataStore).previousScreenTimeout.first()
//                                    CommonUtils().setScreenTimeout(context.contentResolver, previousScreenTimeout)
//                                    enabled = false
//                                    context.stopService(Intent(context, BroadcastReceiverService::class.java))
//                                    screenTimeoutState.value = CommonUtils().readScreenTimeout(context.contentResolver)
//                                }
//                            } else {
//                                coroutineScope.launch {
//                                    CommonUtils().setScreenTimeout(context.contentResolver, maximumTimeout)
//                                    PreferencesRepository(context.dataStore).savePreviousScreenTimeout(screenTimeout)
//                                    enabled = true
//                                    CommonUtils().startBroadcastReceiverService(context)
//                                    screenTimeoutState.value = CommonUtils().readScreenTimeout(context.contentResolver)
//                                }
//                            }
//                        }
//                        .background(
//                            if (!canWrite) GlanceTheme.colors.errorContainer else if (enabled) GlanceTheme.colors.primary else GlanceTheme.colors.background
//                        ),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Image(
//                        provider = if (!canWrite) ImageProvider(R.drawable.outline_error_outline_24) else if (enabled) ImageProvider(R.drawable.outline_lock_clock_crossed) else ImageProvider(R.drawable.outline_lock_clock_qs),
//                        contentDescription = "Keep Screen On",
//                        modifier = GlanceModifier.size(40.dp),
//                        colorFilter = ColorFilter.tint(if (!canWrite) GlanceTheme.colors.onErrorContainer else if (enabled) GlanceTheme.colors.onPrimary else GlanceTheme.colors.onBackground)
//                    )
//                }
//            }
//        }
//    }
//}
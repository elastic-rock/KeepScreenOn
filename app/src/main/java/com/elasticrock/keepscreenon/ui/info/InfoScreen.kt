package com.elasticrock.keepscreenon.ui.info

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.getSystemService
import com.elasticrock.keepscreenon.R
import com.elasticrock.keepscreenon.ui.components.AboutItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(onBackArrowClick: () -> Unit) {
    val context = LocalContext.current
    val clipboard = getSystemService(context, ClipboardManager::class.java) as ClipboardManager

    Scaffold(
        Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.about)) },
                navigationIcon = { IconButton(onClick = onBackArrowClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Go back")
                }
                }
            )
        },
        content = { innerPadding ->
            LazyColumn(contentPadding = innerPadding) {
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
                        val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                        return packageInfo.versionName!!
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
package com.elasticrock.keepscreenon.ui.licenses

import android.content.Intent
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
import androidx.compose.foundation.lazy.items
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
import androidx.core.net.toUri
import com.elasticrock.keepscreenon.R
import com.elasticrock.keepscreenon.ui.components.AboutItem
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(onBackArrowClick: () -> Unit) {
    val context = LocalContext.current

    val libs = Libs.Builder().withContext(context).build()
    val libraries = libs.libraries

    Scaffold(
        Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.third_party_licenses)) },
                navigationIcon = { IconButton(onClick = onBackArrowClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Go back")
                    }
                }
            )
        },
        content = { innerPadding ->
            LazyColumn(contentPadding = innerPadding) {
                items(libraries) { item ->
                    val intent = Intent(Intent.ACTION_VIEW)
                    AboutItem(
                        title = item.name,
                        onClick = {
                            intent.data = item.licenses.first().url?.toUri()
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    )

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
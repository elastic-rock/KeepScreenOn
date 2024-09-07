package com.elasticrock.keepscreenon.ui.licenses

import android.content.Intent
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
import com.elasticrock.keepscreenon.R
import com.elasticrock.keepscreenon.ui.components.AboutItem
import de.philipp_bobek.oss_licenses_parser.OssLicensesParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(onBackArrowClick: () -> Unit) {
    val context = LocalContext.current

    val thirdPartyLicenseMetadataFile = context.resources.openRawResource(R.raw.third_party_license_metadata)
    val thirdPartyLicensesFile = context.resources.openRawResource(R.raw.third_party_licenses)
    val licenses = OssLicensesParser.parseAllLicenses(thirdPartyLicenseMetadataFile, thirdPartyLicensesFile).sortedBy { license -> license.libraryName }

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
                items(licenses) { item ->
                    val intent = Intent(Intent.ACTION_VIEW)
                    AboutItem(
                        title = item.libraryName,
                        onClick = {
                            intent.data = Uri.parse(item.licenseContent)
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
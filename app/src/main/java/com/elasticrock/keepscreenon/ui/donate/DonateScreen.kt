package com.elasticrock.keepscreenon.ui.donate

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.elasticrock.keepscreenon.R
import com.elasticrock.keepscreenon.ui.components.DonationMethod
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonateScreen(
    onBackArrowClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    val layoutDirection = LocalLayoutDirection.current
    val displayCutout = WindowInsets.displayCutout.asPaddingValues()
    val startPadding = displayCutout.calculateStartPadding(layoutDirection)
    val endPadding = displayCutout.calculateEndPadding(layoutDirection)

    Scaffold(
        Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.donate)) },
                modifier = Modifier.padding(start = startPadding, end = endPadding),
                navigationIcon = {
                    IconButton(onClick = onBackArrowClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Go back")
                    }
                }
            )
        },
        content = { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier.padding(start = startPadding, end = endPadding)
            ) {

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.donation_text_1),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Text(
                            text = stringResource(R.string.donation_text_2),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                item {
                    val url = "https://github.com/sponsors/elastic-rock"
                    val intent = Intent(Intent.ACTION_VIEW)
                    DonationMethod(
                        title = stringResource(R.string.credit_card),
                        description = stringResource(R.string.through_github_sponsors),
                        icon = Icons.Filled.CreditCard,
                        onClick = {
                            intent.data = url.toUri()
                            context.startActivity(intent)
                        }
                    )
                }

                item {
                    val walletAddress = "bc1q38cn82smjwtcnj57tukjwmpx23mpwz7jz47rte"
                    val url = "bitcoin:$walletAddress"
                    val intent = Intent(Intent.ACTION_VIEW)
                    val onCopy: () -> Unit = {
                        coroutineScope.launch {
                            val clipData = ClipData.newPlainText("simple text", walletAddress)
                            clipboard.setClipEntry(ClipEntry(clipData))
                        }
                    }
                    DonationMethod(
                        title = stringResource(R.string.bitcoin),
                        description = walletAddress,
                        icon = painterResource(R.drawable.bitcoin_logo_128px),
                        onClick = {
                            intent.data = url.toUri()
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            } else {
                                onCopy
                            }
                        },
                        onCopy = onCopy
                    )
                }

                item {
                    val walletAddress = "0x8C924F0309Bb8d8c11fC468d3AeF9aAc55739278"
                    val url = "ethereum:$walletAddress"
                    val intent = Intent(Intent.ACTION_VIEW)
                    val onCopy: () -> Unit = {
                        coroutineScope.launch {
                            val clipData = ClipData.newPlainText("simple text", walletAddress)
                            clipboard.setClipEntry(ClipEntry(clipData))
                        }
                    }
                    DonationMethod(
                        title = stringResource(R.string.ethereum),
                        description = walletAddress,
                        icon = painterResource(R.drawable.ethereum_logo_128px),
                        onClick = {
                            intent.data = url.toUri()
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            } else {
                                onCopy
                            }
                        },
                        onCopy = onCopy
                    )
                }

                item {
                    val walletAddress = "4B7jCaWktCvjjy6y71ceUoi5UonMyz9b2RnEhqvq7EywBEbMw5Jov4T9tPBxpbbw2SG3uRLiMhxhveMUf1LiXm48LbBavUv"
                    val url = "monero:$walletAddress"
                    val intent = Intent(Intent.ACTION_VIEW)
                    val onCopy: () -> Unit = {
                        coroutineScope.launch {
                            val clipData = ClipData.newPlainText("simple text", walletAddress)
                            clipboard.setClipEntry(ClipEntry(clipData))
                        }
                    }
                    DonationMethod(
                        title = stringResource(R.string.monero),
                        description = walletAddress,
                        icon = painterResource(R.drawable.monero_logo_128px),
                        onClick = {
                            intent.data = url.toUri()
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            } else {
                                onCopy
                            }
                        },
                        onCopy = onCopy
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
package com.elasticrock.keepscreenon.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AboutItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.clickable(
            onClick = onClick
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp, 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .padding(end = 8.dp)
            ) {
                PreferenceItemTitle(text = title)
                if (!subtitle.isNullOrEmpty()) PreferenceItemDescription(
                    text = subtitle
                )
            }
        }
    }
}
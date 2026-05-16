package com.openascend.app.share

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ShareCardFooter(
    tagline: String,
    storeCta: String,
    mutedColor: androidx.compose.ui.graphics.Color,
) {
    Spacer(Modifier.height(8.dp))
    Text(
        tagline,
        color = mutedColor.copy(alpha = 0.9f),
        fontSize = 11.sp,
        lineHeight = 15.sp,
    )
    Text(
        storeCta,
        color = mutedColor.copy(alpha = 0.85f),
        fontSize = 10.sp,
        lineHeight = 14.sp,
    )
}

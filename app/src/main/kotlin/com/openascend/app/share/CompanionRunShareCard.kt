package com.openascend.app.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CompanionRunShareCardUi(
    val heroName: String,
    val gameTitle: String,
    val scoreLine: String,
    val speciesEmoji: String,
    val tagline: String,
    val storeCta: String,
)

@Composable
fun CompanionRunShareCard(payload: CompanionRunShareCardUi) {
    val bg = Color(0xFF12101A)
    val accent = Color(0xFFB39DFF)
    val muted = Color(0xFF9E92B0)
    val main = Color(0xFFF4EEFF)
    Box(
        Modifier
            .fillMaxSize()
            .background(bg)
            .padding(28.dp),
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("OPENASCEND", color = accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
            Text("Companion trial", color = muted, fontSize = 14.sp)
            Text(payload.speciesEmoji, fontSize = 48.sp)
            Text(
                payload.heroName,
                color = main,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                payload.gameTitle,
                color = accent,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                payload.scoreLine,
                color = main,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.weight(1f))
            ShareCardFooter(payload.tagline, payload.storeCta, muted)
        }
    }
}

package com.openascend.app.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BossSealedShareCardUi(
    val heroName: String,
    val bossName: String,
    val targetStat: String,
    val xpAwarded: Int,
    val flavor: String,
    val tagline: String,
    val storeCta: String,
)

@Composable
fun BossSealedShareCardContent(payload: BossSealedShareCardUi) {
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
            Text("Boss sealed", color = muted, fontSize = 14.sp)
            Text(
                payload.heroName,
                color = main,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                payload.bossName,
                color = accent,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "Weak link · ${payload.targetStat}",
                color = muted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                "+${payload.xpAwarded} XP claimed",
                color = main,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                payload.flavor,
                color = muted,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.weight(1f))
            ShareCardFooter(payload.tagline, payload.storeCta, muted)
        }
    }
}

package com.openascend.app.share

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class WeeklyShareCardUi(
    val heroName: String,
    val level: Int = 1,
    val archetypeLine: String = "",
    val recovery: Int,
    val stamina: Int,
    val stability: Int,
    val discipline: Int,
    val vitality: Int,
    val bossName: String,
    val bossFlavor: String,
    val xpHighlights: List<String> = emptyList(),
    val killerStatLine: String? = null,
    @DrawableRes val familiarResId: Int? = null,
    val tagline: String,
    val storeCta: String,
    val disclaimer: String,
)

private val Bg = Color(0xFF12101A)
private val Accent = Color(0xFFB39DFF)
private val Muted = Color(0xFF9E92B0)
private val TextMain = Color(0xFFF4EEFF)

@Composable
fun WeeklyShareCard(payload: WeeklyShareCardUi) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(28.dp),
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "OPENASCEND",
                color = Accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
            )
            Text("Weekly scroll", color = Muted, fontSize = 14.sp)
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        payload.heroName,
                        color = TextMain,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "Level ${payload.level} · ${payload.archetypeLine}",
                        color = Muted,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    payload.killerStatLine?.let { line ->
                        Text(line, color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
                payload.familiarResId?.let { res ->
                    Image(
                        painter = painterResource(res),
                        contentDescription = null,
                        modifier = Modifier.size(88.dp),
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            StatLine("Recovery", payload.recovery)
            StatLine("Stamina", payload.stamina)
            StatLine("Stability", payload.stability)
            StatLine("Discipline", payload.discipline)
            StatLine("Vitality", payload.vitality)
            payload.xpHighlights.take(3).forEach { line ->
                Text(line, color = Muted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Boss · ${payload.bossName}",
                color = Accent,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                payload.bossFlavor,
                color = Muted,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.weight(1f, fill = true))
            ShareCardFooter(payload.tagline, payload.storeCta, Muted)
            Text(
                payload.disclaimer,
                color = Muted.copy(alpha = 0.85f),
                fontSize = 10.sp,
                lineHeight = 14.sp,
            )
        }
    }
}

@Composable
private fun StatLine(label: String, value: Int) {
    Text(
        "$label  $value",
        color = TextMain,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
    )
}

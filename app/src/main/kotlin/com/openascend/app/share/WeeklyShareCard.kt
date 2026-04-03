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

data class WeeklyShareCardUi(
    val heroName: String,
    val recovery: Int,
    val stamina: Int,
    val stability: Int,
    val discipline: Int,
    val vitality: Int,
    val bossName: String,
    val bossFlavor: String,
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "OPENASCEND",
                color = Accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
            )
            Text(
                "Weekly scroll",
                color = Muted,
                fontSize = 14.sp,
            )
            Text(
                payload.heroName,
                color = TextMain,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            StatLine("Recovery", payload.recovery)
            StatLine("Stamina", payload.stamina)
            StatLine("Stability", payload.stability)
            StatLine("Discipline", payload.discipline)
            StatLine("Vitality", payload.vitality)
            Spacer(Modifier.height(12.dp))
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
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.weight(1f, fill = true))
            Text(
                "Your life, scored like a game. Not medical or financial advice.",
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

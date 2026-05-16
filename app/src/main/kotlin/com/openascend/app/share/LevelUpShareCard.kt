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

data class LevelUpShareCardUi(
    val heroName: String,
    val level: Int,
    val archetypeLine: String,
    val compliment: String,
    @DrawableRes val familiarResId: Int? = null,
    val tagline: String,
    val storeCta: String,
)

private val Bg = Color(0xFF12101A)
private val Accent = Color(0xFFB39DFF)
private val Muted = Color(0xFF9E92B0)
private val TextMain = Color(0xFFF4EEFF)

@Composable
fun LevelUpShareCard(payload: LevelUpShareCardUi) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(28.dp),
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("OPENASCEND", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
            Text("Level up", color = Muted, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(
                        payload.heroName,
                        color = TextMain,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "Level ${payload.level}",
                        color = Accent,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(payload.archetypeLine, color = Muted, fontSize = 14.sp)
                }
                payload.familiarResId?.let { res ->
                    Image(
                        painter = painterResource(res),
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                payload.compliment,
                color = TextMain,
                fontSize = 16.sp,
                lineHeight = 22.sp,
            )
            Spacer(Modifier.weight(1f))
            ShareCardFooter(payload.tagline, payload.storeCta, Muted)
        }
    }
}

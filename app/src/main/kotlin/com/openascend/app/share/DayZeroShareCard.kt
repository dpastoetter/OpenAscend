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

data class DayZeroShareCardUi(
    val heroName: String,
    val pathLine: String,
    val speciesLine: String,
    @DrawableRes val familiarResId: Int,
    val tagline: String,
    val storeCta: String,
)

@Composable
fun DayZeroShareCard(payload: DayZeroShareCardUi) {
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
            Text("Day zero chronicle", color = muted, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(
                        payload.heroName,
                        color = main,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(payload.pathLine, color = muted, fontSize = 15.sp)
                    Text(payload.speciesLine, color = accent, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                Image(
                    painter = painterResource(payload.familiarResId),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            ShareCardFooter(payload.tagline, payload.storeCta, muted)
        }
    }
}

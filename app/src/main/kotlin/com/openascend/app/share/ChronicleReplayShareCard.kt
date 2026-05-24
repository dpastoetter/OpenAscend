package com.openascend.app.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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

data class ChronicleReplayShareCardUi(
  val heroName: String,
  val headline: String,
  val dayLines: List<String>,
  val tagline: String,
)

private val Bg = Color(0xFF12101A)
private val Accent = Color(0xFFB39DFF)
private val Muted = Color(0xFF9E92B0)
private val TextMain = Color(0xFFF4EEFF)

@Composable
fun ChronicleReplayShareCard(payload: ChronicleReplayShareCardUi) {
  Box(
    Modifier
      .fillMaxSize()
      .background(Bg)
      .padding(28.dp),
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Text("OPENASCEND", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
      Text("Chronicle replay", color = Muted, fontSize = 14.sp)
      Text(payload.heroName, color = TextMain, fontSize = 26.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
      Text(payload.headline, color = Accent, fontSize = 16.sp, fontWeight = FontWeight.Medium)
      payload.dayLines.take(8).forEach { line ->
        Text(line, color = TextMain, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
      }
      Text(payload.tagline, color = Muted, fontSize = 12.sp)
    }
  }
}

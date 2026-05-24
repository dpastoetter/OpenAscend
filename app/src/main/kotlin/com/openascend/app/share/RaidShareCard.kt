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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class RaidShareCardUi(
    val partyNames: String,
    val bossName: String,
    val outcomeLine: String,
    val powerLine: String,
    val tagline: String,
)

@Composable
fun RaidShareCard(payload: RaidShareCardUi) {
    Box(Modifier.fillMaxSize().background(Color(0xFF12101A)).padding(28.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("OPENASCEND", color = Color(0xFFB39DFF), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text("Party raid", color = Color(0xFF9E92B0), fontSize = 14.sp)
            Text(payload.partyNames, color = Color(0xFFF4EEFF), fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(payload.bossName, color = Color(0xFFB39DFF), fontSize = 16.sp)
            Text(payload.outcomeLine, color = Color(0xFFF4EEFF), fontSize = 15.sp)
            Text(payload.powerLine, color = Color(0xFF9E92B0), fontSize = 13.sp)
            Text(payload.tagline, color = Color(0xFF9E92B0), fontSize = 12.sp)
        }
    }
}

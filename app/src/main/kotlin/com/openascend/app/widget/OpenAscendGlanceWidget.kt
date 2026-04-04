package com.openascend.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.openascend.data.local.prefs.WidgetSnapshotStore

class OpenAscendWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = OpenAscendGlanceWidget()
}

class OpenAscendGlanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetBody()
        }
    }
}

@Composable
private fun WidgetBody() {
    val context = LocalContext.current
    val store = WidgetSnapshotStore(context)
    val muted = ColorProvider(Color(0xFF9E92B0))
    val main = ColorProvider(Color(0xFFF4EEFF))
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            "OPENASCEND",
            style = TextStyle(color = muted, fontWeight = FontWeight.Medium),
        )
        Text(
            "Level ${store.readLevel()}",
            style = TextStyle(color = main, fontWeight = FontWeight.Bold),
        )
        Text(
            text = store.readQuestTitle(),
            style = TextStyle(color = main),
            maxLines = 2,
        )
        Text(
            text = "Boss · ${store.readBossName()}",
            style = TextStyle(color = muted),
            maxLines = 2,
        )
        Text(
            text = store.readFlavorLine(),
            style = TextStyle(color = muted),
            maxLines = 3,
        )
    }
}

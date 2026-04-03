package com.openascend.app.ui.preview

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.openascend.app.share.WeeklyShareCard
import com.openascend.app.share.WeeklyShareCardUi
import com.openascend.app.ui.theme.OpenAscendTheme

@Preview(
    name = "Weekly share card · dark",
    showBackground = true,
    backgroundColor = 0xFF12101A,
    widthDp = 360,
    heightDp = 450,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun PreviewWeeklyShareCard() {
    OpenAscendTheme(dynamicColor = false) {
        Surface(Modifier.fillMaxSize()) {
            WeeklyShareCard(
                WeeklyShareCardUi(
                    heroName = "Asha",
                    recovery = 72,
                    stamina = 81,
                    stability = 64,
                    discipline = 88,
                    vitality = 55,
                    bossName = "The Sleepless Warden",
                    bossFlavor = "A phantom of skipped rest — it feeds on late screens and early alarms.",
                ),
            )
        }
    }
}

@Preview(
    name = "Weekly share card · light",
    showBackground = true,
    widthDp = 360,
    heightDp = 450,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
fun PreviewWeeklyShareCardLight() {
    OpenAscendTheme(darkTheme = false, dynamicColor = false) {
        Surface(Modifier.fillMaxSize()) {
            WeeklyShareCard(
                WeeklyShareCardUi(
                    heroName = "River",
                    recovery = 50,
                    stamina = 50,
                    stability = 50,
                    discipline = 50,
                    vitality = 50,
                    bossName = "The Stillness Colossus",
                    bossFlavor = "Gravity itself seems heavier when movement fades.",
                ),
            )
        }
    }
}

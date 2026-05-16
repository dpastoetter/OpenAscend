package com.openascend.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openascend.app.R
import com.openascend.domain.service.ChronicleCompassDirective
import com.openascend.domain.service.ChronicleCompassKind

@Composable
fun ChronicleCompassCard(
    compass: ChronicleCompassDirective,
    actTitle: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (title, subtitle, cta) = compassCopy(compass)
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        ),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(R.string.home_act_label, actTitle),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
            )
            Button(onClick = onPrimary, modifier = Modifier.fillMaxWidth()) {
                Text(cta)
            }
        }
    }
}

@Composable
private fun compassCopy(
    compass: ChronicleCompassDirective,
): Triple<String, String, String> = when (compass.kind) {
    ChronicleCompassKind.WeeklyReview -> Triple(
        stringResource(R.string.home_compass_weekly_title),
        stringResource(R.string.home_compass_weekly_subtitle),
        stringResource(R.string.home_weekly_review),
    )
    ChronicleCompassKind.EveningCheckIn -> Triple(
        stringResource(R.string.home_compass_evening_title),
        if (compass.habitsOpenCount > 0) {
            stringResource(R.string.home_compass_evening_subtitle_habits, compass.habitsOpenCount)
        } else {
            stringResource(R.string.home_compass_evening_subtitle)
        },
        stringResource(R.string.home_compass_evening_cta),
    )
    ChronicleCompassKind.BossEncounter -> Triple(
        stringResource(R.string.home_compass_boss_title),
        stringResource(R.string.home_compass_boss_subtitle, compass.bossName.orEmpty()),
        stringResource(R.string.home_face_boss),
    )
    ChronicleCompassKind.SealQuest -> Triple(
        stringResource(R.string.home_compass_quest_title),
        stringResource(R.string.home_compass_quest_subtitle, compass.questTitle.orEmpty()),
        stringResource(R.string.home_compass_quest_cta),
    )
    ChronicleCompassKind.Steady -> Triple(
        stringResource(R.string.home_morning_overview),
        if (compass.habitsOpenCount > 0) {
            stringResource(R.string.home_compass_steady_subtitle_habits, compass.habitsOpenCount)
        } else {
            stringResource(R.string.home_compass_steady_subtitle)
        },
        stringResource(R.string.home_compass_steady_cta),
    )
}

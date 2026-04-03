package com.openascend.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.app.ui.components.ProfileAvatar
import com.openascend.domain.model.GameQuest

@Composable
fun HomeScreen(
    onOpenCharacter: () -> Unit,
    onOpenHabits: () -> Unit,
    onOpenCheckIn: () -> Unit,
    onOpenWeekly: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    if (state == null) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val ui = state!!

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            "Morning overview",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ProfileAvatar(
                avatarRelativePath = ui.profile.avatarRelativePath,
                size = 56.dp,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    "Welcome back, ${ui.profile.displayName}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Your life, scored like a game.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            ElevatedAssistChip(onClick = onOpenCheckIn, label = { Text("Evening check-in") })
            AssistChip(onClick = onOpenWeekly, label = { Text("Weekly review") })
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onOpenCharacter) { Text("Character sheet") }
            TextButton(onClick = onOpenHabits) { Text("Habits") }
            TextButton(onClick = onOpenSettings) { Text("Settings") }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Level ${ui.progress.level} · ${ui.progress.archetype.displayName}", fontWeight = FontWeight.SemiBold)
                Text(ui.progress.archetype.tagline, style = MaterialTheme.typography.bodySmall)
                LinearProgressIndicator(
                    progress = {
                        if (ui.progress.xpToNext <= 0) 1f
                        else ui.progress.xpInLevel.toFloat() / (ui.progress.xpInLevel + ui.progress.xpToNext).coerceAtLeast(1)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "XP ${ui.progress.xpInLevel} / ${ui.progress.xpInLevel + ui.progress.xpToNext} · Streak armor ${ui.progress.streakArmor}",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }

        Text("Today's stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        StatRow("Recovery", Icons.Outlined.NightsStay, ui.stats.recovery)
        StatRow("Stamina", Icons.Outlined.FitnessCenter, ui.stats.stamina)
        StatRow("Stability", Icons.Outlined.Payments, ui.stats.stability)
        StatRow("Discipline", Icons.Outlined.TaskAlt, ui.stats.discipline)
        StatRow("Vitality", Icons.Outlined.Spa, ui.stats.vitality)

        Text("Daily quests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        ui.quests.forEach { q ->
            QuestCard(quest = q, onComplete = { viewModel.completeQuest(q) })
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null)
                    Text("Weekly boss", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Text(ui.boss.name, fontWeight = FontWeight.Bold)
                Text(ui.boss.flavor, style = MaterialTheme.typography.bodySmall)
                Text("Weak link: ${ui.boss.targetStat.name}", style = MaterialTheme.typography.labelMedium)
                ui.boss.suggestedActions.forEach { tip ->
                    Text("• $tip", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatRow(
    label: String,
    icon: ImageVector,
    value: Int,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(label, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Text("$value", style = MaterialTheme.typography.labelLarge)
        }
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
        )
    }
}

@Composable
private fun QuestCard(quest: GameQuest, onComplete: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(quest.title, fontWeight = FontWeight.SemiBold)
            Text(quest.description, style = MaterialTheme.typography.bodySmall)
            Text("+${quest.xpReward} XP · ${quest.linkedStat.name}", style = MaterialTheme.typography.labelSmall)
            OutlinedButton(
                onClick = onComplete,
                enabled = !quest.completed,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (quest.completed) "Sealed" else "Mark complete")
            }
        }
    }
}

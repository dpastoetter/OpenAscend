package com.openascend.app.ui.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.app.R
import com.openascend.app.ui.companion.FamiliarStrip
import com.openascend.app.ui.components.ProfileAvatar
import com.openascend.domain.model.CoreStat
import com.openascend.domain.model.GameQuest
import com.openascend.domain.narrative.StatLore

@Composable
fun HomeScreen(
    onOpenCharacter: () -> Unit,
    onOpenHabits: () -> Unit,
    onOpenCheckIn: () -> Unit,
    onOpenWeekly: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenBossRitual: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    if (state == null) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val ui = state!!
    val snack = remember { SnackbarHostState() }
    val sealFlair by viewModel.questSealFlair.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(sealFlair) {
        val msg = sealFlair ?: return@LaunchedEffect
        snack.showSnackbar(msg)
        viewModel.consumeQuestSealFlair()
    }
    LaunchedEffect(ui.levelUpSheet?.newLevel) {
        if (ui.levelUpSheet != null) {
            viewModel.playLevelUpFeedback()
        }
    }
    var loreStat by remember { mutableStateOf<CoreStat?>(null) }
    val archLine = ui.progress.archetype.displayName +
        ui.profile.archetypeSuffix?.let { " · $it" }.orEmpty()

    val levelUp = ui.levelUpSheet
    val suffixPick = ui.suffixPicker
    when {
        levelUp != null -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissLevelUp() },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissLevelUp() }) { Text("Continue") }
                },
                title = { Text("Level ${levelUp.newLevel}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(levelUp.archetypeDisplay, fontWeight = FontWeight.SemiBold)
                        Text(levelUp.compliment, style = MaterialTheme.typography.bodyMedium)
                    }
                },
            )
        }
        suffixPick != null -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissSuffixPicker() },
                title = { Text("Choose your epithet") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "A cosmetic title for level ${suffixPick.bandLevel}.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        suffixPick.choices.forEach { choice ->
                            TextButton(
                                onClick = { viewModel.chooseArchetypeSuffix(choice) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(choice)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissSuffixPicker() }) { Text("Not now") }
                },
            )
        }
    }

    loreStat?.let { st ->
        AlertDialog(
            onDismissRequest = { loreStat = null },
            confirmButton = {
                TextButton(onClick = { loreStat = null }) { Text("Nice") }
            },
            title = { Text(st.name) },
            text = { Text(StatLore.line(st)) },
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
        Text(
            "Morning overview",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            "Act · ${ui.actTitle}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "${ui.actDaysRemaining} days left in this act",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ui.moodHeadline?.let { line ->
            Text(line, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        }
        ui.bossWeekBanner?.let { banner ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            ) {
                Text(
                    banner,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }
        ui.streakArmorChip?.let { chipLine ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(chipLine, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
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
                ui.starterPathLabel?.let { path ->
                    Text(
                        "Path: $path",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        if (ui.familiarEnabled) {
            FamiliarStrip(
                companion = ui.companion,
                species = ui.familiarSpecies,
            )
        }

        if (ui.showOmenCard && ui.omenLine != null) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Today's omen", fontWeight = FontWeight.SemiBold)
                    Text(ui.omenLine, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { viewModel.dismissOmenForToday() }) { Text("Dismiss for today") }
                        FilterChip(
                            selected = ui.omenPinned,
                            onClick = { viewModel.setOmenPinned(!ui.omenPinned) },
                            label = { Text(if (ui.omenPinned) "Pinned" else "Pin") },
                        )
                    }
                }
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

        TextButton(
            onClick = {
                ShareCompat.IntentBuilder(context)
                    .setType("text/plain")
                    .setText(viewModel.buildDailySigilText())
                    .setChooserTitle(context.getString(R.string.home_share_chooser_title))
                    .startChooser()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.home_share_daily_sigil))
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Level ${ui.progress.level} · $archLine", fontWeight = FontWeight.SemiBold)
                Text(ui.progress.archetype.tagline, style = MaterialTheme.typography.bodySmall)
                LinearProgressIndicator(
                    progress = {
                        if (ui.progress.xpToNext <= 0) 1f
                        else ui.progress.xpInLevel.toFloat() / (ui.progress.xpInLevel + ui.progress.xpToNext).coerceAtLeast(1)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Outlined.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        "XP ${ui.progress.xpInLevel} / ${ui.progress.xpInLevel + ui.progress.xpToNext} · Streak armor ${ui.progress.streakArmor}",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }

        Text("Today's stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            "Long-press a stat for lore.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        StatRow("Recovery", Icons.Outlined.NightsStay, ui.stats.recovery, CoreStat.RECOVERY) { loreStat = it }
        StatRow("Stamina", Icons.Outlined.FitnessCenter, ui.stats.stamina, CoreStat.STAMINA) { loreStat = it }
        StatRow("Stability", Icons.Outlined.Payments, ui.stats.stability, CoreStat.STABILITY) { loreStat = it }
        StatRow("Discipline", Icons.Outlined.TaskAlt, ui.stats.discipline, CoreStat.DISCIPLINE) { loreStat = it }
        StatRow("Vitality", Icons.Outlined.Spa, ui.stats.vitality, CoreStat.VITALITY) { loreStat = it }

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
                Text(ui.boss.tell, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text(ui.boss.name, fontWeight = FontWeight.Bold)
                Text(ui.boss.flavor, style = MaterialTheme.typography.bodySmall)
                Text("Weak link: ${ui.boss.targetStat.name}", style = MaterialTheme.typography.labelMedium)
                ui.boss.suggestedActions.forEach { tip ->
                    Text("• $tip", style = MaterialTheme.typography.bodySmall)
                }
                OutlinedButton(onClick = onOpenBossRitual, modifier = Modifier.fillMaxWidth()) {
                    Text("Face the boss")
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        }
        SnackbarHost(
            hostState = snack,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
}

@Composable
private fun StatRow(
    label: String,
    icon: ImageVector,
    value: Int,
    stat: CoreStat,
    onLongPress: (CoreStat) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .pointerInput(stat) {
                detectTapGestures(onLongPress = { onLongPress(stat) })
            },
    ) {
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

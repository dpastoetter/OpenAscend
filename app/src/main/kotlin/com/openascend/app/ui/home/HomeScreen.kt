package com.openascend.app.ui.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.app.R
import com.openascend.app.ui.companion.FamiliarStrip
import com.openascend.app.ui.components.ProfileAvatar
import com.openascend.domain.model.CoreStat
import com.openascend.domain.model.GameQuest
import com.openascend.domain.model.QuestDisplayBonus
import com.openascend.domain.narrative.StatLore
import com.openascend.domain.service.ChronicleCompassKind

@Composable
fun HomeScreen(
    onOpenCharacter: () -> Unit,
    onOpenHabits: () -> Unit,
    onOpenCheckIn: () -> Unit,
    onOpenWeekly: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenBossRitual: () -> Unit,
    onOpenCompanionPlay: () -> Unit = {},
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
    val habitFlair by viewModel.habitSealFlair.collectAsState()
    LaunchedEffect(sealFlair) {
        val msg = sealFlair ?: return@LaunchedEffect
        snack.showSnackbar(msg)
        viewModel.consumeQuestSealFlair()
    }
    LaunchedEffect(habitFlair) {
        val msg = habitFlair ?: return@LaunchedEffect
        snack.showSnackbar(msg)
        viewModel.consumeHabitSealFlair()
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
                    TextButton(onClick = { viewModel.dismissLevelUp() }) {
                        Text(stringResource(R.string.action_continue))
                    }
                },
                title = { Text(stringResource(R.string.home_level_up_title, levelUp.newLevel)) },
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
                title = { Text(stringResource(R.string.home_epithet_title)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            stringResource(R.string.home_epithet_blurb, suffixPick.bandLevel),
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
                    TextButton(onClick = { viewModel.dismissSuffixPicker() }) {
                        Text(stringResource(R.string.action_not_now))
                    }
                },
            )
        }
    }

    loreStat?.let { st ->
        AlertDialog(
            onDismissRequest = { loreStat = null },
            confirmButton = {
                TextButton(onClick = { loreStat = null }) {
                    Text(stringResource(R.string.action_nice))
                }
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ProfileAvatar(
                avatarRelativePath = ui.profile.avatarRelativePath,
                size = 56.dp,
                onClick = onOpenCharacter,
                onClickLabel = stringResource(R.string.home_character_sheet),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.home_welcome_back, ui.profile.displayName),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    stringResource(R.string.home_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ui.starterPathLabel?.let { path ->
                    Text(
                        stringResource(R.string.home_path_label, path),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        ChronicleCompassCard(
            compass = ui.compass,
            actTitle = ui.actTitle,
            onPrimary = {
                when (ui.compass.kind) {
                    ChronicleCompassKind.WeeklyReview -> onOpenWeekly()
                    ChronicleCompassKind.EveningCheckIn -> onOpenCheckIn()
                    ChronicleCompassKind.BossEncounter -> onOpenBossRitual()
                    ChronicleCompassKind.SealQuest -> {
                        ui.quests.firstOrNull { !it.completed }?.let { viewModel.completeQuest(it) }
                    }
                    ChronicleCompassKind.Steady -> {
                        if (ui.compass.habitsOpenCount > 0) {
                            onOpenCheckIn()
                        } else {
                            ui.quests.firstOrNull { !it.completed }?.let { viewModel.completeQuest(it) }
                                ?: onOpenWeekly()
                        }
                    }
                }
            },
        )

        if (ui.familiarEnabled) {
            FamiliarStrip(
                companion = ui.companion,
                species = ui.familiarSpecies,
                dailyBoonAvailable = ui.dailyBoonAvailable,
                memoryWhisper = ui.companionMemoryWhisper,
                onPlayTogether = onOpenCompanionPlay,
            )
        }

        val activeHabits = ui.habits.filter { !it.isRestDay }
        if (activeHabits.isNotEmpty()) {
            Text(
                stringResource(R.string.home_todays_rites),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                activeHabits.forEach { habit ->
                    val done = ui.todayCompletions[habit.id] == true
                    FilterChip(
                        selected = done,
                        onClick = { viewModel.toggleHabit(habit.id, !done) },
                        label = { Text(habit.name) },
                    )
                }
            }
        }

        Text(
            stringResource(R.string.home_more_chronicle),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            AssistChip(onClick = onOpenCheckIn, label = { Text(stringResource(R.string.home_evening_checkin)) })
            AssistChip(onClick = onOpenWeekly, label = { Text(stringResource(R.string.home_weekly_review)) })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onOpenCharacter) {
                Text(stringResource(R.string.home_character_sheet))
            }
            TextButton(onClick = onOpenHabits) { Text(stringResource(R.string.home_habits)) }
            TextButton(onClick = onOpenSettings) { Text(stringResource(R.string.settings_title)) }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.home_level_archetype, ui.progress.level, archLine),
                    fontWeight = FontWeight.SemiBold,
                )
                Text(ui.progress.archetype.tagline, style = MaterialTheme.typography.bodySmall)
                val xpTarget = if (ui.progress.xpToNext <= 0) {
                    1f
                } else {
                    ui.progress.xpInLevel.toFloat() /
                        (ui.progress.xpInLevel + ui.progress.xpToNext).coerceAtLeast(1)
                }
                val animatedXp by animateFloatAsState(
                    targetValue = xpTarget,
                    animationSpec = tween(300),
                    label = "home_xp",
                )
                LinearProgressIndicator(
                    progress = { animatedXp },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Outlined.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        stringResource(
                            R.string.home_xp_streak,
                            ui.progress.xpInLevel,
                            ui.progress.xpInLevel + ui.progress.xpToNext,
                            ui.progress.streakArmor,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }

        Text(
            stringResource(R.string.home_todays_stats),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            stringResource(R.string.home_stats_lore_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        StatRow(
            stringResource(R.string.stat_recovery),
            Icons.Outlined.NightsStay,
            ui.stats.recovery,
            CoreStat.RECOVERY,
        ) { loreStat = it }
        StatRow(
            stringResource(R.string.stat_stamina),
            Icons.Outlined.FitnessCenter,
            ui.stats.stamina,
            CoreStat.STAMINA,
        ) { loreStat = it }
        StatRow(
            stringResource(R.string.stat_stability),
            Icons.Outlined.Payments,
            ui.stats.stability,
            CoreStat.STABILITY,
        ) { loreStat = it }
        StatRow(
            stringResource(R.string.stat_discipline),
            Icons.Outlined.TaskAlt,
            ui.stats.discipline,
            CoreStat.DISCIPLINE,
        ) { loreStat = it }
        StatRow(
            stringResource(R.string.stat_vitality),
            Icons.Outlined.Spa,
            ui.stats.vitality,
            CoreStat.VITALITY,
        ) { loreStat = it }

        Text(
            stringResource(R.string.home_daily_quests),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        ui.quests.forEach { q ->
            QuestCard(quest = q, onComplete = { viewModel.completeQuest(q) })
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null)
                    Text(
                        stringResource(R.string.home_weekly_boss),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(ui.boss.tell, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text(ui.boss.name, fontWeight = FontWeight.Bold)
                Text(ui.boss.flavor, style = MaterialTheme.typography.bodySmall)
                Text(
                    stringResource(R.string.home_weak_link, ui.boss.targetStat.name),
                    style = MaterialTheme.typography.labelMedium,
                )
                if (ui.bossPrepSealsThisWeek > 0) {
                    Text(
                        stringResource(R.string.home_boss_prep_meter, ui.bossPrepSealsThisWeek),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
                ui.boss.suggestedActions.forEach { tip ->
                    Text(
                        stringResource(R.string.home_boss_bullet, tip),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (ui.bossSealedThisWeek) {
                    Text(
                        stringResource(R.string.home_boss_sealed),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                OutlinedButton(onClick = onOpenBossRitual, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (ui.bossSealedThisWeek) {
                            stringResource(R.string.home_review_boss_ritual)
                        } else {
                            stringResource(R.string.home_face_boss)
                        },
                    )
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
            Text(
                stringResource(
                    R.string.home_quest_xp_stat,
                    quest.xpReward,
                    quest.linkedStat.name,
                ) + if (quest.completed) {
                    " · " + stringResource(
                        R.string.home_quest_spotlight_applied,
                        QuestDisplayBonus.PER_SEALED_QUEST,
                    )
                } else {
                    " · " + stringResource(
                        R.string.home_quest_spotlight_pending,
                        QuestDisplayBonus.PER_SEALED_QUEST,
                    )
                },
                style = MaterialTheme.typography.labelSmall,
            )
            OutlinedButton(
                onClick = onComplete,
                enabled = !quest.completed,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (quest.completed) {
                        stringResource(R.string.home_quest_sealed)
                    } else {
                        stringResource(R.string.home_quest_complete)
                    },
                )
            }
        }
    }
}

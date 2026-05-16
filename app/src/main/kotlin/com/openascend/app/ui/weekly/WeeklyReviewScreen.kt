package com.openascend.app.ui.weekly

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.app.R
import com.openascend.app.share.ShareCardCopy
import com.openascend.app.share.ShareLauncher
import com.openascend.app.share.WeeklyShareCardUi
import com.openascend.app.share.captureWeeklyShareCardBitmap
import com.openascend.app.ui.companion.FamiliarPixelDrawable
import com.openascend.domain.companion.CompanionMood
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyReviewScreen(
    onBack: () -> Unit,
    onOpenBossRitual: () -> Unit = {},
    onOpenChronicleDuel: () -> Unit = {},
    viewModel: WeeklyReviewViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weekly review") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (state == null) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        val ui = state!!
        Column(
            Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("7-day roll-up", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(
                "Act · ${ui.actTitle}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Bank vibe this check-in: ${ui.bankLabel}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Recovery ${ui.rolling.recovery} · Stamina ${ui.rolling.stamina} · Stability ${ui.rolling.stability}",
                fontWeight = FontWeight.Medium,
            )
            Text(
                "Discipline ${ui.rolling.discipline} · Vitality ${ui.rolling.vitality}",
                fontWeight = FontWeight.Medium,
            )
            if (ui.xpLedger.total > 0) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            stringResource(R.string.weekly_xp_ledger_title),
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (ui.xpLedger.checkInXp > 0) {
                            Text(stringResource(R.string.weekly_xp_line_checkin, ui.xpLedger.checkInXp))
                        }
                        if (ui.xpLedger.questXp > 0) {
                            Text(stringResource(R.string.weekly_xp_line_quests, ui.xpLedger.questXp))
                        }
                        if (ui.xpLedger.bossXp > 0) {
                            Text(stringResource(R.string.weekly_xp_line_boss, ui.xpLedger.bossXp))
                        }
                        if (ui.xpLedger.companionXp > 0) {
                            Text(stringResource(R.string.weekly_xp_line_companion, ui.xpLedger.companionXp))
                        }
                        if (ui.xpLedger.habitXp > 0) {
                            Text(stringResource(R.string.weekly_xp_line_habits, ui.xpLedger.habitXp))
                        }
                    }
                }
            }
            if (!ui.bossDeferredThisWeek) {
                TextButton(
                    onClick = { viewModel.deferBossToNextWeek() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Defer boss (gentler tale this week — armor thins in the story)")
                }
            } else {
                Text(
                    "Boss encounter deferred this week — you chose a softer chapter.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                TextButton(onClick = { viewModel.clearBossDeferral() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Clear deferral flag")
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Boss encounter", fontWeight = FontWeight.Bold)
                    Text(ui.boss.tell, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Text(ui.boss.name, style = MaterialTheme.typography.titleMedium)
                    Text(ui.boss.flavor, style = MaterialTheme.typography.bodySmall)
                    if (ui.bossSealedThisWeek) {
                        Text(
                            stringResource(R.string.weekly_boss_sealed),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    TextButton(onClick = onOpenBossRitual, modifier = Modifier.fillMaxWidth()) {
                        Text("Open boss ritual")
                    }
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Share card (preview)", fontWeight = FontWeight.SemiBold)
                    Text(ui.shareSummary, style = MaterialTheme.typography.bodySmall)
                }
            }
            TextButton(onClick = onOpenChronicleDuel, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.chronicle_duel_title))
            }
            Button(
                onClick = {
                    val xpLines = buildList {
                        if (ui.xpLedger.checkInXp > 0) {
                            add(context.getString(R.string.weekly_xp_line_checkin, ui.xpLedger.checkInXp))
                        }
                        if (ui.xpLedger.questXp > 0) {
                            add(context.getString(R.string.weekly_xp_line_quests, ui.xpLedger.questXp))
                        }
                        if (ui.xpLedger.bossXp > 0) {
                            add(context.getString(R.string.weekly_xp_line_boss, ui.xpLedger.bossXp))
                        }
                    }
                    val payload = WeeklyShareCardUi(
                        heroName = ui.profile.displayName,
                        level = ui.level,
                        archetypeLine = ui.archetypeLine,
                        recovery = ui.rolling.recovery,
                        stamina = ui.rolling.stamina,
                        stability = ui.rolling.stability,
                        discipline = ui.rolling.discipline,
                        vitality = ui.rolling.vitality,
                        bossName = ui.boss.name,
                        bossFlavor = ui.boss.flavor,
                        xpHighlights = xpLines,
                        killerStatLine = context.getString(
                            R.string.weekly_killer_stat,
                            ui.boss.targetStat.name,
                            ui.rolling.asMap().getValue(ui.boss.targetStat),
                        ),
                        familiarResId = if (ui.familiarEnabled) {
                            FamiliarPixelDrawable.resId(ui.familiarSpecies, CompanionMood.WATCHING)
                        } else {
                            null
                        },
                        tagline = ShareCardCopy.tagline(context),
                        storeCta = ShareCardCopy.storeCta(context),
                        disclaimer = ShareCardCopy.disclaimer(context),
                    )
                    scope.launch {
                        runCatching {
                            val bitmap = captureWeeklyShareCardBitmap(context, payload)
                            ShareLauncher.shareBitmap(
                                context = context,
                                bitmap = bitmap,
                                chooserTitle = context.getString(R.string.share_weekly_chooser),
                                shareText = ui.shareSummary,
                                filePrefix = "openascend_weekly",
                            )
                        }.onFailure {
                            Toast.makeText(
                                context,
                                context.getString(R.string.share_image_error),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.weekly_share_image))
            }
            TextButton(
                onClick = {
                    ShareCompat.IntentBuilder(context)
                        .setType("text/plain")
                        .setText(ui.shareSummary)
                        .setChooserTitle("Share as text")
                        .startChooser()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Share as plain text")
            }
        }
    }
}

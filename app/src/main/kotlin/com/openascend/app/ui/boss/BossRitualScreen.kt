package com.openascend.app.ui.boss

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BossRitualScreen(
    onBack: () -> Unit,
    onOpenWeekly: () -> Unit,
    viewModel: BossRitualViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.boss_ritual_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
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
        val bossCardCd = stringResource(R.string.cd_weekly_boss)
        Column(
            Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(R.string.home_act_label, ui.actTitle),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.weekly_bank_vibe, ui.bankLabel),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (!ui.bossDeferredThisWeek) {
                TextButton(
                    onClick = { viewModel.deferBossToNextWeek() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.weekly_defer_boss))
                }
            } else {
                Text(
                    stringResource(R.string.weekly_deferred_blurb),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                TextButton(onClick = { viewModel.clearBossDeferral() }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.weekly_clear_deferral))
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = bossCardCd
                    },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    RowIconTitle()
                    Text(ui.boss.tell, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Text(ui.boss.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(ui.boss.flavor, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        stringResource(R.string.boss_ritual_weak_link, ui.boss.targetStat.name),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(stringResource(R.string.boss_ritual_suggestions_title), fontWeight = FontWeight.SemiBold)
                    ui.boss.suggestedActions.forEach { tip ->
                        Text("• $tip", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            TextButton(onClick = onOpenWeekly, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.boss_ritual_open_weekly))
            }
        }
    }
}

@Composable
private fun RowIconTitle() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Outlined.AutoAwesome,
            contentDescription = stringResource(R.string.cd_weekly_boss),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(stringResource(R.string.home_weekly_boss), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

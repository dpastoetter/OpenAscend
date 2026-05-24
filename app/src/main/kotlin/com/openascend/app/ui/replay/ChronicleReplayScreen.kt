package com.openascend.app.ui.replay

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.app.R
import com.openascend.app.share.ChronicleReplayShareCardUi
import com.openascend.app.share.ShareCardCopy
import com.openascend.app.share.ShareLauncher
import com.openascend.app.share.captureReplayShareCardBitmap
import com.openascend.domain.service.ChronicleReplayDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

private val replayDateFormat = DateTimeFormatter.ofPattern("MMM d")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChronicleReplayScreen(
    onBack: () -> Unit,
    viewModel: ChronicleReplayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.chronicle_replay_title)) },
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
                Modifier.fillMaxSize().padding(padding),
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                stringResource(R.string.chronicle_replay_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (ui.days.isEmpty()) {
                Text(
                    stringResource(R.string.chronicle_replay_empty),
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            val payload = buildReplayShareCard(context, ui)
                            val bitmap = captureReplayShareCardBitmap(context, payload)
                            val shareText = context.getString(
                                R.string.chronicle_replay_share_text,
                                ui.heroName,
                            )
                            ShareLauncher.shareBitmap(
                                context,
                                bitmap,
                                context.getString(R.string.chronicle_replay_share_chooser),
                                shareText,
                                filePrefix = "openascend_replay",
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.chronicle_replay_share_reel))
                }
            }
            ui.days.forEach { day ->
                ReplayDayCard(day)
            }
        }
    }
}

@Composable
private fun ReplayDayCard(day: ChronicleReplayDay) {
    val date = LocalDate.ofEpochDay(day.epochDay)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(date.format(replayDateFormat), fontWeight = FontWeight.SemiBold)
            Text(
                stringResource(
                    R.string.chronicle_replay_stats_line,
                    day.stats.recovery,
                    day.stats.stamina,
                    day.stats.stability,
                    day.stats.discipline,
                    day.stats.vitality,
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            day.moodHeadline?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                if (day.sealed) {
                    stringResource(R.string.chronicle_replay_sealed)
                } else {
                    stringResource(R.string.chronicle_replay_open)
                },
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

private fun buildReplayShareCard(
    context: android.content.Context,
    ui: ChronicleReplayUiState,
): ChronicleReplayShareCardUi {
    val sealed = ui.days.count { it.sealed }
    val lines = ui.days.takeLast(7).map { day ->
        val date = LocalDate.ofEpochDay(day.epochDay).format(replayDateFormat)
        context.getString(
            R.string.chronicle_replay_share_day_line,
            date,
            day.stats.recovery,
            day.stats.stamina,
        )
    }
    return ChronicleReplayShareCardUi(
        heroName = ui.heroName,
        headline = context.getString(R.string.chronicle_replay_share_headline, sealed, ui.days.size),
        dayLines = lines,
        tagline = ShareCardCopy.tagline(context),
    )
}

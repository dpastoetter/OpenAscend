package com.openascend.app.ui.weekly

import android.content.Intent
import android.graphics.Bitmap.CompressFormat
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
import androidx.core.content.FileProvider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.app.R
import com.openascend.app.share.WeeklyShareCardUi
import com.openascend.app.share.captureWeeklyShareCardBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyReviewScreen(
    onBack: () -> Unit,
    onOpenBossRitual: () -> Unit = {},
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
            Button(
                onClick = {
                    val payload = WeeklyShareCardUi(
                        heroName = ui.profile.displayName,
                        recovery = ui.rolling.recovery,
                        stamina = ui.rolling.stamina,
                        stability = ui.rolling.stability,
                        discipline = ui.rolling.discipline,
                        vitality = ui.rolling.vitality,
                        bossName = ui.boss.name,
                        bossFlavor = ui.boss.flavor,
                    )
                    scope.launch {
                        runCatching {
                            val bitmap = captureWeeklyShareCardBitmap(context, payload)
                            val file = File(context.cacheDir, "openascend_weekly_${System.currentTimeMillis()}.png")
                            try {
                                withContext(Dispatchers.IO) {
                                    FileOutputStream(file).use { out ->
                                        bitmap.compress(CompressFormat.PNG, 100, out)
                                    }
                                }
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file,
                                )
                                ShareCompat.IntentBuilder(context)
                                    .setType("image/png")
                                    .setStream(uri)
                                    .setChooserTitle("Share your card")
                                    .apply {
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    .startChooser()
                            } finally {
                                bitmap.recycle()
                            }
                        }.onFailure {
                            Toast.makeText(context, "Could not build share image", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Share card (image)")
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

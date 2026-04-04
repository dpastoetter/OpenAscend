package com.openascend.app.ui.sigil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.domain.narrative.SigilRitualCopy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SealSigilScreen(
    onFinished: () -> Unit,
    viewModel: SealSigilViewModel = hiltViewModel(),
) {
    val expectedStep by viewModel.expectedStep.collectAsState()
    val wrong by viewModel.showWrongOrder.collectAsState()
    var successLine by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.finished.collect { finish ->
            when (finish) {
                SigilFinish.Success -> {
                    successLine = SigilRitualCopy.successLines().random()
                }
                SigilFinish.Skipped -> onFinished()
            }
        }
    }

    successLine?.let { line ->
        AlertDialog(
            onDismissRequest = {
                successLine = null
                onFinished()
            },
            title = { Text("Sigil sealed") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(line)
                    Text(
                        "Theater for your chronicle only—not therapy, medical, or financial advice.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        successLine = null
                        onFinished()
                    },
                ) {
                    Text("Return home")
                }
            },
        )
    }

    if (wrong) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissWrongHint() },
            title = { Text("Trace broken") },
            text = { Text(SigilRitualCopy.resetHint) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissWrongHint() }) { Text("OK") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(SigilRitualCopy.title) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.skipRitual() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Skip and go back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                SigilRitualCopy.subtitle,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                SigilRitualCopy.orderHint,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            SigilRitualCopy.runeLabels.forEachIndexed { index, label ->
                val isNext = index == expectedStep
                OutlinedButton(
                    onClick = { viewModel.onRuneTapped(index) },
                    enabled = expectedStep < 3,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        buildString {
                            append(index + 1)
                            append(". ")
                            append(label)
                            if (isNext && expectedStep < 3) append(" ← next")
                        },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Money, sleep, and mood entries you sealed earlier are still just a mirror—not professional guidance.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f, fill = true))
            TextButton(
                onClick = { viewModel.skipRitual() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(SigilRitualCopy.skipLabel)
            }
        }
    }
}

package com.openascend.app.ui.sigil

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val scheme = MaterialTheme.colorScheme
    val shake = remember { Animatable(0f) }

    LaunchedEffect(wrong) {
        if (!wrong) return@LaunchedEffect
        shake.snapTo(0f)
        repeat(4) {
            shake.animateTo(12f, tween(40))
            shake.animateTo(-12f, tween(40))
        }
        shake.animateTo(0f, tween(100))
    }

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
            title = { Text(SigilRitualCopy.successDialogTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(line)
                    Text(
                        SigilRitualCopy.disclaimerLine,
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
                    Text(SigilRitualCopy.returnHomeLabel)
                }
            },
        )
    }

    if (wrong) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissWrongHint() },
            title = { Text(SigilRitualCopy.wrongTitle) },
            text = { Text(SigilRitualCopy.resetHint) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissWrongHint() }) { Text(SigilRitualCopy.wrongOk) }
            },
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        SigilRitualCopy.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.skipRitual() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = SigilRitualCopy.skipContentDescription)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            scheme.inverseSurface.copy(alpha = 0.14f),
                            scheme.primary.copy(alpha = 0.18f),
                            scheme.surface,
                        ),
                    ),
                ),
        ) {
            Column(
                Modifier
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .fillMaxSize()
                    .graphicsLayer { translationX = shake.value },
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    SigilRitualCopy.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurface,
                )
                Text(
                    SigilRitualCopy.orderHint,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.primary,
                )
                SigilRitualCopy.runeLabels.forEachIndexed { index, label ->
                    val isNext = index == expectedStep
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = expectedStep < 3) {
                                viewModel.onRuneTapped(index)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isNext && expectedStep < 3) {
                                scheme.primaryContainer.copy(alpha = 0.95f)
                            } else {
                                scheme.surfaceContainerHigh.copy(alpha = 0.88f)
                            },
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (isNext && expectedStep < 3) 6.dp else 1.dp,
                        ),
                        border = if (isNext && expectedStep < 3) {
                            BorderStroke(2.dp, scheme.primary.copy(alpha = 0.65f))
                        } else {
                            null
                        },
                    ) {
                        Text(
                            buildString {
                                append(SigilRitualCopy.runeGlyph)
                                append("  ")
                                append(index + 1)
                                append(". ")
                                append(label)
                                if (isNext && expectedStep < 3) append(SigilRitualCopy.nextMarker)
                            },
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                            color = if (isNext) scheme.onPrimaryContainer else scheme.onSurface,
                            textAlign = TextAlign.Start,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    SigilRitualCopy.footerNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
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
}

package com.openascend.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Onboarding form without Hilt — use in previews and JVM UI tests (Robolectric + Compose).
 */
@Composable
fun OnboardingContent(
    onComplete: (displayName: String, goals: List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    var goalA by remember { mutableStateOf("") }
    var goalB by remember { mutableStateOf("") }

    Column(
        modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Forge your legend", style = MaterialTheme.typography.headlineMedium)
        Text(
            "OpenAscend turns habits and life signals into RPG stats. Nothing here is medical or financial advice — just a playful mirror.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Hero name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = goalA,
            onValueChange = { goalA = it },
            label = { Text("Quest goal #1") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = goalB,
            onValueChange = { goalB = it },
            label = { Text("Quest goal #2 (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.weight(1f))
        Button(
            onClick = {
                onComplete(name, listOf(goalA, goalB))
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Enter the realm")
        }
    }
}

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    OnboardingContent(
        onComplete = { name, goals ->
            viewModel.complete(name, goals, onFinished)
        },
    )
}

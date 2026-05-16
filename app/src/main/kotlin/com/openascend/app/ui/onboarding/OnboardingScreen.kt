package com.openascend.app.ui.onboarding

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.app.R
import com.openascend.app.share.DayZeroShareCardUi
import com.openascend.app.share.ShareCardCopy
import com.openascend.app.share.shareDayZeroCard
import com.openascend.app.ui.companion.FamiliarPixelDrawable
import com.openascend.domain.companion.CompanionMood
import com.openascend.domain.model.FamiliarSpecies
import com.openascend.domain.narrative.StarterPaths

/**
 * Onboarding form without Hilt — use in previews and JVM UI tests (Robolectric + Compose).
 */
@Composable
fun OnboardingContent(
    onComplete: (displayName: String, goals: List<String>, starterPathId: String?, familiarSpecies: FamiliarSpecies) -> Unit,
    modifier: Modifier = Modifier,
) {
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var goalA by remember { mutableStateOf("") }
    var goalB by remember { mutableStateOf("") }
    var starterPathId by remember { mutableStateOf<String?>(null) }
    var familiarSpecies by remember { mutableStateOf(FamiliarSpecies.WOLF) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.onboarding_forge_legend), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(R.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        when (step) {
            0 -> {
                Text(stringResource(R.string.onboarding_step_path), style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.onboarding_hero_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(stringResource(R.string.onboarding_class_fantasy_title), style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = starterPathId == null,
                        onClick = { starterPathId = null },
                        label = { Text(stringResource(R.string.onboarding_class_surprise)) },
                    )
                    StarterPaths.options.forEach { opt ->
                        FilterChip(
                            selected = starterPathId == opt.id,
                            onClick = { starterPathId = opt.id },
                            label = { Text(opt.title) },
                        )
                    }
                }
                Text(stringResource(R.string.onboarding_companion_title), style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FamiliarSpecies.entries.forEach { species ->
                        FilterChip(
                            selected = familiarSpecies == species,
                            onClick = { familiarSpecies = species },
                            label = { Text("${species.emoji} ${species.displayName}") },
                        )
                    }
                }
                Button(
                    onClick = { step = 1 },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_continue))
                }
            }
            1 -> {
                Text(stringResource(R.string.onboarding_step_goals), style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = goalA,
                    onValueChange = { goalA = it },
                    label = { Text(stringResource(R.string.onboarding_goal_1)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = goalB,
                    onValueChange = { goalB = it },
                    label = { Text(stringResource(R.string.onboarding_goal_2)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(onClick = { step = 2 }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.action_continue))
                }
            }
            else -> {
                Text(stringResource(R.string.onboarding_preview_title), style = MaterialTheme.typography.titleLarge)
                Text(
                    stringResource(R.string.onboarding_preview_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(StarterPaths.labelForStoredId(starterPathId).orEmpty())
                Text("${familiarSpecies.emoji} ${familiarSpecies.displayName}")
                TextButton(
                    onClick = {
                        val pathLine = StarterPaths.labelForStoredId(starterPathId) ?: "Surprise path"
                        scope.shareDayZeroCard(
                            context,
                            DayZeroShareCardUi(
                                heroName = name.ifBlank { "Hero" },
                                pathLine = pathLine,
                                speciesLine = "${familiarSpecies.emoji} ${familiarSpecies.displayName}",
                                familiarResId = FamiliarPixelDrawable.resId(familiarSpecies, CompanionMood.CURIOUS),
                                tagline = ShareCardCopy.tagline(context),
                                storeCta = ShareCardCopy.storeCta(context),
                            ),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.onboarding_preview_share))
                }
                Button(
                    onClick = { onComplete(name, listOf(goalA, goalB), starterPathId, familiarSpecies) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.onboarding_preview_enter))
                }
            }
        }
    }
}

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    OnboardingContent(
        onComplete = { name, goals, path, species ->
            viewModel.complete(name, goals, path, species, onFinished)
        },
        modifier = Modifier.fillMaxSize(),
    )
}

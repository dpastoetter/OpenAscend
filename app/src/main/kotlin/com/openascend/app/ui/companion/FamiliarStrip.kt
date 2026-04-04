package com.openascend.app.ui.companion

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openascend.app.R
import com.openascend.domain.companion.FamiliarCopy
import com.openascend.domain.companion.CompanionSnapshot
import com.openascend.domain.model.FamiliarSpecies

/**
 * Compact companion strip: species glyph, mood label, expandable species-aware copy.
 * Expanded: 32×32 pixel sprite (nearest-neighbor scaled) reflects companion mood; see
 * [FamiliarPixelDrawable] and `app/scripts/generate_familiar_sprites.py` for asset naming.
 */
@Composable
fun FamiliarStrip(
    companion: CompanionSnapshot,
    species: FamiliarSpecies,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val styled = remember(companion, species) { FamiliarCopy.stylize(companion, species) }

    val expandCd = stringResource(R.string.cd_expand_companion)
    val collapseCd = stringResource(R.string.cd_collapse_companion)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                stateDescription = if (expanded) collapseCd else expandCd
            }
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ) {
        Column(
            Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(species.emoji, fontSize = 22.sp)
                Text(
                    text = stringResource(R.string.familiar_title, species.displayName, companion.moodLabel),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded) collapseCd else expandCd,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (expanded) {
                val spriteDesc = stringResource(
                    R.string.familiar_pixel_content_description,
                    species.displayName,
                    companion.moodLabel,
                )
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    PixelFamiliar(
                        species = species,
                        mood = companion.mood,
                        contentDescription = spriteDesc,
                    )
                }
                Text(styled.line, style = MaterialTheme.typography.bodyMedium)
                Text(
                    styled.whisper,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

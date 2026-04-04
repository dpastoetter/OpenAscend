package com.openascend.app.ui.companion

import androidx.annotation.DrawableRes
import com.openascend.app.R
import com.openascend.domain.companion.CompanionMood
import com.openascend.domain.model.FamiliarSpecies

/**
 * Maps `(species, mood)` to `drawable-nodpi` PNGs: `familiar_{species.id}_{mood.lowercase()}.png`.
 * Regenerate assets with [app/scripts/generate_familiar_sprites.py].
 */
object FamiliarPixelDrawable {

    @DrawableRes
    fun resId(species: FamiliarSpecies, mood: CompanionMood): Int = when (species) {
        FamiliarSpecies.BEAR -> bear(mood)
        FamiliarSpecies.WOLF -> wolf(mood)
        FamiliarSpecies.DRAGON -> dragon(mood)
    }

    @DrawableRes
    private fun bear(mood: CompanionMood): Int = when (mood) {
        CompanionMood.SPARKLING -> R.drawable.familiar_bear_sparkling
        CompanionMood.COZY -> R.drawable.familiar_bear_cozy
        CompanionMood.WATCHING -> R.drawable.familiar_bear_watching
        CompanionMood.FADING -> R.drawable.familiar_bear_fading
        CompanionMood.CURIOUS -> R.drawable.familiar_bear_curious
        CompanionMood.DORMANT -> R.drawable.familiar_bear_dormant
    }

    @DrawableRes
    private fun wolf(mood: CompanionMood): Int = when (mood) {
        CompanionMood.SPARKLING -> R.drawable.familiar_wolf_sparkling
        CompanionMood.COZY -> R.drawable.familiar_wolf_cozy
        CompanionMood.WATCHING -> R.drawable.familiar_wolf_watching
        CompanionMood.FADING -> R.drawable.familiar_wolf_fading
        CompanionMood.CURIOUS -> R.drawable.familiar_wolf_curious
        CompanionMood.DORMANT -> R.drawable.familiar_wolf_dormant
    }

    @DrawableRes
    private fun dragon(mood: CompanionMood): Int = when (mood) {
        CompanionMood.SPARKLING -> R.drawable.familiar_dragon_sparkling
        CompanionMood.COZY -> R.drawable.familiar_dragon_cozy
        CompanionMood.WATCHING -> R.drawable.familiar_dragon_watching
        CompanionMood.FADING -> R.drawable.familiar_dragon_fading
        CompanionMood.CURIOUS -> R.drawable.familiar_dragon_curious
        CompanionMood.DORMANT -> R.drawable.familiar_dragon_dormant
    }
}

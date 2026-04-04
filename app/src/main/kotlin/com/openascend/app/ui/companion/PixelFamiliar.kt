package com.openascend.app.ui.companion

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import android.graphics.BitmapFactory
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.openascend.domain.companion.CompanionMood
import com.openascend.domain.model.FamiliarSpecies

/** Logical sprite size in pixels (assets are 32×32). */
private const val SpritePx = 32

/** On-screen scale (integer feel, nearest-neighbor). */
private val DisplayDp = (SpritePx * 4).dp

/**
 * Crisp pixel-art familiar: [species] + [mood] sprite with a subtle vertical bob (idle).
 */
@Composable
fun PixelFamiliar(
    species: FamiliarSpecies,
    mood: CompanionMood,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val resId = FamiliarPixelDrawable.resId(species, mood)
    val painter = remember(resId) {
        val bmp = BitmapFactory.decodeResource(context.resources, resId)
        BitmapPainter(bmp.asImageBitmap(), filterQuality = FilterQuality.None)
    }
    val transition = rememberInfiniteTransition(label = "familiar_idle")
    val bobPhase by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bob",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier
                .size(DisplayDp)
                .graphicsLayer { translationY = bobPhase * 4f },
            contentScale = ContentScale.Fit,
        )
    }
}

package com.openascend.app.share

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.Density
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val CARD_WIDTH_PX = 1080
private const val CARD_HEIGHT_PX = 1350
private const val EXPORT_DENSITY = 3f

suspend fun captureWeeklyShareCardBitmap(
    context: Context,
    payload: WeeklyShareCardUi,
): Bitmap = withContext(Dispatchers.Main) {
    val view = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(EXPORT_DENSITY, 1f)) {
                WeeklyShareCard(payload)
            }
        }
    }
    view.measure(
        View.MeasureSpec.makeMeasureSpec(CARD_WIDTH_PX, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(CARD_HEIGHT_PX, View.MeasureSpec.EXACTLY),
    )
    view.layout(0, 0, CARD_WIDTH_PX, CARD_HEIGHT_PX)
    view.drawToBitmap()
}

package com.openascend.app.share

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.Density
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val SHARE_CARD_WIDTH_PX = 1080
const val SHARE_CARD_HEIGHT_PX = 1350
private const val EXPORT_DENSITY = 3f

suspend fun captureShareCardBitmap(
    context: Context,
    content: @Composable () -> Unit,
    widthPx: Int = SHARE_CARD_WIDTH_PX,
    heightPx: Int = SHARE_CARD_HEIGHT_PX,
): Bitmap = withContext(Dispatchers.Main) {
    val view = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(EXPORT_DENSITY, 1f)) {
                content()
            }
        }
    }
    view.measure(
        View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY),
    )
    view.layout(0, 0, widthPx, heightPx)
    view.drawToBitmap()
}

suspend fun captureWeeklyShareCardBitmap(
    context: Context,
    payload: WeeklyShareCardUi,
): Bitmap = captureShareCardBitmap(context, content = { WeeklyShareCard(payload) })

suspend fun captureLevelUpShareCardBitmap(
    context: Context,
    payload: LevelUpShareCardUi,
): Bitmap = captureShareCardBitmap(context, content = { LevelUpShareCard(payload) })

suspend fun captureBossSealedShareCardBitmap(
    context: Context,
    payload: BossSealedShareCardUi,
): Bitmap = captureShareCardBitmap(context, content = { BossSealedShareCardContent(payload) })

suspend fun captureCompanionRunShareCardBitmap(
    context: Context,
    payload: CompanionRunShareCardUi,
): Bitmap = captureShareCardBitmap(context, content = { CompanionRunShareCard(payload) })

suspend fun captureDayZeroShareCardBitmap(
    context: Context,
    payload: DayZeroShareCardUi,
): Bitmap = captureShareCardBitmap(context, content = { DayZeroShareCard(payload) })

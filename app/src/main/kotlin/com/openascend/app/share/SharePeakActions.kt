package com.openascend.app.share

import android.content.Context
import android.widget.Toast
import com.openascend.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun CoroutineScope.shareLevelUpCard(
    context: Context,
    payload: LevelUpShareCardUi,
    onError: () -> Unit = {
        Toast.makeText(context, context.getString(R.string.share_image_error), Toast.LENGTH_SHORT).show()
    },
) {
    launch {
        runCatching {
            val bitmap = captureLevelUpShareCardBitmap(context, payload)
            ShareLauncher.shareBitmap(
                context = context,
                bitmap = bitmap,
                chooserTitle = context.getString(R.string.share_level_up_chooser),
                shareText = context.getString(
                    R.string.share_level_up_text,
                    payload.level,
                    payload.heroName,
                ),
                filePrefix = "openascend_level",
            )
        }.onFailure { onError() }
    }
}

fun CoroutineScope.shareBossSealedCard(
    context: Context,
    payload: BossSealedShareCardUi,
    onError: () -> Unit = {
        Toast.makeText(context, context.getString(R.string.share_image_error), Toast.LENGTH_SHORT).show()
    },
) {
    launch {
        runCatching {
            val bitmap = captureBossSealedShareCardBitmap(context, payload)
            ShareLauncher.shareBitmap(
                context = context,
                bitmap = bitmap,
                chooserTitle = context.getString(R.string.share_boss_chooser),
                shareText = context.getString(
                    R.string.share_boss_sealed_text,
                    payload.bossName,
                    payload.xpAwarded,
                ),
                filePrefix = "openascend_boss",
            )
        }.onFailure { onError() }
    }
}

fun CoroutineScope.shareCompanionRunCard(
    context: Context,
    payload: CompanionRunShareCardUi,
    onError: () -> Unit = {
        Toast.makeText(context, context.getString(R.string.share_image_error), Toast.LENGTH_SHORT).show()
    },
) {
    launch {
        runCatching {
            val bitmap = captureCompanionRunShareCardBitmap(context, payload)
            ShareLauncher.shareBitmap(
                context = context,
                bitmap = bitmap,
                chooserTitle = context.getString(R.string.share_companion_chooser),
                shareText = context.getString(
                    R.string.share_companion_run_text,
                    payload.scoreLine,
                    payload.heroName,
                ),
                filePrefix = "openascend_companion",
            )
        }.onFailure { onError() }
    }
}

fun CoroutineScope.shareDayZeroCard(
    context: Context,
    payload: DayZeroShareCardUi,
    onError: () -> Unit = {
        Toast.makeText(context, context.getString(R.string.share_image_error), Toast.LENGTH_SHORT).show()
    },
) {
    launch {
        runCatching {
            val bitmap = captureDayZeroShareCardBitmap(context, payload)
            ShareLauncher.shareBitmap(
                context = context,
                bitmap = bitmap,
                chooserTitle = context.getString(R.string.share_day_zero_chooser),
                shareText = context.getString(R.string.share_day_zero_text, payload.heroName),
                filePrefix = "openascend_day0",
            )
        }.onFailure { onError() }
    }
}

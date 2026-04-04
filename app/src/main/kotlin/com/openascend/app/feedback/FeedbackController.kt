package com.openascend.app.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.openascend.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackController @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val sealSoundId: Int = soundPool.load(context, R.raw.seal, 1)

    fun playQuestSeal(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) doublePulse(context)
        playSealSound(soundEnabled, volume = 0.42f, rate = 1.06f)
    }

    fun playHabitSeal(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) oneShot(context, durationMs = 30, amplitude = 90)
        playSealSound(soundEnabled, volume = 0.28f, rate = 1f)
    }

    fun playCheckInSeal(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) oneShot(context, durationMs = 48, amplitude = VibrationEffect.DEFAULT_AMPLITUDE)
        playSealSound(soundEnabled, volume = 0.35f, rate = 1f)
    }

    fun playLevelUp(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) triplePulse(context)
        playSealSound(soundEnabled, volume = 0.52f, rate = 1.14f)
    }

    /** Optional post-check-in sigil ritual—distinct from the main check-in seal. */
    fun playSigilRitualComplete(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) sigilPulse(context)
        playSealSound(soundEnabled, volume = 0.33f, rate = 1.02f)
    }

    private fun playSealSound(soundEnabled: Boolean, volume: Float, rate: Float) {
        if (soundEnabled && sealSoundId > 0) {
            soundPool.play(sealSoundId, volume, volume, 1, 0, rate)
        }
    }

    private fun vibrator(context: Context): Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

    private fun oneShot(context: Context, durationMs: Long, amplitude: Int) {
        val v = vibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(durationMs)
        }
    }

    private fun doublePulse(context: Context) {
        val v = vibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 32, 52, 36),
                    intArrayOf(0, 140, 0, 160),
                    -1,
                ),
            )
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(longArrayOf(0, 32, 52, 36), -1)
        }
    }

    private fun triplePulse(context: Context) {
        val v = vibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 28, 40, 28, 40, 34),
                    intArrayOf(0, 160, 0, 140, 0, 180),
                    -1,
                ),
            )
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(longArrayOf(0, 28, 40, 28, 40, 34), -1)
        }
    }

    private fun sigilPulse(context: Context) {
        val v = vibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 22, 36, 22, 36, 26),
                    intArrayOf(0, 120, 0, 120, 0, 150),
                    -1,
                ),
            )
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(longArrayOf(0, 22, 36, 22, 36, 26), -1)
        }
    }
}

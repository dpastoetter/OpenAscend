package com.openascend.app.ui.companion

import com.openascend.app.R

enum class CompanionDailyTrial(val labelRes: Int) {
    TREAT(R.string.companion_trial_treat),
    MEMORY(R.string.companion_trial_memory),
    SEQUENCE(R.string.companion_trial_sequence),
    GLIDE(R.string.companion_trial_glide),
    STACK(R.string.companion_trial_stack),
    THREAD(R.string.companion_trial_thread),
    ;

    companion object {
        fun forEpochDay(epochDay: Long): CompanionDailyTrial =
            entries[(epochDay % entries.size).toInt()]
    }
}

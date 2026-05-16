package com.openascend.app.navigation

object Routes {
    const val Bootstrap = "bootstrap"
    const val Onboarding = "onboarding"
    const val Home = "home"
    const val Character = "character"
    const val Habits = "habits"
    const val HabitEdit = "habit_edit/{habitId}"
    const val CheckIn = "check_in"
    const val Weekly = "weekly"
    const val Settings = "settings"
    const val BossRitual = "boss_ritual"
    const val SealSigil = "seal_sigil"
    const val CompanionHub = "companion_games"
    const val CompanionPlay = "companion_play"
    const val CompanionMemory = "companion_memory"
    const val CompanionSequence = "companion_sequence"
    const val CompanionGlide = "companion_glide"
    const val CompanionStack = "companion_stack"
    const val CompanionThread = "companion_thread"
    const val ChronicleReplay = "chronicle_replay"
    const val ChronicleDuel = "chronicle_duel"

    fun habitEdit(habitId: Long): String = "habit_edit/$habitId"
}

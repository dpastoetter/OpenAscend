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

    fun habitEdit(habitId: Long): String = "habit_edit/$habitId"
}

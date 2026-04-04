package com.openascend.domain.narrative

/** One-minute “class fantasy” picks stored on the profile at onboarding. */
object StarterPaths {

    data class Option(val id: String, val title: String, val blurb: String)

    val options: List<Option> = listOf(
        Option("warden", "Warden", "Sleep and recovery lead your tale."),
        Option("skirmisher", "Skirmisher", "Steps, breath, and momentum first."),
        Option("keeper", "Keeper", "Stability, spending mood, and calm ledgers."),
    )

    fun labelForStoredId(id: String?): String? =
        id?.let { stored -> options.firstOrNull { it.id == stored }?.title }
}

package com.openascend.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ThemePreference {
    /** Follow system light/dark. */
    SYSTEM,
    LIGHT,
    DARK,
}

package com.openascend.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.openascend.domain.model.PrivacySettings
import com.openascend.domain.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.privacyDataStore: DataStore<Preferences> by preferencesDataStore("privacy")

class PrivacyPreferences(
    context: Context,
) {
    private val store = context.applicationContext.privacyDataStore

    private object Keys {
        val analytics = booleanPreferencesKey("analytics_opt_in")
        val crash = booleanPreferencesKey("crash_reports_opt_in")
        val financeHints = booleanPreferencesKey("show_finance_hints")
        val theme = stringPreferencesKey("theme_preference")
    }

    val settings: Flow<PrivacySettings> = store.data.map { p ->
        val themeRaw = p[Keys.theme]
        val theme = themeRaw?.let { runCatching { ThemePreference.valueOf(it) }.getOrNull() }
            ?: ThemePreference.SYSTEM
        PrivacySettings(
            analyticsOptIn = p[Keys.analytics] ?: false,
            crashReportsOptIn = p[Keys.crash] ?: false,
            showFinanceHints = p[Keys.financeHints] ?: true,
            themePreference = theme,
        )
    }

    suspend fun save(settings: PrivacySettings) {
        store.edit { p ->
            p[Keys.analytics] = settings.analyticsOptIn
            p[Keys.crash] = settings.crashReportsOptIn
            p[Keys.financeHints] = settings.showFinanceHints
            p[Keys.theme] = settings.themePreference.name
        }
    }
}

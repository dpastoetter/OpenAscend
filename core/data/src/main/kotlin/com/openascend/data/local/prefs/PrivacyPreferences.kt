package com.openascend.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.openascend.domain.model.FamiliarSpecies
import com.openascend.domain.model.PrivacySettings
import com.openascend.domain.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.privacyDataStore: DataStore<Preferences> by preferencesDataStore("privacy")

data class HomePreferenceSnapshot(
    val settings: PrivacySettings,
    val deferredBossWeekStart: Long?,
    val omenEpochDay: Long?,
    val omenPinned: Boolean,
    val eveningMoodIds: String?,
    val eveningMoodEpochDay: Long?,
    val lastKnownLevel: Int?,
    /** Last calendar day the optional sigil ritual was completed (epoch day). */
    val lastSigilRitualEpochDay: Long?,
    /** Monday epoch day of the week the user sealed the weekly boss ritual (XP awarded once). */
    val bossRitualSealedWeekStart: Long?,
)

class PrivacyPreferences(
    context: Context,
) {
    private val store = context.applicationContext.privacyDataStore

    private object Keys {
        val analytics = booleanPreferencesKey("analytics_opt_in")
        val crash = booleanPreferencesKey("crash_reports_opt_in")
        val theme = stringPreferencesKey("theme_preference")
        val flavorPack = stringPreferencesKey("flavor_pack_id")
        val haptics = booleanPreferencesKey("haptics_enabled")
        val sound = booleanPreferencesKey("sound_enabled")
        val omenEpochDay = longPreferencesKey("omen_epoch_day")
        val omenPinned = booleanPreferencesKey("omen_pinned")
        val eveningMood = stringPreferencesKey("evening_mood_ids")
        val eveningMoodDay = longPreferencesKey("evening_mood_epoch_day")
        val deferredBossWeek = longPreferencesKey("deferred_boss_week_start")
        val lastKnownLevel = intPreferencesKey("last_known_level")
        val familiarEnabled = booleanPreferencesKey("familiar_enabled")
        val familiarSpecies = stringPreferencesKey("familiar_species")
        val healthConnectSync = booleanPreferencesKey("health_connect_sync_enabled")
        val remindersEnabled = booleanPreferencesKey("reminders_enabled")
        val reminderMorning = booleanPreferencesKey("reminder_morning_enabled")
        val reminderEvening = booleanPreferencesKey("reminder_evening_enabled")
        val reminderBoss = booleanPreferencesKey("reminder_boss_enabled")
        val lastSigilRitualEpochDay = longPreferencesKey("last_sigil_ritual_epoch_day")
        val bossRitualSealedWeek = longPreferencesKey("boss_ritual_sealed_week_start")
    }

    private fun readPrivacySettings(p: Preferences): PrivacySettings {
        val themeRaw = p[Keys.theme]
        val theme = themeRaw?.let { runCatching { ThemePreference.valueOf(it) }.getOrNull() }
            ?: ThemePreference.SYSTEM
        return PrivacySettings(
            analyticsOptIn = p[Keys.analytics] ?: false,
            crashReportsOptIn = p[Keys.crash] ?: false,
            themePreference = theme,
            flavorPackId = p[Keys.flavorPack] ?: "default",
            hapticsEnabled = p[Keys.haptics] ?: true,
            soundEnabled = p[Keys.sound] ?: true,
            familiarEnabled = p[Keys.familiarEnabled] ?: false,
            familiarSpecies = FamiliarSpecies.fromId(p[Keys.familiarSpecies]),
            healthConnectSyncEnabled = p[Keys.healthConnectSync] ?: false,
            remindersEnabled = p[Keys.remindersEnabled] ?: false,
            reminderMorningEnabled = p[Keys.reminderMorning] ?: true,
            reminderEveningEnabled = p[Keys.reminderEvening] ?: true,
            reminderBossEnabled = p[Keys.reminderBoss] ?: true,
        )
    }

    val settings: Flow<PrivacySettings> = store.data.map(::readPrivacySettings)

    suspend fun getSettingsSnapshot(): PrivacySettings = store.data.first().let(::readPrivacySettings)

    val omenEpochDay: Flow<Long?> = store.data.map { p -> p[Keys.omenEpochDay] }
    val omenPinned: Flow<Boolean> = store.data.map { p -> p[Keys.omenPinned] ?: false }
    val eveningMoodIds: Flow<String?> = store.data.map { p -> p[Keys.eveningMood] }
    val eveningMoodEpochDay: Flow<Long?> = store.data.map { p -> p[Keys.eveningMoodDay] }
    val deferredBossWeekStart: Flow<Long?> = store.data.map { p -> p[Keys.deferredBossWeek]?.takeIf { it >= 0 } }
    val lastKnownLevel: Flow<Int?> = store.data.map { p -> p[Keys.lastKnownLevel] }

    val homeSnapshot: Flow<HomePreferenceSnapshot> = store.data.map { p ->
        HomePreferenceSnapshot(
            settings = readPrivacySettings(p),
            deferredBossWeekStart = p[Keys.deferredBossWeek]?.takeIf { it >= 0 },
            omenEpochDay = p[Keys.omenEpochDay],
            omenPinned = p[Keys.omenPinned] ?: false,
            eveningMoodIds = p[Keys.eveningMood],
            eveningMoodEpochDay = p[Keys.eveningMoodDay],
            lastKnownLevel = p[Keys.lastKnownLevel],
            lastSigilRitualEpochDay = p[Keys.lastSigilRitualEpochDay]?.takeIf { it >= 0 },
            bossRitualSealedWeekStart = p[Keys.bossRitualSealedWeek]?.takeIf { it >= 0 },
        )
    }

    suspend fun setOmenDismissed(epochDay: Long) {
        store.edit { it[Keys.omenEpochDay] = epochDay }
    }

    suspend fun setOmenPinned(pinned: Boolean) {
        store.edit { it[Keys.omenPinned] = pinned }
    }

    suspend fun setEveningMood(moodIdsCsv: String, epochDay: Long) {
        store.edit {
            it[Keys.eveningMood] = moodIdsCsv
            it[Keys.eveningMoodDay] = epochDay
        }
    }

    suspend fun setDeferredBossWeekStart(weekStartEpochDay: Long?) {
        store.edit {
            if (weekStartEpochDay == null) {
                it.remove(Keys.deferredBossWeek)
            } else {
                it[Keys.deferredBossWeek] = weekStartEpochDay
            }
        }
    }

    suspend fun setLastKnownLevel(level: Int) {
        store.edit { it[Keys.lastKnownLevel] = level }
    }

    suspend fun setLastSigilRitualEpochDay(epochDay: Long) {
        store.edit { it[Keys.lastSigilRitualEpochDay] = epochDay }
    }

    /**
     * Records boss ritual sealed for [weekStartEpochDay] if not already recorded for that week.
     * @return true if this call newly set the seal (caller should award XP once).
     */
    suspend fun markBossRitualSealedIfNew(weekStartEpochDay: Long): Boolean {
        var newlySealed = false
        store.edit { pref ->
            val cur = pref[Keys.bossRitualSealedWeek]?.takeIf { it >= 0 }
            if (cur == weekStartEpochDay) return@edit
            pref[Keys.bossRitualSealedWeek] = weekStartEpochDay
            newlySealed = true
        }
        return newlySealed
    }

    suspend fun save(settings: PrivacySettings) {
        store.edit { p ->
            p[Keys.analytics] = settings.analyticsOptIn
            p[Keys.crash] = settings.crashReportsOptIn
            p[Keys.theme] = settings.themePreference.name
            p[Keys.flavorPack] = settings.flavorPackId
            p[Keys.haptics] = settings.hapticsEnabled
            p[Keys.sound] = settings.soundEnabled
            p[Keys.familiarEnabled] = settings.familiarEnabled
            p[Keys.familiarSpecies] = settings.familiarSpecies.id
            p[Keys.healthConnectSync] = settings.healthConnectSyncEnabled
            p[Keys.remindersEnabled] = settings.remindersEnabled
            p[Keys.reminderMorning] = settings.reminderMorningEnabled
            p[Keys.reminderEvening] = settings.reminderEveningEnabled
            p[Keys.reminderBoss] = settings.reminderBossEnabled
        }
    }
}

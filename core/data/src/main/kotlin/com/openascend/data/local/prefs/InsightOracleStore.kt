package com.openascend.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.insightDataStore: DataStore<Preferences> by preferencesDataStore("insight_oracle")

class InsightOracleStore(context: Context) {
    private val store = context.applicationContext.insightDataStore

    private object Keys {
        val dismissedWeekStart = longPreferencesKey("dismissed_week_start")
        val shownInsightId = stringPreferencesKey("shown_insight_id")
    }

    suspend fun isDismissedForWeek(weekStartEpochDay: Long): Boolean {
        val prefs = store.data.first()
        return prefs[Keys.dismissedWeekStart] == weekStartEpochDay
    }

    suspend fun dismissForWeek(weekStartEpochDay: Long, insightId: String) {
        store.edit {
            it[Keys.dismissedWeekStart] = weekStartEpochDay
            it[Keys.shownInsightId] = insightId
        }
    }

    suspend fun lastShownInsightId(): String? = store.data.first()[Keys.shownInsightId]

    val dismissedWeekFlow = store.data.map { it[Keys.dismissedWeekStart] }
}

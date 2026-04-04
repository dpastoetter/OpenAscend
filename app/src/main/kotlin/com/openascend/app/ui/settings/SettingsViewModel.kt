package com.openascend.app.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.openascend.app.notifications.ReminderWorker
import androidx.lifecycle.viewModelScope
import com.openascend.data.export.UserDataExporter
import com.openascend.data.export.UserDataImporter
import com.openascend.domain.model.FamiliarSpecies
import com.openascend.domain.model.PrivacySettings
import com.openascend.domain.model.ThemePreference
import com.openascend.domain.repository.ProfileRepository
import com.openascend.app.health.HealthConnectBridge
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
    val privacy: PrivacySettings,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val userDataExporter: UserDataExporter,
    private val userDataImporter: UserDataImporter,
    private val healthConnectBridge: HealthConnectBridge,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = profileRepository.observePrivacy()
        .map { SettingsUiState(privacy = it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            SettingsUiState(
                privacy = PrivacySettings(
                    analyticsOptIn = false,
                    crashReportsOptIn = false,
                    themePreference = ThemePreference.SYSTEM,
                    flavorPackId = "default",
                    hapticsEnabled = true,
                    soundEnabled = true,
                    familiarEnabled = false,
                    familiarSpecies = FamiliarSpecies.WOLF,
                    healthConnectSyncEnabled = false,
                    remindersEnabled = false,
                    reminderMorningEnabled = true,
                    reminderEveningEnabled = true,
                    reminderBossEnabled = true,
                ),
            ),
        )

    fun setPrivacy(settings: PrivacySettings) {
        viewModelScope.launch {
            profileRepository.savePrivacy(settings)
            if (settings.remindersEnabled) {
                ReminderWorker.schedule(appContext)
            } else {
                ReminderWorker.cancel(appContext)
            }
        }
    }

    suspend fun exportJson(): String = userDataExporter.buildJson()

    suspend fun exportMarkdownLast30Days(): String = userDataExporter.buildMarkdown(lastDays = 30)

    suspend fun saveBackupToUri(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = userDataExporter.buildJson().toByteArray(Charsets.UTF_8)
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            true
        }.getOrDefault(false)
    }

    suspend fun restoreBackupFromUri(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val text = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.readBytes().toString(Charsets.UTF_8)
            } ?: return@withContext false
            userDataImporter.importFromJsonString(text).isSuccess
        }.getOrDefault(false)
    }

    fun openHealthConnectSettings(context: Context) {
        healthConnectBridge.openHealthConnectManagement(context)
    }
}

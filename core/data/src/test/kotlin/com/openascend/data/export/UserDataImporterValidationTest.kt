package com.openascend.data.export

import com.openascend.domain.model.UserProfile
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class UserDataImporterValidationTest {

    private val validProfile = UserProfile(
        displayName = "Tester",
        onboardingComplete = true,
        goals = emptyList(),
        streakDays = 0,
    )

    @Test
    fun validateExport_acceptsSupportedBackup() {
        UserDataImporter.validateExport(
            OpenAscendExport(
                schemaVersion = 2,
                exportedAtMillis = 1L,
                profile = validProfile,
                habits = emptyList(),
                dailyMetrics = emptyList(),
                xpTotal = 0,
                xpEvents = emptyList(),
            ),
        )
    }

    @Test
    fun validateExport_rejectsMissingProfile() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            UserDataImporter.validateExport(
                OpenAscendExport(
                    schemaVersion = 1,
                    exportedAtMillis = 1L,
                    profile = null,
                    habits = emptyList(),
                    dailyMetrics = emptyList(),
                    xpTotal = 0,
                    xpEvents = emptyList(),
                ),
            )
        }
        assertTrue(error.message!!.contains("profile"))
    }

    @Test
    fun validateExport_rejectsUnsupportedSchema() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            UserDataImporter.validateExport(
                OpenAscendExport(
                    schemaVersion = 99,
                    exportedAtMillis = 1L,
                    profile = validProfile,
                    habits = emptyList(),
                    dailyMetrics = emptyList(),
                    xpTotal = 0,
                    xpEvents = emptyList(),
                ),
            )
        }
        assertTrue(error.message!!.contains("schemaVersion"))
    }
}

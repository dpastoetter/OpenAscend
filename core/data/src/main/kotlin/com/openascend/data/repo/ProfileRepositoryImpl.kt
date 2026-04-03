package com.openascend.data.repo

import com.openascend.data.local.db.ProfileDao
import com.openascend.data.local.mapper.toDomain
import com.openascend.data.local.mapper.toEntity
import com.openascend.data.local.prefs.PrivacyPreferences
import com.openascend.domain.model.PrivacySettings
import com.openascend.domain.model.UserProfile
import com.openascend.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao,
    private val privacyPreferences: PrivacyPreferences,
) : ProfileRepository {

    override fun observeProfile(): Flow<UserProfile> =
        profileDao.observeProfile().map { it?.toDomain() ?: defaultProfile() }

    override suspend fun getProfile(): UserProfile? =
        profileDao.getProfile()?.toDomain() ?: defaultProfile()

    override suspend fun saveProfile(profile: UserProfile) {
        profileDao.upsert(profile.toEntity())
    }

    override fun observePrivacy(): Flow<PrivacySettings> = privacyPreferences.settings

    override suspend fun savePrivacy(settings: PrivacySettings) {
        privacyPreferences.save(settings)
    }

    private fun defaultProfile() = UserProfile(
        displayName = "Traveler",
        onboardingComplete = false,
        goals = emptyList(),
        streakDays = 0,
        lastLoggedEpochDay = null,
        avatarRelativePath = null,
    )
}

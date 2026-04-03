package com.openascend.domain.repository

import com.openascend.domain.model.PrivacySettings
import com.openascend.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfile(): Flow<UserProfile>
    suspend fun getProfile(): UserProfile?
    suspend fun saveProfile(profile: UserProfile)
    fun observePrivacy(): Flow<PrivacySettings>
    suspend fun savePrivacy(settings: PrivacySettings)
}

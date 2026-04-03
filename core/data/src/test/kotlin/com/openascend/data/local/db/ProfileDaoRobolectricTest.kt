package com.openascend.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProfileDaoRobolectricTest {

    private lateinit var db: OpenAscendDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OpenAscendDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsertAndGetProfile() = runBlocking {
        val dao = db.profileDao()
        val entity = ProfileEntity(
            id = 1L,
            displayName = "Traveler",
            onboardingComplete = true,
            goalsJson = "[]",
            streakDays = 3,
            lastLoggedEpochDay = 100L,
            avatarRelativePath = "avatars/profile.jpg",
        )
        dao.upsert(entity)
        assertEquals(entity, dao.getProfile())
    }

    @Test
    fun observeProfile_emitsUpserted() = runBlocking {
        val dao = db.profileDao()
        assertNull(dao.observeProfile().first())
        val entity = ProfileEntity(
            id = 1L,
            displayName = "A",
            onboardingComplete = false,
            goalsJson = "[]",
            streakDays = 0,
            lastLoggedEpochDay = null,
            avatarRelativePath = null,
        )
        dao.upsert(entity)
        assertEquals(entity, dao.observeProfile().first())
    }
}

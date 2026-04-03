package com.openascend.data.local.mapper

import com.openascend.data.local.db.DailyMetricEntity
import com.openascend.data.local.db.HabitEntity
import com.openascend.data.local.db.ProfileEntity
import com.openascend.data.local.db.XpEventEntity
import com.openascend.domain.model.CoreStat
import com.openascend.domain.model.DailyMetric
import com.openascend.domain.model.Habit
import com.openascend.domain.model.UserProfile
import com.openascend.domain.model.XpEvent
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }
private val stringList = ListSerializer(String.serializer())

fun ProfileEntity.toDomain(): UserProfile = UserProfile(
    displayName = displayName,
    onboardingComplete = onboardingComplete,
    goals = runCatching { json.decodeFromString(stringList, goalsJson) }.getOrDefault(emptyList()),
    streakDays = streakDays,
    lastLoggedEpochDay = lastLoggedEpochDay,
    avatarRelativePath = avatarRelativePath,
)

fun UserProfile.toEntity(): ProfileEntity = ProfileEntity(
    displayName = displayName,
    onboardingComplete = onboardingComplete,
    goalsJson = json.encodeToString(stringList, goals),
    streakDays = streakDays,
    lastLoggedEpochDay = lastLoggedEpochDay,
    avatarRelativePath = avatarRelativePath,
)

fun HabitEntity.toDomain(): Habit = Habit(
    id = id,
    name = name,
    frequencyPerWeek = frequencyPerWeek,
    difficulty = difficulty,
    linkedStat = runCatching { CoreStat.valueOf(linkedStat) }.getOrDefault(CoreStat.DISCIPLINE),
)

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    name = name,
    frequencyPerWeek = frequencyPerWeek,
    difficulty = difficulty,
    linkedStat = linkedStat.name,
)

fun DailyMetricEntity.toDomain(): DailyMetric = DailyMetric(
    epochDay = epochDay,
    sleepHours = sleepHours,
    steps = steps,
    bankControlScore = bankControlScore,
    moneyNote = moneyNote,
    vitalityScore = vitalityScore,
)

fun DailyMetric.toEntity(): DailyMetricEntity = DailyMetricEntity(
    epochDay = epochDay,
    sleepHours = sleepHours,
    steps = steps,
    bankControlScore = bankControlScore,
    moneyNote = moneyNote,
    vitalityScore = vitalityScore,
)

fun XpEventEntity.toDomain(): XpEvent = XpEvent(
    id = id,
    timestampMillis = timestampMillis,
    amount = amount,
    reason = reason,
)

package com.openascend.app.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.openascend.domain.health.HealthConnectMetricMerge
import com.openascend.domain.model.PrivacySettings
import com.openascend.domain.repository.MetricsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class HealthConnectMetricsSync @Inject constructor(
    @ApplicationContext private val context: Context,
    private val metricsRepository: MetricsRepository,
) {

    private val readPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
    )

    suspend fun syncIfEnabled(privacy: PrivacySettings) {
        if (!privacy.healthConnectSyncEnabled) return
        if (HealthConnectClient.getSdkStatus(context) != HealthConnectClient.SDK_AVAILABLE) return
        val client = runCatching { HealthConnectClient.getOrCreate(context) }.getOrNull() ?: return
        val granted = client.permissionController.getGrantedPermissions()
        if (!granted.containsAll(readPermissions)) return

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        for (offset in 0L..13L) {
            val date = today.minusDays(offset)
            val start = date.atStartOfDay(zone).toInstant()
            val end = date.plusDays(1).atStartOfDay(zone).toInstant()
            val epoch = date.toEpochDay()
            val steps = readStepsTotal(client, start, end)
            val sleepHours = readSleepHoursTotal(client, start, end)
            val existing = metricsRepository.getDay(epoch)
            val merged = HealthConnectMetricMerge.mergeDay(epoch, existing, sleepHours, steps) ?: continue
            metricsRepository.upsertDay(merged)
        }
    }

    private suspend fun readStepsTotal(client: HealthConnectClient, start: Instant, end: Instant): Int? {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end),
            ),
        )
        val total = response.records.sumOf { it.count.toLong() }.toInt()
        return total.takeIf { it > 0 }
    }

    private suspend fun readSleepHoursTotal(client: HealthConnectClient, start: Instant, end: Instant): Float? {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end),
            ),
        )
        var seconds = 0L
        for (rec in response.records) {
            seconds += java.time.Duration.between(rec.startTime, rec.endTime).seconds
        }
        if (seconds <= 0L) return null
        return (seconds / 3600f * 10f).roundToInt() / 10f
    }
}

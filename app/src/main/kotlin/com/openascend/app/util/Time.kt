package com.openascend.app.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

fun todayEpochDay(): Long = LocalDate.now().toEpochDay()

fun weekStartMondayEpochDay(today: LocalDate = LocalDate.now()): Long =
    today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toEpochDay()

package com.joao.gastos.ui

import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

fun YearMonth.toMonthLabel(locale: Locale = Locale.getDefault()): String {
    val raw = atDay(1).format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
    return raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}

fun Int.toEpochDayLabel(locale: Locale = Locale.getDefault()): String {
    return LocalDate.ofEpochDay(toLong())
        .format(DateTimeFormatter.ofPattern("dd MMM", locale))
}

fun Int.toUtcStartMillis(): Long {
    return LocalDate.ofEpochDay(toLong()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

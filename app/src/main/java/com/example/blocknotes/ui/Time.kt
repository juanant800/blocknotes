package com.example.blocknotes.ui

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val SpanishLocale: Locale = Locale.forLanguageTag("es")
private val zone: ZoneId = ZoneId.systemDefault()

fun dateOf(ms: Long): LocalDate = Instant.ofEpochMilli(ms).atZone(zone).toLocalDate()
fun timeOf(ms: Long): String = Instant.ofEpochMilli(ms).atZone(zone).format(DateTimeFormatter.ofPattern("HH:mm"))
fun fmtDateTime(ms: Long): String = Instant.ofEpochMilli(ms).atZone(zone).format(DateTimeFormatter.ofPattern("d MMM · HH:mm", SpanishLocale))
fun toMillis(d: LocalDate, h: Int, m: Int): Long = d.atTime(h, m).atZone(zone).toInstant().toEpochMilli()
fun dayStart(d: LocalDate): Long = d.atStartOfDay(zone).toInstant().toEpochMilli()

fun relTime(ms: Long): String {
    val d = System.currentTimeMillis() - ms
    return when {
        d < 60_000L -> "ahora"
        d < 3_600_000L -> "hace ${d / 60_000L} min"
        d < 86_400_000L -> "hace ${d / 3_600_000L} h"
        else -> "hace ${d / 86_400_000L} días"
    }
}

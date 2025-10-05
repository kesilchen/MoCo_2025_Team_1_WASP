package io.moxd.mocohands_on.model.ranging.uwb.logparser

import java.io.InputStream
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object UwbLogParser {

    private val lineRegex = Regex(
        """^(\d{4}-\d{2}-\d{2})\s+(\d{2}:\d{2}:\d{2}\.\d{3}).*?\sD\s+(\d{2}:\d{2}):\s+([-\d.]+|null)\s+([-\d.]+|null)\s+([-\d.]+|null)\s*$"""
    )

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    private val systemZone = ZoneId.systemDefault()

    data class TimedSample(
        val timestampMillis: Long,
        val distanceMeters: Double?,
        val azimuthDegrees: Double?,
        val elevationDegrees: Double?
    )

    fun parseSamplesWithTime(input: InputStream, board: String? = null): List<TimedSample> =
        input.bufferedReader().useLines { lines ->
            lines.mapNotNull { line ->
                val match = lineRegex.matchEntire(line) ?: return@mapNotNull null

                val datePart = match.groupValues[1]
                val timePart = match.groupValues[2]
                val boardId = match.groupValues[3]
                if (board != null && boardId != board) return@mapNotNull null

                val timestampMillis = try {
                    LocalDateTime.parse("$datePart $timePart", dateTimeFormatter)
                        .atZone(systemZone)
                        .toInstant()
                        .toEpochMilli()
                } catch (_: Exception) {
                    return@mapNotNull null
                }

                val distance = parseNullableDouble(match.groupValues[4])
                val azimuth = parseNullableDouble(match.groupValues[5])
                val elevation = parseNullableDouble(match.groupValues[6])

                TimedSample(
                    timestampMillis = timestampMillis,
                    distanceMeters = distance,
                    azimuthDegrees = azimuth,
                    elevationDegrees = elevation
                )
            }.toList()
        }

    private fun parseNullableDouble(value: String): Double? =
        if (value.equals("null", ignoreCase = true)) null else value.toDoubleOrNull()
}

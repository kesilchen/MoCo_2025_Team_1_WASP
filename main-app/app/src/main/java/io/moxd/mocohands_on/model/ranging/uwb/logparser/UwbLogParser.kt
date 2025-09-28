package io.moxd.mocohands_on.model.ranging.uwb.logparser

import java.io.InputStream

object UwbLogParser {

    private val lineRegex = Regex(
        """^\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}\.\d{3}.*?\sD\s+(\d{2}:\d{2}):\s+([-\d.]+|null)\s+([-\d.]+|null)\s+([-\d.]+|null)\s*$"""
    )

    fun parseTriplesFromLog(input: InputStream, board: String? = null): List<Triple<Double, Double, Double>> =
        input.bufferedReader().useLines { lines ->
            lines.mapNotNull { line ->
                val matchResult = lineRegex.matchEntire(line) ?: return@mapNotNull null
                val boardId = matchResult.groupValues[1]
                if (board != null && boardId != board) return@mapNotNull null

                val distance  = parseNum(matchResult.groupValues[2])
                val azimuth = parseNum(matchResult.groupValues[3])
                val elevation = parseNum(matchResult.groupValues[4])

                Triple(distance, azimuth, elevation)
            }.toList()
        }

    private fun parseNum(numberString: String): Double =
        if (numberString == "null") Double.NaN else numberString.toDoubleOrNull() ?: Double.NaN
}

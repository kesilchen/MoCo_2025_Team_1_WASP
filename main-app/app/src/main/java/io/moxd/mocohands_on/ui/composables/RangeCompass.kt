package io.moxd.mocohands_on.ui.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Immutable
data class BoardTarget(
    val angleDegrees: Double?,
    val distanceMeters: Double?,
    val color: Color? = null
)

@Composable
fun RangeCompass(
    targets: List<BoardTarget>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f),
    minMarkerSizeDp: Dp = 10.dp,
    maxMarkerSizeDp: Dp = 28.dp,
    maxRangeMeters: Double = 3.0,
    ringStrokeWidthDp: Dp = 2.dp,
    ringColor: Color = MaterialTheme.colorScheme.outline,
    defaultMarkerColor: Color = MaterialTheme.colorScheme.primary,
    activeIndex: Int? = null,
    activeMarkerStrokeWidthDp: Dp = 2.dp
) {
    val density = LocalDensity.current
    val ringStrokeWidthPx = with(density) { ringStrokeWidthDp.toPx() }
    val minMarkerSizePx = with(density) { minMarkerSizeDp.toPx() }
    val maxMarkerSizePx = with(density) { maxMarkerSizeDp.toPx() }
    val tickLengthPx   = with(density) { 10.dp.toPx() }
    val activeMarkerStrokeWidthPx = with(density) { activeMarkerStrokeWidthDp.toPx() }

    val computedTargets = targets.map { target ->
        val proximity = target.distanceMeters?.let { distance ->
            (1.0 - (distance / maxRangeMeters)).coerceIn(0.0, 1.0)
        } ?: 0.0
        val markerSizePx = minMarkerSizePx + (maxMarkerSizePx - minMarkerSizePx) * proximity.toFloat()
        Triple(target, markerSizePx, target.color ?: defaultMarkerColor)
    }

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val compassRadius = min(centerX, centerY) - (maxMarkerSizePx / 2f) - ringStrokeWidthPx

        drawCircle(
            color = ringColor,
            radius = compassRadius,
            center = Offset(centerX, centerY),
            style = Stroke(width = ringStrokeWidthPx)
        )

        listOf(0.0, 90.0, 180.0, 270.0).forEach { angleDegrees ->
            val angleRadians = Math.toRadians(angleDegrees)
            val startX = centerX + (compassRadius - tickLengthPx) * sin(angleRadians).toFloat()
            val startY = centerY - (compassRadius - tickLengthPx) * cos(angleRadians).toFloat()
            val endX = centerX + compassRadius * sin(angleRadians).toFloat()
            val endY = centerY - compassRadius * cos(angleRadians).toFloat()
            drawLine(ringColor, Offset(startX, startY), Offset(endX, endY), strokeWidth = ringStrokeWidthPx)
        }

        computedTargets.forEachIndexed { index, (target, markerSizePx, markerColor) ->
            target.angleDegrees?.let { angleDegrees ->
                val angleRadians = Math.toRadians(angleDegrees)
                val markerCenterX = centerX + compassRadius * sin(angleRadians).toFloat()
                val markerCenterY = centerY - compassRadius * cos(angleRadians).toFloat()
                val halfMarkerSize = markerSizePx / 2f
                val topLeftOffset = Offset(markerCenterX - halfMarkerSize, markerCenterY - halfMarkerSize)
                val markerRectSize = Size(markerSizePx, markerSizePx)

                drawRect(color = markerColor, topLeft = topLeftOffset, size = markerRectSize)

                if (activeIndex != null && index == activeIndex) {
                    drawRect(
                        color = markerColor,
                        topLeft = topLeftOffset,
                        size = markerRectSize,
                        style = Stroke(width = activeMarkerStrokeWidthPx)
                    )
                }
            }
        }
    }
}
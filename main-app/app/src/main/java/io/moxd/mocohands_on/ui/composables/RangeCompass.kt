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
import androidx.core.uwb.UwbAddress
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

@Immutable
data class BoardTarget(
    val address: UwbAddress,
    val angleDegrees: Double?,
    val elevationDegrees: Double?,
    val distanceMeters: Double?,
    val color: Color? = null
)

@Composable
fun RangeCompass(
    targets: List<BoardTarget>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f),
    minMarkerSizeDp: Dp = 12.dp,
    maxMarkerSizeDp: Dp = 36.dp,
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
    val tickLengthPx = with(density) { 10.dp.toPx() }
    val activeMarkerStrokeWidthPx = with(density) { activeMarkerStrokeWidthDp.toPx() }

    val computedTargets = targets.mapIndexed { idx, target ->
        val linear = target.distanceMeters
            ?.let { d -> (1.0 - (d / maxRangeMeters)).coerceIn(0.0, 1.0) }
            ?.toFloat() ?: 0f

        val adjusted = linear.pow(1.8f)

        val markerSizePx = minMarkerSizePx + (maxMarkerSizePx - minMarkerSizePx) * adjusted
        Triple(idx, target, markerSizePx to (target.color ?: defaultMarkerColor))
    }

    val elevationPriority: (BoardTarget) -> Double = { t ->
        abs(t.elevationDegrees ?: Double.POSITIVE_INFINITY)
    }

    val baseOrder = computedTargets
        .sortedWith(compareByDescending<Triple<Int, BoardTarget, Pair<Float, Color>>> {
            elevationPriority(it.second)
        })
        .toMutableList()

    if (activeIndex != null) {
        val i = baseOrder.indexOfFirst { it.first == activeIndex }
        if (i >= 0) {
            val active = baseOrder.removeAt(i)
            baseOrder.add(active)
        }
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

        listOf(0.0, 90.0, 180.0, 270.0).forEach { deg ->
            val rad = Math.toRadians(deg)
            val startX = centerX + (compassRadius - tickLengthPx) * sin(rad).toFloat()
            val startY = centerY - (compassRadius - tickLengthPx) * cos(rad).toFloat()
            val endX = centerX + compassRadius * sin(rad).toFloat()
            val endY = centerY - compassRadius * cos(rad).toFloat()
            drawLine(ringColor, Offset(startX, startY), Offset(endX, endY), strokeWidth = ringStrokeWidthPx)
        }

        baseOrder.forEach { (origIndex, target, sizeAndColor) ->
            val (markerSizePx, markerColor) = sizeAndColor
            target.angleDegrees?.let { angleDegrees ->
                val rad = Math.toRadians(angleDegrees)
                val cx = centerX + compassRadius * sin(rad).toFloat()
                val cy = centerY - compassRadius * cos(rad).toFloat()
                val half = markerSizePx / 2f
                val topLeft = Offset(cx - half, cy - half)
                val rectSize = Size(markerSizePx, markerSizePx)

                drawCircle(
                    color = markerColor.copy(alpha = 0.35f),
                    radius = markerSizePx * 0.95f,
                    center = Offset(cx, cy)
                )

                drawRect(color = markerColor, topLeft = topLeft, size = rectSize)

                drawRect(
                    color = Color.Black.copy(alpha = 0.85f),
                    topLeft = topLeft,
                    size = rectSize,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                if (activeIndex != null && origIndex == activeIndex) {
                    drawRect(
                        color = Color.White.copy(alpha = 0.9f),
                        topLeft = topLeft,
                        size = rectSize,
                        style = Stroke(width = activeMarkerStrokeWidthPx)
                    )
                }
            }
        }
    }
}
package io.moxd.mocohands_on.ui.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import io.moxd.mocohands_on.model.data.RangingReadingDto
import io.moxd.mocohands_on.ui.screens.getColorFromAddress
import kotlinx.coroutines.flow.SharedFlow
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.tan

data class ProjectedBox(
    val visible: Boolean,
    val centerX: Float,
    val centerY: Float,
    val width: Float,
    val height: Float
)

fun projectObjectToScreen(
    angleHDeg: Double,
    angleVDeg: Double,
    dist: Double,
    hfovDeg: Double,
    vfovDeg: Double,
    screenW: Float,
    screenH: Float
): ProjectedBox {
    if (dist <= 0.0) return ProjectedBox(false, 0f, 0f, 0f, 0f)

    val angleH = Math.toRadians(angleHDeg)
    val angleV = Math.toRadians(-angleVDeg)
    val hfov = Math.toRadians(hfovDeg)
    val vfov = Math.toRadians(vfovDeg)

    val f = (screenW / 2.0) / tan(hfov / 2.0) // horizontal focal length in px
    val fv = (screenH / 2.0) / tan(vfov / 2.0) // horizontal focal length in px

    val cx = screenW / 2.0
    val cy = screenH / 2.0

    val xPx = f * tan(angleH) + cx
    val yPx = fv * tan(angleV) + cy

    val baseScale = 100.0
    val size = (baseScale / dist).coerceIn(16.0, 1000.0)

    if (angleHDeg > hfovDeg / 2.0) {
        return ProjectedBox(false, screenW, yPx.toFloat(), size.toFloat(), size.toFloat())
    }
    if (-angleHDeg > hfovDeg / 2.0) {
        return ProjectedBox(false, 0f, yPx.toFloat(), size.toFloat(), size.toFloat())
    }
    if (angleVDeg > Math.toDegrees(vfov) / 2.0) {
        return ProjectedBox(false, xPx.toFloat(), 0f, size.toFloat(), size.toFloat())
    }
    if (-angleVDeg > Math.toDegrees(vfov) / 2.0) {
        return ProjectedBox(false, xPx.toFloat(), screenH, size.toFloat(), size.toFloat())
    }

    return ProjectedBox(
        visible = true,
        centerX = xPx.toFloat(),
        centerY = yPx.toFloat(),
        width = size.toFloat(),
        height = size.toFloat()
    )
}

fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
}

@Composable
fun RangingPovView(targets: List<BoardTarget>, readings: SharedFlow<RangingReadingDto>, onInteract: (target: BoardTarget?) -> Unit) {
    val screenWidth = LocalWindowInfo.current.containerSize.width.toFloat()
    var canvasHeight by remember { mutableFloatStateOf(1000f) }

    var objectInFocus by remember { mutableStateOf(false) }


    LaunchedEffect(readings) {
        readings.collect { reading ->
            if (reading.azimuthDegrees != null && reading.elevationDegrees != null && reading.distanceMeters != null) {
                val box = projectObjectToScreen(
                    angleHDeg = reading.azimuthDegrees - 5,
                    angleVDeg = reading.elevationDegrees,
                    dist = reading.distanceMeters,
                    hfovDeg = 30.0,
                    vfovDeg = 40.0,
                    screenW = screenWidth,
                    screenH = canvasHeight
                )
                val left = box.centerX - box.width / 2
                val top = box.centerY - box.height / 2
                val hCenter = screenWidth / 2
                val vCenter = canvasHeight / 2

                val distanceFromCenter = distance(left, top, hCenter, vCenter)
                objectInFocus = abs(distanceFromCenter) < 350
            }
        }
    }


    val aimToleranceDeg = 10.0

    val activeIndex = targets
        .withIndex()
        .filter { it.value.angleDegrees != null }
        .filter { abs(it.value.angleDegrees!!) <= aimToleranceDeg }
        .minWithOrNull(
            compareBy(
                { abs(it.value.elevationDegrees ?: Double.POSITIVE_INFINITY) },
                { abs(it.value.angleDegrees!!) }
            )
        )
        ?.index

    val isAimingAtBoard = activeIndex != null
    val chosen = activeIndex?.let { targets[it] }

    Box {

        Column {
            Text("Test: ${targets.size}")
            if (targets.isNotEmpty()) {
                val box = projectObjectToScreen(
                    angleHDeg = targets[0].angleDegrees!! - 5,
                    angleVDeg = targets[0].elevationDegrees!! + 12,
                    dist = targets[0].distanceMeters!!,
                    hfovDeg = 30.0,
                    vfovDeg = 50.0,
                    screenW = screenWidth,
                    screenH = canvasHeight
                )
                val left = box.centerX - box.width / 2
                val top = box.centerY - box.height / 2
                Text("Box: $left $top")
                Text("Box: ${box.width} ${box.height}")
                Text("Angle: ${targets[0].angleDegrees}")
                Text("Elevation: ${targets[0].elevationDegrees}")
            }
        }
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { layoutCoordinates ->
                    canvasHeight = layoutCoordinates.size.height.toFloat()
                }) {
            targets.forEach {
                if (it.angleDegrees != null && it.distanceMeters != null && it.elevationDegrees != null) {
                    val box = projectObjectToScreen(
                        angleHDeg = it.angleDegrees - 5,
                        angleVDeg = it.elevationDegrees + 12,
                        dist = it.distanceMeters,
                        hfovDeg = 30.0,
                        vfovDeg = 50.0,
                        screenW = screenWidth,
                        screenH = canvasHeight
                    )
                    val left = box.centerX - box.width / 2
                    val top = box.centerY - box.height / 2

                    drawRect(
                        color = getColorFromAddress(it.address),
                        topLeft = Offset(left, top),
                        size = Size(box.width, box.height),
                        style = if (box.visible) Fill else Stroke(width = 8f)
                    )
                }
            }
            drawLine(
                color = Color.White,
                start = Offset(center.x - 40f, center.y),
                end = Offset(center.x + 40f, center.y),
                strokeWidth = 4f
            )
            drawLine(
                color = Color.White,
                start = Offset(center.x, center.y - 40f),
                end = Offset(center.x, center.y + 40f),
                strokeWidth = 4f
            )
        }
        if (isAimingAtBoard) {
            Button(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 15.dp),
                onClick = {onInteract(chosen)}
            ) {
                Text("Interact")
            }
        }
    }
}
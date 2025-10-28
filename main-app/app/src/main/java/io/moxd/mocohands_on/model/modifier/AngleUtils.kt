package io.moxd.mocohands_on.model.modifier

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

internal fun circularMeanDegrees(values: Collection<Double>): Double {
    var x = 0.0
    var y = 0.0
    for (degrees in values) {
        val radians = Math.toRadians(degrees)
        x += cos(radians)
        y += sin(radians)
    }
    return Math.toDegrees(atan2(y, x))
}

internal fun minimalAngularDifferenceDegrees(angle: Double, reference: Double): Double {
    val diff = angle - reference
    val normalized = ((diff + 540.0) % 360.0) - 180.0
    return normalized
}

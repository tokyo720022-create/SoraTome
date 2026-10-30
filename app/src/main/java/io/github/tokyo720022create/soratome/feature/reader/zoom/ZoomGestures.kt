package io.github.tokyo720022create.soratome.feature.reader.zoom

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import kotlin.math.abs

/**
 * Pinch to zoom, and drag to move while zoomed.
 *
 * It looks at touches before the content does, but only takes them when two fingers are
 * down or the view is already zoomed. A single finger at normal size passes through, so
 * swiping between pages and scrolling still work.
 *
 * @param key Restarts the gesture handling when it changes, so [onOverflow] stays current.
 * @param onOverflow Called with the part of a drag that did not fit because the view hit
 * its edge (used to keep scrolling a vertical list while zoomed).
 */
fun Modifier.zoomGestures(
    state: ZoomState,
    key: Any? = null,
    onOverflow: (Offset) -> Unit = {},
): Modifier = pointerInput(state, key) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)

        val touchSlop = viewConfiguration.touchSlop
        var pastSlop = false
        var zoomSoFar = 1f
        var panSoFar = Offset.Zero

        do {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val canceled = event.changes.any { it.isConsumed }

            if (!canceled) {
                val zoomChange = event.calculateZoom()
                val panChange = event.calculatePan()
                val fingersDown = event.changes.count { it.pressed }

                if (!pastSlop) {
                    zoomSoFar *= zoomChange
                    panSoFar += panChange
                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = abs(1f - zoomSoFar) * centroidSize
                    if (zoomMotion > touchSlop || panSoFar.getDistance() > touchSlop) {
                        pastSlop = true
                    }
                }

                if (pastSlop && (fingersDown > 1 || state.isZoomed)) {
                    if (zoomChange != 1f || panChange != Offset.Zero) {
                        val centroid = event.calculateCentroid(useCurrent = false)
                        val overflow = state.transform(centroid, panChange, zoomChange, size)
                        if (overflow != Offset.Zero) onOverflow(overflow)
                    }
                    event.changes.forEach { change ->
                        if (change.positionChanged()) change.consume()
                    }
                }
            }
        } while (!canceled && event.changes.any { it.pressed })
    }
}

/** Calls [onDown] whenever a finger touches the content. It never takes the touch. */
fun Modifier.onTouchDown(onDown: () -> Unit): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        onDown()
        do {
            val event = awaitPointerEvent(PointerEventPass.Initial)
        } while (event.changes.any { it.pressed })
    }
}

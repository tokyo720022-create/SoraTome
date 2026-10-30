package io.github.tokyo720022create.soratome.feature.reader.zoom

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

/**
 * Zoom level and position of the reader content.
 * The content is scaled around its centre and then moved by [translation].
 */
@Stable
class ZoomState {

    var scale by mutableFloatStateOf(1f)
        private set

    var translation by mutableStateOf(Offset.Zero)
        private set

    val isZoomed: Boolean get() = scale > ZOOMED_THRESHOLD

    fun reset() {
        scale = 1f
        translation = Offset.Zero
    }

    /**
     * Applies one pinch/drag step. [centroid] is the point between the fingers in the
     * container, [pan] how far it moved and [zoom] the scale change.
     * Returns the part of [pan] that did not fit because the view reached its edge.
     */
    fun transform(centroid: Offset, pan: Offset, zoom: Float, size: IntSize): Offset {
        val newScale = (scale * zoom).coerceIn(1f, MAX_SCALE)
        val effectiveZoom = newScale / scale
        val center = Offset(size.width / 2f, size.height / 2f)

        // Keep the point under the fingers where it is, then add the drag.
        val moved = (centroid - center) * (1f - effectiveZoom) + translation * effectiveZoom + pan

        val maxX = (newScale - 1f) * size.width / 2f
        val maxY = (newScale - 1f) * size.height / 2f
        val clamped = Offset(moved.x.coerceIn(-maxX, maxX), moved.y.coerceIn(-maxY, maxY))

        scale = newScale
        translation = clamped
        return moved - clamped
    }

    /** Double tap: zoom in around [at], or back out if already zoomed. */
    fun toggle(at: Offset, size: IntSize) {
        if (isZoomed) reset() else transform(at, Offset.Zero, DOUBLE_TAP_SCALE, size)
    }

    companion object {
        const val MAX_SCALE = 4f
        const val DOUBLE_TAP_SCALE = 2.5f
        private const val ZOOMED_THRESHOLD = 1.01f
    }
}

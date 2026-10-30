package io.github.tokyo720022create.soratome.feature.reader

/**
 * Lets the reader receive the volume keys. MainActivity passes volume key presses to
 * [onVolumeKey] while it is set (the reader sets it only when "Volume keys turn pages" is on).
 */
object ReaderKeyEvents {
    /** Called with true for volume down (go forward) and false for volume up (go back). */
    @Volatile
    var onVolumeKey: ((forward: Boolean) -> Unit)? = null
}

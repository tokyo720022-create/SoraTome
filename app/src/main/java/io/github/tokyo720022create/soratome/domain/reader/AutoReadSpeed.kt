package io.github.tokyo720022create.soratome.domain.reader

/**
 * Auto read speeds.
 * @property pageSeconds Seconds on each page in the page-by-page modes.
 * @property scrollDpPerSecond Scroll speed in the vertical mode.
 */
enum class AutoReadSpeed(val label: String, val pageSeconds: Int, val scrollDpPerSecond: Float) {
    VerySlow("Very slow", 10, 30f),
    Slow("Slow", 7, 55f),
    Medium("Medium", 5, 90f),
    Fast("Fast", 3, 140f),
    VeryFast("Very fast", 2, 220f),
}

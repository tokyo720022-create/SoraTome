package io.github.tokyo720022create.soratome.plugin.api.source

/**
 * One page image of a chapter.
 *
 * @property url Full-quality image address.
 * @property lowQualityUrl Smaller "data saver" version, if the source offers one.
 * @property reportUrl If set, the app reports how each load went (success, size, time)
 * to this address. Some image networks ask clients to do this.
 */
data class PageInfo(
    val index: Int,
    val url: String,
    val lowQualityUrl: String? = null,
    val reportUrl: String? = null,
)

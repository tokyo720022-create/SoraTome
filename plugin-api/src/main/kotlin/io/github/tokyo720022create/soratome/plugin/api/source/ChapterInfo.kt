package io.github.tokyo720022create.soratome.plugin.api.source

/**
 * @property url Web address of the chapter, used for "Share chapter link". Optional.
 */
data class ChapterInfo(
    val id: String,
    val name: String,
    val number: Float? = null,
    val dateMillis: Long? = null,
    val url: String? = null,
)

package io.github.tokyo720022create.soratome.plugin.api.source

data class ChapterInfo(
    val id: String,
    val name: String,
    val number: Float? = null,
    val dateMillis: Long? = null,
)

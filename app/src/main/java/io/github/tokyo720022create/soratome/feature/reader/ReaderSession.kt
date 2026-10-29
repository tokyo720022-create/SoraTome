package io.github.tokyo720022create.soratome.feature.reader

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import io.github.tokyo720022create.soratome.plugin.api.source.ChapterInfo
import io.github.tokyo720022create.soratome.plugin.api.source.SourcePlugin

/**
 * The chapter list being read and the position in it.
 * [chapters] are in reading order (first chapter first).
 */
class ReaderSession(
    val plugin: SourcePlugin,
    val seriesTitle: String,
    val chapters: List<ChapterInfo>,
    startIndex: Int,
) {
    var chapterIndex by mutableIntStateOf(startIndex.coerceIn(0, chapters.lastIndex))
        private set

    val chapter: ChapterInfo get() = chapters[chapterIndex]
    val hasNext: Boolean get() = chapterIndex < chapters.lastIndex
    val hasPrevious: Boolean get() = chapterIndex > 0

    fun goToNext() {
        if (hasNext) chapterIndex++
    }

    fun goToPrevious() {
        if (hasPrevious) chapterIndex--
    }
}

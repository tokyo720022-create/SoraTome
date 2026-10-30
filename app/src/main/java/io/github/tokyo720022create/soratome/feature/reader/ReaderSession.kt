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

    /** With [skipExtras], chapters that have no number (extras, side stories) are passed over. */
    fun hasNext(skipExtras: Boolean): Boolean = indexAfter(skipExtras) != null

    fun hasPrevious(skipExtras: Boolean): Boolean = indexBefore(skipExtras) != null

    fun goToNext(skipExtras: Boolean) {
        indexAfter(skipExtras)?.let { chapterIndex = it }
    }

    fun goToPrevious(skipExtras: Boolean) {
        indexBefore(skipExtras)?.let { chapterIndex = it }
    }

    private fun indexAfter(skipExtras: Boolean): Int? {
        var index = chapterIndex + 1
        while (index <= chapters.lastIndex) {
            if (!skipExtras || chapters[index].number != null) return index
            index++
        }
        return null
    }

    private fun indexBefore(skipExtras: Boolean): Int? {
        var index = chapterIndex - 1
        while (index >= 0) {
            if (!skipExtras || chapters[index].number != null) return index
            index--
        }
        return null
    }
}

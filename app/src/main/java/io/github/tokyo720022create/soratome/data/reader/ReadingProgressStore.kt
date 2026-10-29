package io.github.tokyo720022create.soratome.data.reader

import android.content.Context

/**
 * Remembers the last page read and finished chapters, per plugin and chapter.
 * Kept in SharedPreferences for now; it moves into the library database later.
 */
class ReadingProgressStore(context: Context) {

    private val prefs = context.getSharedPreferences("reading_progress", Context.MODE_PRIVATE)

    fun lastPage(pluginId: String, chapterId: String): Int? {
        val page = prefs.getInt(pageKey(pluginId, chapterId), -1)
        return if (page >= 0) page else null
    }

    fun isFinished(pluginId: String, chapterId: String): Boolean =
        prefs.getBoolean(finishedKey(pluginId, chapterId), false)

    fun savePage(pluginId: String, chapterId: String, page: Int) {
        prefs.edit().putInt(pageKey(pluginId, chapterId), page).apply()
    }

    fun markFinished(pluginId: String, chapterId: String) {
        prefs.edit().putBoolean(finishedKey(pluginId, chapterId), true).apply()
    }

    private fun pageKey(pluginId: String, chapterId: String) = "page|$pluginId|$chapterId"

    private fun finishedKey(pluginId: String, chapterId: String) = "done|$pluginId|$chapterId"
}

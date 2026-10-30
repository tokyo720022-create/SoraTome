package io.github.tokyo720022create.soratome.data.reader

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.tokyo720022create.soratome.domain.reader.ChapterNaming
import io.github.tokyo720022create.soratome.domain.reader.ChapterOrder
import io.github.tokyo720022create.soratome.domain.reader.ChapterSort

/** How the chapter list on the series page is sorted, named and filtered. */
class ChapterListPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("chapter_list", Context.MODE_PRIVATE)

    var sort by mutableStateOf(
        ChapterSort.entries.firstOrNull { it.name == prefs.getString(KEY_SORT, null) } ?: ChapterSort.BySource,
    )
        private set

    var order by mutableStateOf(
        ChapterOrder.entries.firstOrNull { it.name == prefs.getString(KEY_ORDER, null) }
            ?: ChapterOrder.NewestFirst,
    )
        private set

    var naming by mutableStateOf(
        ChapterNaming.entries.firstOrNull { it.name == prefs.getString(KEY_NAMING, null) }
            ?: ChapterNaming.SourceTitle,
    )
        private set

    var onlyUnread by mutableStateOf(prefs.getBoolean(KEY_ONLY_UNREAD, false))
        private set

    fun updateSort(value: ChapterSort) {
        sort = value
        prefs.edit().putString(KEY_SORT, value.name).apply()
    }

    fun updateOrder(value: ChapterOrder) {
        order = value
        prefs.edit().putString(KEY_ORDER, value.name).apply()
    }

    fun updateNaming(value: ChapterNaming) {
        naming = value
        prefs.edit().putString(KEY_NAMING, value.name).apply()
    }

    fun updateOnlyUnread(value: Boolean) {
        onlyUnread = value
        prefs.edit().putBoolean(KEY_ONLY_UNREAD, value).apply()
    }

    private companion object {
        const val KEY_SORT = "sort"
        const val KEY_ORDER = "order"
        const val KEY_NAMING = "naming"
        const val KEY_ONLY_UNREAD = "only_unread"
    }
}

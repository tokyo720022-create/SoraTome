package io.github.tokyo720022create.soratome

import android.app.Application
import io.github.tokyo720022create.soratome.data.image.PageImageLoader
import io.github.tokyo720022create.soratome.data.plugins.PluginManager
import io.github.tokyo720022create.soratome.data.reader.ChapterListPreferences
import io.github.tokyo720022create.soratome.data.reader.ReaderPreferences
import io.github.tokyo720022create.soratome.data.reader.ReadingProgressStore
import io.github.tokyo720022create.soratome.feature.browse.BrowseState
import io.github.tokyo720022create.soratome.feature.reader.ReaderSession
import io.github.tokyo720022create.soratome.plugin.api.source.SourcePlugin

class SoraTomeApplication : Application() {

    /** Installs, updates and removes plugins, and keeps the list of repositories. */
    val pluginManager: PluginManager by lazy { PluginManager(this) }

    /** The source plugins that are switched on. */
    val plugins: List<SourcePlugin> get() = pluginManager.enabledPlugins

    val imageLoader: PageImageLoader by lazy { PageImageLoader() }
    val readerPreferences: ReaderPreferences by lazy { ReaderPreferences(this) }
    val progressStore: ReadingProgressStore by lazy { ReadingProgressStore(this) }
    val chapterListPreferences: ChapterListPreferences by lazy { ChapterListPreferences(this) }

    /** Search and selection state of the Browse tab, kept while the user moves between screens. */
    val browseState = BrowseState()

    /** The chapter list being read. Set before opening the reader. */
    var readerSession: ReaderSession? = null
}

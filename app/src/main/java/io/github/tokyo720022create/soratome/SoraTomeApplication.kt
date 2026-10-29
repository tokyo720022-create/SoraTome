package io.github.tokyo720022create.soratome

import android.app.Application
import android.util.Log
import io.github.tokyo720022create.soratome.data.image.PageImageLoader
import io.github.tokyo720022create.soratome.data.reader.ReaderPreferences
import io.github.tokyo720022create.soratome.data.reader.ReadingProgressStore
import io.github.tokyo720022create.soratome.feature.browse.BrowseState
import io.github.tokyo720022create.soratome.feature.reader.ReaderSession
import io.github.tokyo720022create.soratome.plugin.api.source.SourcePlugin
import io.github.tokyo720022create.soratome.plugin.runtime.loader.AssetPluginLoader

class SoraTomeApplication : Application() {

    /** Installed source plugins, loaded the first time they are needed. */
    val plugins: List<SourcePlugin> by lazy {
        AssetPluginLoader(this) { message -> Log.d("SoraTome", message) }.loadAll()
    }

    val imageLoader: PageImageLoader by lazy { PageImageLoader() }
    val readerPreferences: ReaderPreferences by lazy { ReaderPreferences(this) }
    val progressStore: ReadingProgressStore by lazy { ReadingProgressStore(this) }

    /** Search and selection state of the Browse tab, kept while the user moves between screens. */
    val browseState = BrowseState()

    /** The chapter list being read. Set before opening the reader. */
    var readerSession: ReaderSession? = null
}

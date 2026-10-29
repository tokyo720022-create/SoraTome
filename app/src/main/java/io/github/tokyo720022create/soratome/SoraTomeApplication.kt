package io.github.tokyo720022create.soratome

import android.app.Application
import android.util.Log
import io.github.tokyo720022create.soratome.plugin.api.SourcePlugin
import io.github.tokyo720022create.soratome.plugin.runtime.AssetPluginLoader

class SoraTomeApplication : Application() {

    /** Installed source plugins, loaded the first time they are needed. */
    val plugins: List<SourcePlugin> by lazy {
        AssetPluginLoader(this) { message -> Log.d("SoraTome", message) }.loadAll()
    }
}

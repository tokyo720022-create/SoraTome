package io.github.tokyo720022create.soratome.plugin.runtime.error

/** Base class for every error raised while loading or running a plugin. */
open class PluginException(message: String, cause: Throwable? = null) : Exception(message, cause)

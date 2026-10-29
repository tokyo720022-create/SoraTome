package io.github.tokyo720022create.soratome.plugin.runtime

/** Base class for every error raised while loading or running a plugin. */
open class PluginException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Raised when a plugin tries to do something its manifest does not allow. */
class PluginSecurityException(message: String) : PluginException(message)

package io.github.tokyo720022create.soratome.plugin.runtime.error

/** Raised when a plugin tries to do something its manifest does not allow. */
class PluginSecurityException(message: String) : PluginException(message)

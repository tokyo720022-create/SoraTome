package io.github.tokyo720022create.soratome.data.image

/** A page or cover image could not be loaded. [message] is meant to be shown to the user. */
class PageLoadException(
    message: String,
    val httpCode: Int? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

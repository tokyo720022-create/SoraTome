package io.github.tokyo720022create.soratome.plugin.runtime.script

import org.json.JSONArray
import org.json.JSONObject

/** Returns the string value of [key], or null when the key is missing or JSON null. */
internal fun JSONObject.stringOrNull(key: String): String? = if (isNull(key)) null else getString(key)

internal inline fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
    List(length()) { index -> transform(getJSONObject(index)) }

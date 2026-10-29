package io.github.tokyo720022create.soratome.data.reader

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.tokyo720022create.soratome.domain.reader.ImageQuality
import io.github.tokyo720022create.soratome.domain.reader.ReadingMode

/** Reader options saved on the device. The values are Compose state, so screens update live. */
class ReaderPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("reader", Context.MODE_PRIVATE)

    var readingMode by mutableStateOf(
        ReadingMode.entries.firstOrNull { it.name == prefs.getString(KEY_MODE, null) }
            ?: ReadingMode.RightToLeft,
    )
        private set

    var imageQuality by mutableStateOf(
        ImageQuality.entries.firstOrNull { it.name == prefs.getString(KEY_QUALITY, null) }
            ?: ImageQuality.Original,
    )
        private set

    fun updateReadingMode(mode: ReadingMode) {
        readingMode = mode
        prefs.edit().putString(KEY_MODE, mode.name).apply()
    }

    fun updateImageQuality(quality: ImageQuality) {
        imageQuality = quality
        prefs.edit().putString(KEY_QUALITY, quality.name).apply()
    }

    private companion object {
        const val KEY_MODE = "reading_mode"
        const val KEY_QUALITY = "image_quality"
    }
}

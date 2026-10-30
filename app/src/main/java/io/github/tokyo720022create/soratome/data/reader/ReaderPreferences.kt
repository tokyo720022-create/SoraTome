package io.github.tokyo720022create.soratome.data.reader

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.tokyo720022create.soratome.domain.reader.AutoReadSpeed
import io.github.tokyo720022create.soratome.domain.reader.ImageQuality
import io.github.tokyo720022create.soratome.domain.reader.PageFit
import io.github.tokyo720022create.soratome.domain.reader.ReadingMode
import io.github.tokyo720022create.soratome.domain.reader.ScreenRotation

/** Reader options saved on the device. The values are Compose state, so screens update live. */
class ReaderPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("reader", Context.MODE_PRIVATE)

    var readingMode by mutableStateOf(
        ReadingMode.entries.firstOrNull { it.name == prefs.getString(KEY_MODE, null) }
            ?: ReadingMode.RightToLeft,
    )
        private set

    var pageFit by mutableStateOf(
        PageFit.entries.firstOrNull { it.name == prefs.getString(KEY_FIT, null) } ?: PageFit.FitScreen,
    )
        private set

    var imageQuality by mutableStateOf(
        ImageQuality.entries.firstOrNull { it.name == prefs.getString(KEY_QUALITY, null) }
            ?: ImageQuality.Original,
    )
        private set

    var screenRotation by mutableStateOf(
        ScreenRotation.entries.firstOrNull { it.name == prefs.getString(KEY_ROTATION, null) }
            ?: ScreenRotation.Auto,
    )
        private set

    var autoReadSpeed by mutableStateOf(
        AutoReadSpeed.entries.firstOrNull { it.name == prefs.getString(KEY_AUTO_SPEED, null) }
            ?: AutoReadSpeed.Medium,
    )
        private set

    var showStatusBar by mutableStateOf(prefs.getBoolean(KEY_STATUS_BAR, true))
        private set

    var showTimeAndBattery by mutableStateOf(prefs.getBoolean(KEY_TIME_BATTERY, false))
        private set

    var volumeKeysTurnPages by mutableStateOf(prefs.getBoolean(KEY_VOLUME_KEYS, false))
        private set

    var chapterReminder by mutableStateOf(prefs.getBoolean(KEY_REMINDER, false))
        private set

    var skipExtras by mutableStateOf(prefs.getBoolean(KEY_SKIP_EXTRAS, false))
        private set

    /** True once the first-run reader tips have been shown. */
    var tipsSeen by mutableStateOf(prefs.getBoolean(KEY_TIPS_SEEN, false))
        private set

    fun updateReadingMode(mode: ReadingMode) {
        readingMode = mode
        prefs.edit().putString(KEY_MODE, mode.name).apply()
    }

    fun updatePageFit(fit: PageFit) {
        pageFit = fit
        prefs.edit().putString(KEY_FIT, fit.name).apply()
    }

    fun updateImageQuality(quality: ImageQuality) {
        imageQuality = quality
        prefs.edit().putString(KEY_QUALITY, quality.name).apply()
    }

    fun updateScreenRotation(rotation: ScreenRotation) {
        screenRotation = rotation
        prefs.edit().putString(KEY_ROTATION, rotation.name).apply()
    }

    fun updateAutoReadSpeed(speed: AutoReadSpeed) {
        autoReadSpeed = speed
        prefs.edit().putString(KEY_AUTO_SPEED, speed.name).apply()
    }

    fun updateShowStatusBar(value: Boolean) {
        showStatusBar = value
        prefs.edit().putBoolean(KEY_STATUS_BAR, value).apply()
    }

    fun updateShowTimeAndBattery(value: Boolean) {
        showTimeAndBattery = value
        prefs.edit().putBoolean(KEY_TIME_BATTERY, value).apply()
    }

    fun updateVolumeKeysTurnPages(value: Boolean) {
        volumeKeysTurnPages = value
        prefs.edit().putBoolean(KEY_VOLUME_KEYS, value).apply()
    }

    fun updateChapterReminder(value: Boolean) {
        chapterReminder = value
        prefs.edit().putBoolean(KEY_REMINDER, value).apply()
    }

    fun updateSkipExtras(value: Boolean) {
        skipExtras = value
        prefs.edit().putBoolean(KEY_SKIP_EXTRAS, value).apply()
    }

    fun updateTipsSeen(value: Boolean) {
        tipsSeen = value
        prefs.edit().putBoolean(KEY_TIPS_SEEN, value).apply()
    }

    private companion object {
        const val KEY_MODE = "reading_mode"
        const val KEY_FIT = "page_fit"
        const val KEY_QUALITY = "image_quality"
        const val KEY_ROTATION = "screen_rotation"
        const val KEY_AUTO_SPEED = "auto_read_speed"
        const val KEY_STATUS_BAR = "show_status_bar"
        const val KEY_TIME_BATTERY = "show_time_battery"
        const val KEY_VOLUME_KEYS = "volume_keys"
        const val KEY_REMINDER = "chapter_reminder"
        const val KEY_SKIP_EXTRAS = "skip_extras"
        const val KEY_TIPS_SEEN = "tips_seen"
    }
}

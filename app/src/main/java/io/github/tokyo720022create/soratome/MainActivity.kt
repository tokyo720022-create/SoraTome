package io.github.tokyo720022create.soratome

import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.tokyo720022create.soratome.feature.reader.ReaderKeyEvents
import io.github.tokyo720022create.soratome.feature.splash.SplashScreen
import io.github.tokyo720022create.soratome.ui.SoraTomeApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var showSplash by remember { mutableStateOf(true) }

                // Load plugins and saved options while the splash is on screen,
                // and keep the splash up for at least a moment so it does not just flash.
                LaunchedEffect(Unit) {
                    val started = SystemClock.elapsedRealtime()
                    val app = application as SoraTomeApplication
                    withContext(Dispatchers.Default) {
                        app.plugins.size
                        app.readerPreferences
                        app.chapterListPreferences
                    }
                    val remaining = MIN_SPLASH_MS - (SystemClock.elapsedRealtime() - started)
                    if (remaining > 0) delay(remaining)
                    showSplash = false
                }

                Crossfade(targetState = showSplash, animationSpec = tween(400), label = "splash") { splash ->
                    if (splash) SplashScreen() else SoraTomeApp()
                }
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val handler = ReaderKeyEvents.onVolumeKey
        val isVolumeKey = event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
            event.keyCode == KeyEvent.KEYCODE_VOLUME_UP
        if (handler != null && isVolumeKey) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                handler(event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    private companion object {
        const val MIN_SPLASH_MS = 1_500L
    }
}

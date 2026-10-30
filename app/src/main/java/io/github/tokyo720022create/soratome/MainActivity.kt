package io.github.tokyo720022create.soratome

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import io.github.tokyo720022create.soratome.feature.reader.ReaderKeyEvents
import io.github.tokyo720022create.soratome.ui.SoraTomeApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SoraTomeApp()
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
}

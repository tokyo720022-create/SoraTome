package io.github.tokyo720022create.soratome.feature.reader.components

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Date
import kotlinx.coroutines.delay

/** Small clock and battery level shown over the page. */
@Composable
fun ClockBatteryBadge(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var text by remember { mutableStateOf(readClockAndBattery(context)) }

    LaunchedEffect(Unit) {
        while (true) {
            text = readClockAndBattery(context)
            delay(15_000)
        }
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White.copy(alpha = 0.9f),
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

private fun readClockAndBattery(context: Context): String {
    val time = DateFormat.getTimeFormat(context).format(Date())
    val battery = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    return if (level >= 0 && scale > 0) "$time  ${level * 100 / scale}%" else time
}

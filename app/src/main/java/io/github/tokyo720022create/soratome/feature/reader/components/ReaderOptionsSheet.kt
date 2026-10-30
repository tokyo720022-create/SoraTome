package io.github.tokyo720022create.soratome.feature.reader.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.tokyo720022create.soratome.data.reader.ReaderPreferences
import io.github.tokyo720022create.soratome.domain.reader.AutoReadSpeed
import io.github.tokyo720022create.soratome.domain.reader.ImageQuality
import io.github.tokyo720022create.soratome.domain.reader.ReadingMode
import io.github.tokyo720022create.soratome.domain.reader.ScreenRotation
import io.github.tokyo720022create.soratome.ui.components.SegmentedOptions
import io.github.tokyo720022create.soratome.ui.components.SwitchRow
import kotlin.math.roundToInt

/** The reader's options sheet: mode, quality, auto read, rotation, display and controls. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderOptionsSheet(
    preferences: ReaderPreferences,
    autoReadActive: Boolean,
    onToggleAutoRead: () -> Unit,
    canShareChapter: Boolean,
    onShareChapter: () -> Unit,
    onShowTips: () -> Unit,
    onMoreSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Reader options", style = MaterialTheme.typography.titleLarge)
                FilledTonalButton(onClick = onDismiss) { Text("Done") }
            }

            SectionLabel("READING MODE")
            SegmentedOptions(
                options = ReadingMode.entries,
                selected = preferences.readingMode,
                label = {
                    when (it) {
                        ReadingMode.LeftToRight -> "→\nLeft to right"
                        ReadingMode.RightToLeft -> "←\nRight to left"
                        ReadingMode.Vertical -> "↕\nVertical"
                    }
                },
                onSelect = { preferences.updateReadingMode(it) },
            )

            SectionLabel("IMAGE QUALITY")
            SegmentedOptions(
                options = ImageQuality.entries,
                selected = preferences.imageQuality,
                label = { it.label },
                onSelect = { preferences.updateImageQuality(it) },
            )

            SectionLabel("AUTO READ")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Scroll or turn pages for you", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Speed: ${preferences.autoReadSpeed.label}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Button(onClick = onToggleAutoRead) { Text(if (autoReadActive) "Stop" else "Start") }
            }
            Slider(
                value = preferences.autoReadSpeed.ordinal.toFloat(),
                onValueChange = { value ->
                    val index = value.roundToInt().coerceIn(0, AutoReadSpeed.entries.lastIndex)
                    preferences.updateAutoReadSpeed(AutoReadSpeed.entries[index])
                },
                valueRange = 0f..AutoReadSpeed.entries.lastIndex.toFloat(),
                steps = AutoReadSpeed.entries.size - 2,
            )

            SectionLabel("SCREEN ROTATION")
            SegmentedOptions(
                options = ScreenRotation.entries,
                selected = preferences.screenRotation,
                label = { it.label },
                onSelect = { preferences.updateScreenRotation(it) },
            )

            SwitchRow(
                title = "Show status bar",
                summary = "Keep the system bar visible while reading",
                checked = preferences.showStatusBar,
                onCheckedChange = { preferences.updateShowStatusBar(it) },
            )
            SwitchRow(
                title = "Time and battery",
                summary = "A small clock and battery level on the page",
                checked = preferences.showTimeAndBattery,
                onCheckedChange = { preferences.updateShowTimeAndBattery(it) },
            )
            SwitchRow(
                title = "Volume keys turn pages",
                summary = "Volume down goes forward, volume up goes back",
                checked = preferences.volumeKeysTurnPages,
                onCheckedChange = { preferences.updateVolumeKeysTurnPages(it) },
            )
            SwitchRow(
                title = "Chapter reminder",
                summary = "Show the chapter name when you open it",
                checked = preferences.chapterReminder,
                onCheckedChange = { preferences.updateChapterReminder(it) },
            )
            SwitchRow(
                title = "Skip extras",
                summary = "Next and previous skip chapters without a number",
                checked = preferences.skipExtras,
                onCheckedChange = { preferences.updateSkipExtras(it) },
            )

            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
            if (canShareChapter) {
                LinkRow(title = "Share chapter link", onClick = onShareChapter) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                }
            }
            LinkRow(title = "See the operation tips again", onClick = onShowTips) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
            LinkRow(title = "More settings", onClick = onMoreSettings) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun LinkRow(title: String, onClick: () -> Unit, trailing: @Composable () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = trailing,
        modifier = Modifier.clickable(onClick = onClick),
    )
}

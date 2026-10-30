package io.github.tokyo720022create.soratome.feature.settings.reader

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.tokyo720022create.soratome.SoraTomeApplication
import io.github.tokyo720022create.soratome.domain.reader.AutoReadSpeed
import io.github.tokyo720022create.soratome.domain.reader.ChapterNaming
import io.github.tokyo720022create.soratome.domain.reader.ChapterOrder
import io.github.tokyo720022create.soratome.domain.reader.ChapterSort
import io.github.tokyo720022create.soratome.domain.reader.ImageQuality
import io.github.tokyo720022create.soratome.domain.reader.PageFit
import io.github.tokyo720022create.soratome.domain.reader.ReadingMode
import io.github.tokyo720022create.soratome.domain.reader.ScreenRotation
import io.github.tokyo720022create.soratome.feature.settings.components.SettingsScaffold
import io.github.tokyo720022create.soratome.feature.settings.components.SettingsSectionHeader
import io.github.tokyo720022create.soratome.ui.components.DropdownRow
import io.github.tokyo720022create.soratome.ui.components.SwitchRow

/** Settings > Reader: the reader options, reader display and controls, and the chapter list. */
@Composable
fun ReaderSettingsScreen(onBack: () -> Unit) {
    val application = LocalContext.current.applicationContext as SoraTomeApplication
    val reader = application.readerPreferences
    val chapterList = application.chapterListPreferences

    SettingsScaffold(title = "Reader", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsSectionHeader("Reader")
            DropdownRow(
                title = "Reading mode",
                summary = "Scroll for webtoons, right to left for manga.",
                options = ReadingMode.entries,
                selected = reader.readingMode,
                label = { it.label },
                onSelect = { reader.updateReadingMode(it) },
            )
            DropdownRow(
                title = "Page fit",
                summary = "For the page-by-page modes.",
                options = PageFit.entries,
                selected = reader.pageFit,
                label = { it.label },
                onSelect = { reader.updatePageFit(it) },
            )
            DropdownRow(
                title = "Image quality",
                summary = "Data saver uses less mobile data.",
                options = ImageQuality.entries,
                selected = reader.imageQuality,
                label = { it.label },
                onSelect = { reader.updateImageQuality(it) },
            )
            DropdownRow(
                title = "Screen rotation",
                summary = "Lock portrait or landscape while reading.",
                options = ScreenRotation.entries,
                selected = reader.screenRotation,
                label = { it.label },
                onSelect = { reader.updateScreenRotation(it) },
            )
            DropdownRow(
                title = "Auto read speed",
                summary = "Start auto read from the reader options.",
                options = AutoReadSpeed.entries,
                selected = reader.autoReadSpeed,
                label = { it.label },
                onSelect = { reader.updateAutoReadSpeed(it) },
            )
            ListItem(
                headlineContent = { Text("Resume where you left off") },
                supportingContent = {
                    Text("Chapters reopen at the page you stopped on. Finished chapters start at page one.")
                },
                trailingContent = { Text("Automatic") },
            )

            SettingsSectionHeader("Reader display and controls")
            SwitchRow(
                title = "Show status bar",
                summary = "Turn off to read full screen.",
                checked = reader.showStatusBar,
                onCheckedChange = { reader.updateShowStatusBar(it) },
            )
            SwitchRow(
                title = "Time and battery",
                summary = "A small clock and battery level on the page.",
                checked = reader.showTimeAndBattery,
                onCheckedChange = { reader.updateShowTimeAndBattery(it) },
            )
            SwitchRow(
                title = "Volume keys turn pages",
                summary = "Volume down goes forward, volume up goes back.",
                checked = reader.volumeKeysTurnPages,
                onCheckedChange = { reader.updateVolumeKeysTurnPages(it) },
            )
            SwitchRow(
                title = "Chapter reminder",
                summary = "Show the chapter name when you open it.",
                checked = reader.chapterReminder,
                onCheckedChange = { reader.updateChapterReminder(it) },
            )
            SwitchRow(
                title = "Skip extras",
                summary = "Next and previous skip chapters with no number.",
                checked = reader.skipExtras,
                onCheckedChange = { reader.updateSkipExtras(it) },
            )
            ListItem(
                headlineContent = { Text("Operation tips") },
                supportingContent = { Text("Show the reader tips again next time.") },
                trailingContent = {
                    OutlinedButton(onClick = { reader.updateTipsSeen(false) }) { Text("Show again") }
                },
            )

            SettingsSectionHeader("Chapter list")
            DropdownRow(
                title = "Sort chapters",
                summary = "By source keeps the order the source gives.",
                options = ChapterSort.entries,
                selected = chapterList.sort,
                label = { it.label },
                onSelect = { chapterList.updateSort(it) },
            )
            DropdownRow(
                title = "Order",
                summary = "Which end of the list comes first.",
                options = ChapterOrder.entries,
                selected = chapterList.order,
                label = { it.label },
                onSelect = { chapterList.updateOrder(it) },
            )
            DropdownRow(
                title = "Chapter names",
                summary = "Source title, or just the number.",
                options = ChapterNaming.entries,
                selected = chapterList.naming,
                label = { it.label },
                onSelect = { chapterList.updateNaming(it) },
            )
            SwitchRow(
                title = "Only unread chapters",
                summary = "Hide chapters you have finished.",
                checked = chapterList.onlyUnread,
                onCheckedChange = { chapterList.updateOnlyUnread(it) },
            )
        }
    }
}

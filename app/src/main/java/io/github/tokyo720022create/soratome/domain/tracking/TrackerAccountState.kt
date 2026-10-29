package io.github.tokyo720022create.soratome.domain.tracking

/** Whether the user is signed in to a [TrackerService]. */
sealed interface TrackerAccountState {
    data object NotConnected : TrackerAccountState

    data class Connected(val username: String) : TrackerAccountState
}

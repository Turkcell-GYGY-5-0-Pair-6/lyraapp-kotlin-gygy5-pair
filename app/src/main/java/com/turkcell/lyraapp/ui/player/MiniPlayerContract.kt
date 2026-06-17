package com.turkcell.lyraapp.ui.player

import com.turkcell.lyraapp.data.player.PlaybackState

/**
 * Mini Player ekran/bileşen MVI sözleşmesi: State ve Intent.
 */
data class MiniPlayerUiState(
    val playbackState: PlaybackState? = null
)

sealed interface MiniPlayerIntent {
    data object TogglePlayPause : MiniPlayerIntent
    data object ToggleLike : MiniPlayerIntent
    data object SkipNext : MiniPlayerIntent
    data object SkipPrevious : MiniPlayerIntent
}

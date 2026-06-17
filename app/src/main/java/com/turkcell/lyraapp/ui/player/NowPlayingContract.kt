package com.turkcell.lyraapp.ui.player

import com.turkcell.lyraapp.data.player.PlaybackState

/**
 * Now Playing ekranı MVI sözleşmesi: State, Intent ve Effect.
 */
data class NowPlayingUiState(
    val isLoading: Boolean = false,
    val playbackState: PlaybackState? = null,
    val error: String? = null
)

sealed interface NowPlayingIntent {
    data class LoadSong(val songId: String) : NowPlayingIntent
    data object TogglePlayPause : NowPlayingIntent
    data object ToggleLike : NowPlayingIntent
    data object ToggleShuffle : NowPlayingIntent
    data object ToggleRepeat : NowPlayingIntent
    data class SeekTo(val progressMs: Long) : NowPlayingIntent
    data object SkipNext : NowPlayingIntent
    data object SkipPrevious : NowPlayingIntent
    data object BackClicked : NowPlayingIntent
}

sealed interface NowPlayingEffect {
    data object NavigateBack : NowPlayingEffect
    data class ShowError(val message: String) : NowPlayingEffect
}

package com.turkcell.lyraapp.ui.playlist

import com.turkcell.lyraapp.data.playlist.PlaylistDetail

/**
 * Playlist detay ekranının MVI sözleşmesi: State, Intent ve Effect.
 */
data class PlaylistDetailUiState(
    val isLoading: Boolean = false,
    val playlist: PlaylistDetail? = null,
    val error: String? = null
)

sealed interface PlaylistDetailIntent {
    data class LoadPlaylist(val playlistId: String) : PlaylistDetailIntent
    data class ToggleLikeSong(val songId: String) : PlaylistDetailIntent
    data object TogglePlaylistFavorite : PlaylistDetailIntent
    data object TogglePlaylistDownload : PlaylistDetailIntent
    data object ToggleShuffle : PlaylistDetailIntent
    data object PlayPlaylist : PlaylistDetailIntent
    data class SongClicked(val songId: String) : PlaylistDetailIntent
    data object BackClicked : PlaylistDetailIntent
}

sealed interface PlaylistDetailEffect {
    data object NavigateBack : PlaylistDetailEffect
    data class ShowError(val message: String) : PlaylistDetailEffect
}

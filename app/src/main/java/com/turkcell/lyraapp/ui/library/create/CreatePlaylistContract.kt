package com.turkcell.lyraapp.ui.library.create

import com.turkcell.lyraapp.data.library.LibrarySong

/**
 * Yeni Çalma Listesi ekranı MVI sözleşmesi.
 */
data class CreatePlaylistUiState(
    val title: String = "",
    val description: String = "",
    val isPublic: Boolean = true,
    val songs: List<LibrarySong> = emptyList(),
    val selectedSongIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isSaveEnabled: Boolean = false
) {
    val selectedSongsText: String = "${selectedSongIds.size} seçili"
}

sealed interface CreatePlaylistIntent {
    data object LoadSongs : CreatePlaylistIntent
    data class TitleChanged(val value: String) : CreatePlaylistIntent
    data class DescriptionChanged(val value: String) : CreatePlaylistIntent
    data class ToggleSongSelection(val songId: String) : CreatePlaylistIntent
    data class SetPublic(val value: Boolean) : CreatePlaylistIntent
    data object SavePlaylist : CreatePlaylistIntent
    data object CancelClicked : CreatePlaylistIntent
    data object ChangeCoverClicked : CreatePlaylistIntent
}

sealed interface CreatePlaylistEffect {
    data object NavigateBack : CreatePlaylistEffect
    data object SaveSuccess : CreatePlaylistEffect
    data class ShowError(val message: String) : CreatePlaylistEffect
}

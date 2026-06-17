package com.turkcell.lyraapp.ui.library

import com.turkcell.lyraapp.data.library.LibraryPlaylist

/**
 * Kütüphane ekranının MVI sözleşmesi: State (durum), Intent (kullanıcı niyeti) ve
 * Effect (tek seferlik olay) tek dosyada tanımlanmıştır.
 */

data class LibraryUiState(
    val playlists: List<LibraryPlaylist> = emptyList(),
    val filteredPlaylists: List<LibraryPlaylist> = emptyList(),
    val isLoading: Boolean = false,
    val selectedFilter: String = "Çalma listeleri",
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val error: String? = null
)

sealed interface LibraryIntent {
    data object LoadLibrary : LibraryIntent
    data class FilterSelected(val filter: String) : LibraryIntent
    data object AddPlaylistClicked : LibraryIntent
    data object ToggleSearch : LibraryIntent
    data class SearchQueryChanged(val query: String) : LibraryIntent
    data class PlaylistClicked(val playlistId: String) : LibraryIntent
}

sealed interface LibraryEffect {
    data class ShowError(val message: String) : LibraryEffect
    data object NavigateToCreatePlaylist : LibraryEffect
    data class NavigateToPlaylistDetail(val playlistId: String) : LibraryEffect
}

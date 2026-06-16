package com.turkcell.lyraapp.ui.library

import com.turkcell.lyraapp.data.library.LibraryPlaylist

/**
 * Kütüphane ekranının MVI sözleşmesi: State (durum), Intent (kullanıcı niyeti) ve
 * Effect (tek seferlik olay) tek dosyada tanımlanmıştır.
 */

data class LibraryUiState(
    val playlists: List<LibraryPlaylist> = emptyList(),
    val isLoading: Boolean = false,
    val selectedFilter: String = "Çalma listeleri",
    val error: String? = null
)

sealed interface LibraryIntent {
    data object LoadLibrary : LibraryIntent
    data class FilterSelected(val filter: String) : LibraryIntent
}

sealed interface LibraryEffect {
    data class ShowError(val message: String) : LibraryEffect
}

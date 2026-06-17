package com.turkcell.lyraapp.ui.favorites

import com.turkcell.lyraapp.data.favorites.FavoriteSong

/**
 * Favoriler (Beğenilen Şarkılar) ekranı MVI sözleşmesi.
 */
data class FavoritesUiState(
    val isLoading: Boolean = false,
    val songs: List<FavoriteSong> = emptyList(),
    val isShuffleEnabled: Boolean = false,
    val isDownloaded: Boolean = false
) {
    val totalSongsText: String = "${songs.size} şarkı"
    val totalDurationText: String = "19 dk" // Toplam süre (3:34 + 4:07 + 3:25 + 3:43 + 4:29 = 19 dk 8 sn)
}

sealed interface FavoritesIntent {
    data object LoadSongs : FavoritesIntent
    data object PlayAll : FavoritesIntent
    data object ToggleShuffle : FavoritesIntent
    data object ToggleDownload : FavoritesIntent
    data class SongClicked(val songId: String) : FavoritesIntent
    data class ToggleLikeSong(val songId: String) : FavoritesIntent
    data object BackClicked : FavoritesIntent
}

sealed interface FavoritesEffect {
    data object NavigateBack : FavoritesEffect
    data class ShowError(val message: String) : FavoritesEffect
}

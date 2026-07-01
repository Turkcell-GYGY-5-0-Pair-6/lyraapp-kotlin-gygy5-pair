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
    val totalDurationText: String
        get() {
            var totalSeconds = 0
            for (song in songs) {
                val parts = song.duration.split(":")
                val seconds = try {
                    when (parts.size) {
                        1 -> parts[0].toIntOrNull() ?: 0
                        2 -> {
                            val mins = parts[0].toIntOrNull() ?: 0
                            val secs = parts[1].toIntOrNull() ?: 0
                            mins * 60 + secs
                        }
                        3 -> {
                            val hrs = parts[0].toIntOrNull() ?: 0
                            val mins = parts[1].toIntOrNull() ?: 0
                            val secs = parts[2].toIntOrNull() ?: 0
                            hrs * 3600 + mins * 60 + secs
                        }
                        else -> 0
                    }
                } catch (e: Exception) {
                    0
                }
                totalSeconds += seconds
            }

            if (totalSeconds == 0) return "0 dk"

            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            return when {
                hours > 0 -> {
                    if (seconds > 0) "${hours} sa ${minutes} dk ${seconds} sn"
                    else if (minutes > 0) "${hours} sa ${minutes} dk"
                    else "${hours} sa"
                }
                minutes > 0 -> {
                    if (seconds > 0) "${minutes} dk ${seconds} sn"
                    else "${minutes} dk"
                }
                else -> "${seconds} sn"
            }
        }
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
    data class NavigateToNowPlaying(val songId: String) : FavoritesEffect
}

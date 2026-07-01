package com.turkcell.lyraapp.data.favorites

import kotlinx.serialization.Serializable
import kotlinx.coroutines.flow.Flow

@Serializable
data class FavoriteSong(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val isLiked: Boolean = true,
    val isPlaying: Boolean = false,
    val artworkStartColor: Long,
    val artworkEndColor: Long
)

interface FavoritesRepository {
    val favoriteSongsFlow: Flow<List<FavoriteSong>>
    suspend fun getFavoriteSongs(): Result<List<FavoriteSong>>
    suspend fun isFavorite(songId: String): Boolean
    suspend fun toggleFavorite(song: FavoriteSong): Result<Unit>
}

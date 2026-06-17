package com.turkcell.lyraapp.data.favorites

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
    suspend fun getFavoriteSongs(): Result<List<FavoriteSong>>
}

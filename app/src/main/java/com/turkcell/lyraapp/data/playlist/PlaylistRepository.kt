package com.turkcell.lyraapp.data.playlist

data class PlaylistSong(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val isLiked: Boolean = false,
    val isPlaying: Boolean = false,
    val artworkStartColor: Long,
    val artworkEndColor: Long
)

data class PlaylistDetail(
    val id: String,
    val title: String,
    val description: String,
    val creator: String,
    val songCount: Int,
    val durationText: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val isShuffleEnabled: Boolean = false,
    val isPlaying: Boolean = false,
    val songs: List<PlaylistSong> = emptyList()
)

interface PlaylistRepository {
    suspend fun getPlaylistDetail(playlistId: String): Result<PlaylistDetail>
    suspend fun toggleLikeSong(playlistId: String, songId: String): Result<Unit>
    suspend fun togglePlaylistFavorite(playlistId: String): Result<Unit>
    suspend fun togglePlaylistDownload(playlistId: String): Result<Unit>
    suspend fun toggleShuffle(playlistId: String): Result<Unit>
    suspend fun playPlaylist(playlistId: String): Result<Unit>
    suspend fun deletePlaylist(playlistId: String): Result<Unit>
}

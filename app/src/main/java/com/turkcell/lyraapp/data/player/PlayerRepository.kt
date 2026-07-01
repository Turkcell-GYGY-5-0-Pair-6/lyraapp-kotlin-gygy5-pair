package com.turkcell.lyraapp.data.player

import kotlinx.coroutines.flow.Flow
import androidx.media3.exoplayer.ExoPlayer

/**
 * Oynatma için imzalı stream URL'si sağlayan veri kaynağı soyutlaması.
 *
 * ExoPlayer'a verilecek URL backend tarafından kısa ömürlü (signed) üretildiğinden,
 * oynatmadan hemen önce alınır (bkz. docs/api/openapi.json — /songs/{id}/stream-url).
 */
data class PlaybackState(
    val songId: String,
    val title: String,
    val artist: String,
    val albumName: String, // e.g. "Gece Vardiyası"
    val durationText: String, // e.g. "3:43"
    val durationMs: Long,
    val currentProgressText: String, // e.g. "1:33"
    val currentProgressMs: Long,
    val isPlaying: Boolean = false,
    val isLiked: Boolean = false,
    val isShuffleEnabled: Boolean = false,
    val isRepeatEnabled: Boolean = false,
    val artworkStartColor: Long,
    val artworkEndColor: Long
)

enum class SongDownloadState {
    NOT_DOWNLOADED,
    DOWNLOADING,
    DOWNLOADED
}

interface PlayerRepository {
    val player: ExoPlayer
    val playbackStateFlow: Flow<PlaybackState?>
    fun setCurrentSongId(songId: String)
    suspend fun getPlaybackState(songId: String): Result<PlaybackState>
    suspend fun getStreamUrl(songId: String): Result<String>
    suspend fun togglePlayPause(): Result<Unit>
    suspend fun toggleLike(): Result<Unit>
    suspend fun toggleShuffle(): Result<Unit>
    suspend fun toggleRepeat(): Result<Unit>
    suspend fun seekTo(progressMs: Long): Result<Unit>
    suspend fun skipToNext(): Result<Unit>
    suspend fun skipToPrevious(): Result<Unit>
    suspend fun stop(): Result<Unit>

    fun isSongDownloaded(songId: String): Boolean
    fun getSongDownloadState(songId: String): Flow<SongDownloadState>
    suspend fun downloadSong(songId: String): Result<Unit>
    suspend fun deleteDownloadedSong(songId: String): Result<Unit>
}

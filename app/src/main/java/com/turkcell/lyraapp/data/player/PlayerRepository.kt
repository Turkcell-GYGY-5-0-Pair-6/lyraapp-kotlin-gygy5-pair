package com.turkcell.lyraapp.data.player

import kotlinx.coroutines.flow.Flow

data class PlaybackState(
    val songId: String,
    val title: String,
    val artist: String,
    val albumName: String, // e.g. "Gece Vardiyası" (mockup says Gece Vardiyası)
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

interface PlayerRepository {
    val playbackStateFlow: Flow<PlaybackState?>
    suspend fun getPlaybackState(songId: String): Result<PlaybackState>
    suspend fun togglePlayPause(): Result<Unit>
    suspend fun toggleLike(): Result<Unit>
    suspend fun toggleShuffle(): Result<Unit>
    suspend fun toggleRepeat(): Result<Unit>
    suspend fun seekTo(progressMs: Long): Result<Unit>
    suspend fun skipToNext(): Result<Unit>
    suspend fun skipToPrevious(): Result<Unit>
}

package com.turkcell.lyraapp.data.player

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakePlayerRepository @Inject constructor() : PlayerRepository {

    private var currentState = PlaybackState(
        songId = "song-1",
        title = "Neon Sokaklar",
        artist = "Şehir Işıkları",
        albumName = "Gece Vardiyası",
        durationText = "3:43",
        durationMs = 223000L,
        currentProgressText = "1:33",
        currentProgressMs = 93000L,
        isPlaying = true,
        isLiked = true,
        isShuffleEnabled = false,
        isRepeatEnabled = false,
        artworkStartColor = 0xFFD98E4A,
        artworkEndColor = 0xFF8A5526
    )

    private val _playbackStateFlow = MutableStateFlow<PlaybackState?>(null)
    override val playbackStateFlow: Flow<PlaybackState?> = _playbackStateFlow.asStateFlow()

    init {
        _playbackStateFlow.value = currentState
    }

    private fun updateState(newState: PlaybackState) {
        currentState = newState
        _playbackStateFlow.value = newState
    }

    override suspend fun getPlaybackState(songId: String): Result<PlaybackState> {
        delay(400L)
        val updated = currentState.copy(songId = songId)
        updateState(updated)
        return Result.success(updated)
    }

    override suspend fun togglePlayPause(): Result<Unit> {
        delay(100L)
        val updated = currentState.copy(isPlaying = !currentState.isPlaying)
        updateState(updated)
        return Result.success(Unit)
    }

    override suspend fun toggleLike(): Result<Unit> {
        delay(100L)
        val updated = currentState.copy(isLiked = !currentState.isLiked)
        updateState(updated)
        return Result.success(Unit)
    }

    override suspend fun toggleShuffle(): Result<Unit> {
        delay(100L)
        val updated = currentState.copy(isShuffleEnabled = !currentState.isShuffleEnabled)
        updateState(updated)
        return Result.success(Unit)
    }

    override suspend fun toggleRepeat(): Result<Unit> {
        delay(100L)
        val updated = currentState.copy(isRepeatEnabled = !currentState.isRepeatEnabled)
        updateState(updated)
        return Result.success(Unit)
    }

    override suspend fun seekTo(progressMs: Long): Result<Unit> {
        delay(100L)
        val progressSeconds = progressMs / 1000
        val min = progressSeconds / 60
        val sec = progressSeconds % 60
        val progressText = String.format("%d:%02d", min, sec)
        val updated = currentState.copy(
            currentProgressMs = progressMs,
            currentProgressText = progressText
        )
        updateState(updated)
        return Result.success(Unit)
    }

    override suspend fun skipToNext(): Result<Unit> {
        delay(200L)
        val updated = currentState.copy(
            songId = "next-song",
            title = "Gece Yarısı",
            artist = "Mavi Deniz",
            durationText = "3:34",
            durationMs = 214000L,
            currentProgressText = "0:00",
            currentProgressMs = 0L,
            isPlaying = true,
            artworkStartColor = 0xFF6FBF5A,
            artworkEndColor = 0xFF356B2A
        )
        updateState(updated)
        return Result.success(Unit)
    }

    override suspend fun skipToPrevious(): Result<Unit> {
        delay(200L)
        val updated = currentState.copy(
            songId = "prev-song",
            title = "Yıldız Tozu",
            artist = "Polaris",
            durationText = "4:07",
            durationMs = 247000L,
            currentProgressText = "0:00",
            currentProgressMs = 0L,
            isPlaying = true,
            artworkStartColor = 0xFF3D5A80,
            artworkEndColor = 0xFF1B2A45
        )
        updateState(updated)
        return Result.success(Unit)
    }
}

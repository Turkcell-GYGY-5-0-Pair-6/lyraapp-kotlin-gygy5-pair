package com.turkcell.lyraapp.data.player

import kotlinx.coroutines.delay
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

    override suspend fun getPlaybackState(songId: String): Result<PlaybackState> {
        delay(400L)
        // Eğer songId "fav-4" veya "song-1" (Neon Sokaklar) değilse, isimleri gelen songId'ye göre güncelleyebiliriz
        // ama görsel prototip için ekran görüntüsündeki Neon Sokaklar detaylarını koruyoruz.
        currentState = currentState.copy(songId = songId)
        return Result.success(currentState)
    }

    override suspend fun togglePlayPause(): Result<Unit> {
        delay(100L)
        currentState = currentState.copy(isPlaying = !currentState.isPlaying)
        return Result.success(Unit)
    }

    override suspend fun toggleLike(): Result<Unit> {
        delay(100L)
        currentState = currentState.copy(isLiked = !currentState.isLiked)
        return Result.success(Unit)
    }

    override suspend fun toggleShuffle(): Result<Unit> {
        delay(100L)
        currentState = currentState.copy(isShuffleEnabled = !currentState.isShuffleEnabled)
        return Result.success(Unit)
    }

    override suspend fun toggleRepeat(): Result<Unit> {
        delay(100L)
        currentState = currentState.copy(isRepeatEnabled = !currentState.isRepeatEnabled)
        return Result.success(Unit)
    }

    override suspend fun seekTo(progressMs: Long): Result<Unit> {
        delay(100L)
        val progressSeconds = progressMs / 1000
        val min = progressSeconds / 60
        val sec = progressSeconds % 60
        val progressText = String.format("%d:%02d", min, sec)
        currentState = currentState.copy(
            currentProgressMs = progressMs,
            currentProgressText = progressText
        )
        return Result.success(Unit)
    }

    override suspend fun skipToNext(): Result<Unit> {
        delay(200L)
        // Sonraki şarkıyı simüle et
        currentState = currentState.copy(
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
        return Result.success(Unit)
    }

    override suspend fun skipToPrevious(): Result<Unit> {
        delay(200L)
        // Önceki şarkıyı simüle et
        currentState = currentState.copy(
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
        return Result.success(Unit)
    }
}

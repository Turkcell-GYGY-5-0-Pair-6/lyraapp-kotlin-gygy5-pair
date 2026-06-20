package com.turkcell.lyraapp.data.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.turkcell.lyraapp.data.songs.SongDto
import com.turkcell.lyraapp.data.songs.SongsApi
import com.turkcell.lyraapp.data.songs.RecordPlayRequest
import com.turkcell.lyraapp.data.auth.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * [PlayerRepository]'nin ExoPlayer tabanlı gerçek API implementasyonu.
 */
@Singleton
class DefaultPlayerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songsApi: SongsApi,
    private val authRepository: AuthRepository,
) : PlayerRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var progressJob: Job? = null

    override val player: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            addListener(listener)
        }
    }

    private val _playbackStateFlow = MutableStateFlow<PlaybackState?>(null)
    override val playbackStateFlow: Flow<PlaybackState?> = _playbackStateFlow.asStateFlow()

    private var cachedSongs: List<SongDto> = emptyList()
    private var currentSongId: String? = null
    private var isLikedState = false
    private var isShuffleState = false
    private var isRepeatState = false

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePlaybackState()
            if (isPlaying) {
                startProgressPolling()
                startPlaybackService()
            } else {
                stopProgressPolling()
            }
        }

        override fun onPlaybackStateChanged(state: Int) {
            updatePlaybackState()
            if (state == Player.STATE_ENDED) {
                if (player.repeatMode != Player.REPEAT_MODE_ONE) {
                    scope.launch { skipToNext() }
                }
            }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            updatePlaybackState()
        }
    }

    private suspend fun getSongList(): List<SongDto> {
        if (cachedSongs.isNotEmpty()) return cachedSongs
        return try {
            val response = songsApi.getSongs(limit = 100)
            cachedSongs = response.data
            response.data
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                updatePlaybackState()
                delay(500L)
            }
        }
    }

    private fun stopProgressPolling() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun updatePlaybackState() {
        val songId = currentSongId ?: return
        val song = cachedSongs.find { it.id == songId } ?: return

        val isPlaying = player.isPlaying
        val durationMs = player.duration.coerceAtLeast(0L)
        val progressMs = player.currentPosition.coerceAtLeast(0L)
        val (startColor, endColor) = artworkColorsFor(songId)

        val newState = PlaybackState(
            songId = songId,
            title = song.title,
            artist = song.artist,
            albumName = song.album ?: "Lyra Album",
            durationText = formatTime(durationMs),
            durationMs = durationMs,
            currentProgressText = formatTime(progressMs),
            currentProgressMs = progressMs,
            isPlaying = isPlaying,
            isLiked = isLikedState,
            isShuffleEnabled = isShuffleState,
            isRepeatEnabled = isRepeatState,
            artworkStartColor = startColor,
            artworkEndColor = endColor
        )
        _playbackStateFlow.value = newState
    }

    override suspend fun getStreamUrl(songId: String): Result<String> = runCatching {
        songsApi.getStreamUrl(songId).data.url
    }

    override fun setCurrentSongId(songId: String) {
        currentSongId = songId
        scope.launch {
            getSongList()
            updatePlaybackState()
        }
    }

    override suspend fun getPlaybackState(songId: String): Result<PlaybackState> = withContext(Dispatchers.Main) {
        runCatching {
            var song = cachedSongs.find { it.id == songId }
            if (song == null) {
                getSongList()
                song = cachedSongs.find { it.id == songId } ?: throw NoSuchElementException("Song not found")
            }

            val streamUrl = songsApi.getStreamUrl(songId).data.url

            if (currentSongId != songId) {
                currentSongId = songId
                
                val mediaMetadata = MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album ?: "Lyra Album")
                    .build()

                val mediaItem = MediaItem.Builder()
                    .setUri(Uri.parse(streamUrl))
                    .setMediaMetadata(mediaMetadata)
                    .build()

                player.setMediaItem(mediaItem)
                player.prepare()
            }
            player.play()

            // Asenkron olarak çalma işlemini backend'e kaydet (recently-played için)
            scope.launch(Dispatchers.IO) {
                try {
                    val token = authRepository.getAccessToken()
                    if (token != null) {
                        songsApi.recordPlay(
                            authorization = "Bearer $token",
                            request = RecordPlayRequest(songId)
                        )
                    }
                } catch (e: Exception) {
                    // Hataları sessizce yut
                }
            }

            updatePlaybackState()
            _playbackStateFlow.value!!
        }
    }

    private fun startPlaybackService() {
        try {
            val intent = Intent(context, PlaybackService::class.java)
            context.startService(intent)
        } catch (e: Exception) {
            // Gracefully ignore service startup failures in background
        }
    }

    override suspend fun togglePlayPause(): Result<Unit> = withContext(Dispatchers.Main) {
        runCatching {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
            updatePlaybackState()
        }
    }

    override suspend fun toggleLike(): Result<Unit> = runCatching {
        isLikedState = !isLikedState
        withContext(Dispatchers.Main) {
            updatePlaybackState()
        }
    }

    override suspend fun toggleShuffle(): Result<Unit> = runCatching {
        isShuffleState = !isShuffleState
        withContext(Dispatchers.Main) {
            updatePlaybackState()
        }
    }

    override suspend fun toggleRepeat(): Result<Unit> = withContext(Dispatchers.Main) {
        runCatching {
            isRepeatState = !isRepeatState
            player.repeatMode = if (isRepeatState) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
            updatePlaybackState()
        }
    }

    override suspend fun seekTo(progressMs: Long): Result<Unit> = withContext(Dispatchers.Main) {
        runCatching {
            player.seekTo(progressMs)
            updatePlaybackState()
        }
    }

    override suspend fun skipToNext(): Result<Unit> = withContext(Dispatchers.Main) {
        runCatching {
            val songList = getSongList()
            val currentIndex = songList.indexOfFirst { it.id == currentSongId }
            if (currentIndex != -1 && songList.isNotEmpty()) {
                val nextIndex = (currentIndex + 1) % songList.size
                val nextSong = songList[nextIndex]
                getPlaybackState(nextSong.id)
            }
            Unit
        }
    }

    override suspend fun skipToPrevious(): Result<Unit> = withContext(Dispatchers.Main) {
        runCatching {
            val songList = getSongList()
            val currentIndex = songList.indexOfFirst { it.id == currentSongId }
            if (currentIndex != -1 && songList.isNotEmpty()) {
                val prevIndex = if (currentIndex - 1 < 0) songList.size - 1 else currentIndex - 1
                val prevSong = songList[prevIndex]
                getPlaybackState(prevSong.id)
            }
            Unit
        }
    }

    private companion object {
        fun formatTime(ms: Long): String {
            if (ms <= 0) return "0:00"
            val totalSeconds = ms / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }

        fun artworkColorsFor(id: String): Pair<Long, Long> {
            val hue = (abs(id.hashCode()) % 360).toFloat()
            val start = hslToArgb(hue, saturation = 0.50f, lightness = 0.55f)
            val end = hslToArgb(hue, saturation = 0.55f, lightness = 0.32f)
            return start to end
        }

        fun hslToArgb(hue: Float, saturation: Float, lightness: Float): Long {
            val c = (1f - abs(2f * lightness - 1f)) * saturation
            val hPrime = hue / 60f
            val x = c * (1f - abs(hPrime % 2f - 1f))
            val (r1, g1, b1) = when {
                hPrime < 1f -> Triple(c, x, 0f)
                hPrime < 2f -> Triple(x, c, 0f)
                hPrime < 3f -> Triple(0f, c, x)
                hPrime < 4f -> Triple(0f, x, c)
                hPrime < 5f -> Triple(x, 0f, c)
                else -> Triple(c, 0f, x)
            }
            val m = lightness - c / 2f
            val r = ((r1 + m) * 255f).roundToInt().coerceIn(0, 255).toLong()
            val g = ((g1 + m) * 255f).roundToInt().coerceIn(0, 255).toLong()
            val b = ((b1 + m) * 255f).roundToInt().coerceIn(0, 255).toLong()
            return (0xFFL shl 24) or (r shl 16) or (g shl 8) or b
        }
    }
}
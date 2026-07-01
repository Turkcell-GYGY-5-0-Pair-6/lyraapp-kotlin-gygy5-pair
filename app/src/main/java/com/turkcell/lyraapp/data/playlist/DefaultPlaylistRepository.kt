package com.turkcell.lyraapp.data.playlist

import com.turkcell.lyraapp.data.auth.AuthRepository
import com.turkcell.lyraapp.data.favorites.FavoritesRepository
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.roundToInt

@Singleton
class DefaultPlaylistRepository @Inject constructor(
    private val playlistApi: PlaylistApi,
    private val authRepository: AuthRepository,
    private val favoritesRepository: FavoritesRepository
) : PlaylistRepository {

    private val favoritePlaylists = mutableSetOf<String>()
    private val downloadedPlaylists = mutableSetOf<String>()
    private val shuffledPlaylists = mutableSetOf<String>()
    private val playingPlaylists = mutableSetOf<String>()

    override suspend fun getPlaylistDetail(playlistId: String): Result<PlaylistDetail> = runCatching {
        val playlistDto = playlistApi.getPlaylistDetail(playlistId).data
        val songs = playlistDto.songs.map { songDto ->
            val (startColor, endColor) = artworkColorsFor(songDto.id)
            val durationMs = songDto.durationMs ?: 0
            val durationText = formatTime(durationMs.toLong())
            PlaylistSong(
                id = songDto.id,
                title = songDto.title,
                artist = songDto.artist,
                duration = durationText,
                isLiked = favoritesRepository.isFavorite(songDto.id),
                isPlaying = false,
                artworkStartColor = startColor,
                artworkEndColor = endColor
            )
        }

        val totalDurationMs = playlistDto.songs.sumOf { it.durationMs ?: 0 }.toLong()
        val durationText = if (totalDurationMs > 0) {
            val totalSeconds = totalDurationMs / 1000
            val minutes = totalSeconds / 60
            "$minutes dk"
        } else {
            "0 dk"
        }

        val (startColor, endColor) = artworkColorsFor(playlistDto.id)
        val creator = if (playlistDto.ownerId != null) "Kitaplığın" else "Lyra"

        PlaylistDetail(
            id = playlistDto.id,
            title = playlistDto.name,
            description = playlistDto.description ?: "",
            creator = creator,
            songCount = songs.size,
            durationText = durationText,
            artworkStartColor = startColor,
            artworkEndColor = endColor,
            isFavorite = favoritePlaylists.contains(playlistId),
            isDownloaded = downloadedPlaylists.contains(playlistId),
            isShuffleEnabled = shuffledPlaylists.contains(playlistId),
            isPlaying = playingPlaylists.contains(playlistId),
            songs = songs
        )
    }

    override suspend fun toggleLikeSong(playlistId: String, songId: String): Result<Unit> = runCatching {
        val detail = getPlaylistDetail(playlistId).getOrThrow()
        val song = detail.songs.find { it.id == songId } ?: throw NoSuchElementException("Şarkı bulunamadı.")
        
        val favoriteSong = com.turkcell.lyraapp.data.favorites.FavoriteSong(
            id = song.id,
            title = song.title,
            artist = song.artist,
            duration = song.duration,
            isLiked = !song.isLiked,
            isPlaying = song.isPlaying,
            artworkStartColor = song.artworkStartColor,
            artworkEndColor = song.artworkEndColor
        )
        favoritesRepository.toggleFavorite(favoriteSong).getOrThrow()
    }

    override suspend fun togglePlaylistFavorite(playlistId: String): Result<Unit> = runCatching {
        if (favoritePlaylists.contains(playlistId)) {
            favoritePlaylists.remove(playlistId)
        } else {
            favoritePlaylists.add(playlistId)
        }
    }

    override suspend fun togglePlaylistDownload(playlistId: String): Result<Unit> = runCatching {
        if (downloadedPlaylists.contains(playlistId)) {
            downloadedPlaylists.remove(playlistId)
        } else {
            downloadedPlaylists.add(playlistId)
        }
    }

    override suspend fun toggleShuffle(playlistId: String): Result<Unit> = runCatching {
        if (shuffledPlaylists.contains(playlistId)) {
            shuffledPlaylists.remove(playlistId)
        } else {
            shuffledPlaylists.add(playlistId)
        }
    }

    override suspend fun playPlaylist(playlistId: String): Result<Unit> = runCatching {
        if (playingPlaylists.contains(playlistId)) {
            playingPlaylists.remove(playlistId)
        } else {
            playingPlaylists.add(playlistId)
        }
    }

    override suspend fun deletePlaylist(playlistId: String): Result<Unit> = runCatching {
        val token = authRepository.getAccessToken() ?: throw IllegalStateException("Oturum açılmadı. Lütfen giriş yapın.")
        playlistApi.deletePlaylist("Bearer $token", playlistId)
    }

    private companion object {
        fun formatTime(ms: Long): String {
            if (ms <= 0) return "0:00"
            val totalSeconds = ms / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format(Locale.US, "%d:%02d", minutes, seconds)
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

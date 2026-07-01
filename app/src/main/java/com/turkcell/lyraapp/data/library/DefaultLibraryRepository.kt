package com.turkcell.lyraapp.data.library

import com.turkcell.lyraapp.data.auth.AuthRepository
import com.turkcell.lyraapp.data.playlist.PlaylistApi
import com.turkcell.lyraapp.data.songs.SongsApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.roundToInt

@Singleton
class DefaultLibraryRepository @Inject constructor(
    private val playlistApi: PlaylistApi,
    private val songsApi: SongsApi,
    private val authRepository: AuthRepository
) : LibraryRepository {

    override suspend fun getLibraryPlaylists(): Result<List<LibraryPlaylist>> = runCatching {
        val token = authRepository.getAccessToken() ?: throw IllegalStateException("Oturum açılmadı. Lütfen giriş yapın.")
        val playlists = playlistApi.getMyPlaylists("Bearer $token").data
        
        coroutineScope {
            playlists.map { playlist ->
                async {
                    val detailResult = runCatching { playlistApi.getPlaylistDetail(playlist.id) }
                    val songCount = detailResult.getOrNull()?.data?.songs?.size ?: 0
                    val (startColor, endColor) = artworkColorsFor(playlist.id)
                    LibraryPlaylist(
                        id = playlist.id,
                        title = playlist.name,
                        type = "Çalma listesi",
                        songCount = songCount,
                        isPinned = false,
                        artworkStartColor = startColor,
                        artworkEndColor = endColor
                    )
                }
            }.awaitAll()
        }
    }

    override suspend fun getAvailableSongs(): Result<List<LibrarySong>> = runCatching {
        val songsDto = songsApi.getSongs(limit = 100).data
        songsDto.map { songDto ->
            val (startColor, endColor) = artworkColorsFor(songDto.id)
            LibrarySong(
                id = songDto.id,
                title = songDto.title,
                artist = songDto.artist,
                artworkStartColor = startColor,
                artworkEndColor = endColor
            )
        }
    }

    override suspend fun createPlaylist(
        title: String,
        description: String,
        isPublic: Boolean,
        songIds: List<String>
    ): Result<Unit> = runCatching {
        val token = authRepository.getAccessToken() ?: throw IllegalStateException("Oturum açılmadı. Lütfen giriş yapın.")
        
        val createResponse = playlistApi.createPlaylist(
            authorization = "Bearer $token",
            request = com.turkcell.lyraapp.data.playlist.CreatePlaylistRequestDto(
                name = title,
                description = description.takeIf { it.isNotBlank() }
            )
        )
        val playlistId = createResponse.data.id
        
        for (songId in songIds) {
            playlistApi.addTrackToPlaylist(
                authorization = "Bearer $token",
                id = playlistId,
                request = com.turkcell.lyraapp.data.playlist.AddTrackRequestDto(songId = songId)
            )
        }
    }

    private companion object {
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

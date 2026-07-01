package com.turkcell.lyraapp.data.playlist

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import com.turkcell.lyraapp.data.favorites.FavoritesRepository
import com.turkcell.lyraapp.data.favorites.FavoriteSong

@Singleton
class FakePlaylistRepository @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) : PlaylistRepository {

    private var currentPlaylist = PlaylistDetail(
        id = "lib-2",
        title = "Gece Sürüşü",
        description = "Karanlık yollar için synth-pop",
        creator = "Zeynep Kaya",
        songCount = 6,
        durationText = "23 dk",
        artworkStartColor = 0xFF8B6FB8,
        artworkEndColor = 0xFF4A3D6B,
        isFavorite = false,
        isDownloaded = false,
        isShuffleEnabled = false,
        isPlaying = false,
        songs = listOf(
            PlaylistSong("song-1", "Neon Sokaklar", "Şehir Işıkları", "3:43", isLiked = true, isPlaying = true, 0xFFD98E4A, 0xFF8A5526),
            PlaylistSong("song-2", "Gece Yarısı", "Mavi Deniz", "3:34", isLiked = true, isPlaying = false, 0xFF6FBF5A, 0xFF356B2A),
            PlaylistSong("song-3", "Mor Bulutlar", "Derin Kaya", "3:52", isLiked = false, isPlaying = false, 0xFF4AC2A8, 0xFF1F6E5C),
            PlaylistSong("song-4", "Son Tren", "Peron", "3:37", isLiked = false, isPlaying = false, 0xFF4AC2A8, 0xFF1F6E5C),
            PlaylistSong("song-5", "Yıldız Tozu", "Polaris", "4:07", isLiked = true, isPlaying = false, 0xFF3D5A80, 0xFF1B2A45),
            PlaylistSong("song-6", "Sessiz Şehir", "Ela Tuna", "4:10", isLiked = false, isPlaying = false, 0xFF8B6FB8, 0xFF4A3D6B)
        )
    )

    override suspend fun getPlaylistDetail(playlistId: String): Result<PlaylistDetail> {
        delay(600L)
        val updatedSongs = currentPlaylist.songs.map { song ->
            song.copy(isLiked = favoritesRepository.isFavorite(song.id))
        }
        currentPlaylist = currentPlaylist.copy(songs = updatedSongs)
        return Result.success(currentPlaylist.copy(id = playlistId))
    }

    override suspend fun toggleLikeSong(playlistId: String, songId: String): Result<Unit> {
        delay(200L)
        val song = currentPlaylist.songs.find { it.id == songId } ?: return Result.failure(Exception("Şarkı bulunamadı."))
        val favoriteSong = FavoriteSong(
            id = song.id,
            title = song.title,
            artist = song.artist,
            duration = song.duration,
            isLiked = !song.isLiked,
            isPlaying = song.isPlaying,
            artworkStartColor = song.artworkStartColor,
            artworkEndColor = song.artworkEndColor
        )
        val result = favoritesRepository.toggleFavorite(favoriteSong)
        if (result.isSuccess) {
            val updatedSongs = currentPlaylist.songs.map { s ->
                if (s.id == songId) {
                    s.copy(isLiked = !s.isLiked)
                } else {
                    s
                }
            }
            currentPlaylist = currentPlaylist.copy(songs = updatedSongs)
        }
        return result
    }

    override suspend fun togglePlaylistFavorite(playlistId: String): Result<Unit> {
        delay(200L)
        currentPlaylist = currentPlaylist.copy(isFavorite = !currentPlaylist.isFavorite)
        return Result.success(Unit)
    }

    override suspend fun togglePlaylistDownload(playlistId: String): Result<Unit> {
        delay(200L)
        currentPlaylist = currentPlaylist.copy(isDownloaded = !currentPlaylist.isDownloaded)
        return Result.success(Unit)
    }

    override suspend fun toggleShuffle(playlistId: String): Result<Unit> {
        delay(200L)
        currentPlaylist = currentPlaylist.copy(isShuffleEnabled = !currentPlaylist.isShuffleEnabled)
        return Result.success(Unit)
    }

    override suspend fun playPlaylist(playlistId: String): Result<Unit> {
        delay(200L)
        currentPlaylist = currentPlaylist.copy(isPlaying = !currentPlaylist.isPlaying)
        return Result.success(Unit)
    }
}

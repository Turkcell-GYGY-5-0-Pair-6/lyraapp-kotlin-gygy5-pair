package com.turkcell.lyraapp.data.playlist

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakePlaylistRepository @Inject constructor() : PlaylistRepository {

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
        // Farklı playlist ID'leri gelirse de prototip/showcase amaçlı Gece Sürüşü'nü dönüyoruz,
        // ancak id bilgisini gelen playlistId ile güncelliyoruz.
        return Result.success(currentPlaylist.copy(id = playlistId))
    }

    override suspend fun toggleLikeSong(playlistId: String, songId: String): Result<Unit> {
        delay(200L)
        val updatedSongs = currentPlaylist.songs.map { song ->
            if (song.id == songId) {
                song.copy(isLiked = !song.isLiked)
            } else {
                song
            }
        }
        currentPlaylist = currentPlaylist.copy(songs = updatedSongs)
        return Result.success(Unit)
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

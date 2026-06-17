package com.turkcell.lyraapp.data.library

import kotlinx.coroutines.delay
import javax.inject.Inject

class FakeLibraryRepository @Inject constructor() : LibraryRepository {
    override suspend fun getLibraryPlaylists(): Result<List<LibraryPlaylist>> {
        delay(800L) // Ağ gecikmesi simülasyonu
        return Result.success(PLAYLISTS.toList())
    }

    override suspend fun getAvailableSongs(): Result<List<LibrarySong>> {
        delay(400L)
        return Result.success(AVAILABLE_SONGS)
    }

    override suspend fun createPlaylist(
        title: String,
        description: String,
        isPublic: Boolean,
        songIds: List<String>
    ): Result<Unit> {
        delay(800L)
        // Yeni çalma listesi oluştur
        val newPlaylist = LibraryPlaylist(
            id = "lib-${PLAYLISTS.size + 1}",
            title = title,
            type = "Çalma listesi",
            songCount = songIds.size,
            isPinned = false,
            // Önizlemedeki gibi turuncu-kahve gradyanı varsayılan olarak kullanılır
            artworkStartColor = 0xFFD98E4A,
            artworkEndColor = 0xFF8A5526
        )
        PLAYLISTS.add(newPlaylist)
        return Result.success(Unit)
    }

    private companion object {
        val PLAYLISTS = mutableListOf(
            LibraryPlaylist(
                id = "lib-1",
                title = "Beğenilen Şarkılar",
                type = "Çalma listesi",
                songCount = 5,
                isPinned = true,
                artworkStartColor = 0xFFFFD9E2,
                artworkEndColor = 0xFF8F4A5F
            ),
            LibraryPlaylist(
                id = "lib-2",
                title = "Gece Sürüşü",
                type = "Çalma listesi",
                songCount = 6,
                isPinned = false,
                artworkStartColor = 0xFF8B6FB8,
                artworkEndColor = 0xFF4A3D6B
            ),
            LibraryPlaylist(
                id = "lib-3",
                title = "Sabah Kahvesi",
                type = "Çalma listesi",
                songCount = 5,
                isPinned = false,
                artworkStartColor = 0xFF7C83D9,
                artworkEndColor = 0xFF3E4486
            ),
            LibraryPlaylist(
                id = "lib-4",
                title = "Odaklan",
                type = "Çalma listesi",
                songCount = 5,
                isPinned = false,
                artworkStartColor = 0xFF4AC2A8,
                artworkEndColor = 0xFF1F6E5C
            ),
            LibraryPlaylist(
                id = "lib-5",
                title = "Yaz Anıları",
                type = "Çalma listesi",
                songCount = 5,
                isPinned = false,
                artworkStartColor = 0xFF5AAFC9,
                artworkEndColor = 0xFF2A5F73
            ),
            LibraryPlaylist(
                id = "lib-6",
                title = "Akustik Akşam",
                type = "Çalma listesi",
                songCount = 4,
                isPinned = false,
                artworkStartColor = 0xFF3FAE9C,
                artworkEndColor = 0xFF1E5D52
            )
        )

        val AVAILABLE_SONGS = listOf(
            LibrarySong("song-1", "Gece Yarısı", "Mavi Deniz", 0xFF6FBF5A, 0xFF356B2A),
            LibrarySong("song-2", "Sessiz Şehir", "Ela Tuna", 0xFF8B6FB8, 0xFF4A3D6B),
            LibrarySong("song-3", "Yıldız Tozu", "Polaris", 0xFF3D5A80, 0xFF1B2A45),
            LibrarySong("song-4", "Sahil Yolu", "Kumsal", 0xFFD98E4A, 0xFF8A5526),
            LibrarySong("song-5", "Mor Bulutlar", "Derin Kaya", 0xFF4AC2A8, 0xFF1F6E5C),
            LibrarySong("song-6", "İlk Işık", "Sabah Ezgisi", 0xFF5AAFC9, 0xFF2A5F73),
            LibrarySong("song-7", "Kayıp Anlar", "Eko", 0xFF3FAE9C, 0xFF1E5D52)
        )
    }
}


package com.turkcell.lyraapp.data.library

import kotlinx.coroutines.delay
import javax.inject.Inject

class FakeLibraryRepository @Inject constructor() : LibraryRepository {
    override suspend fun getLibraryPlaylists(): Result<List<LibraryPlaylist>> {
        delay(800L) // Ağ gecikmesi simülasyonu
        return Result.success(PLAYLISTS)
    }

    private companion object {
        val PLAYLISTS = listOf(
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
    }
}

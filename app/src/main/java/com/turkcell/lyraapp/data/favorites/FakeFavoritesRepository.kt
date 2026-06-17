package com.turkcell.lyraapp.data.favorites

import kotlinx.coroutines.delay
import javax.inject.Inject

class FakeFavoritesRepository @Inject constructor() : FavoritesRepository {
    override suspend fun getFavoriteSongs(): Result<List<FavoriteSong>> {
        delay(NETWORK_DELAY_MS)
        return Result.success(FAVORITE_SONGS)
    }

    companion object {
        private const val NETWORK_DELAY_MS = 600L

        private val FAVORITE_SONGS = listOf(
            FavoriteSong(
                id = "fav-1",
                title = "Gece Yarısı",
                artist = "Mavi Deniz",
                duration = "3:34",
                isLiked = true,
                isPlaying = false,
                artworkStartColor = 0xFF6FBF5A,
                artworkEndColor = 0xFF356B2A
            ),
            FavoriteSong(
                id = "fav-2",
                title = "Yıldız Tozu",
                artist = "Polaris",
                duration = "4:07",
                isLiked = true,
                isPlaying = false,
                artworkStartColor = 0xFF3D5A80,
                artworkEndColor = 0xFF1B2A45
            ),
            FavoriteSong(
                id = "fav-3",
                title = "İlk Işık",
                artist = "Sabah Ezgisi",
                duration = "3:25",
                isLiked = true,
                isPlaying = false,
                artworkStartColor = 0xFF5AAFC9,
                artworkEndColor = 0xFF2A5F73
            ),
            FavoriteSong(
                id = "fav-4",
                title = "Neon Sokaklar",
                artist = "Şehir Işıkları",
                duration = "3:43",
                isLiked = true,
                isPlaying = true,
                artworkStartColor = 0xFFD98E4A,
                artworkEndColor = 0xFF8A5526
            ),
            FavoriteSong(
                id = "fav-5",
                title = "Derin Mavi",
                artist = "Okyanus",
                duration = "4:29",
                isLiked = true,
                isPlaying = false,
                artworkStartColor = 0xFF6FBF5A,
                artworkEndColor = 0xFF356B2A
            )
        )
    }
}

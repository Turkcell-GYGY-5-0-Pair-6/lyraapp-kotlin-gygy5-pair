package com.turkcell.lyraapp.data.favorites

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeFavoritesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json
) : FavoritesRepository {

    private val _favoriteSongsFlow = MutableStateFlow<List<FavoriteSong>>(emptyList())
    override val favoriteSongsFlow: Flow<List<FavoriteSong>> = _favoriteSongsFlow.asStateFlow()

    private val favoritesFile = File(context.filesDir, "favorites.json")

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        try {
            if (favoritesFile.exists()) {
                val jsonText = favoritesFile.readText()
                val list = json.decodeFromString<List<FavoriteSong>>(jsonText)
                _favoriteSongsFlow.value = list
            } else {
                _favoriteSongsFlow.value = emptyList()
            }
        } catch (e: Exception) {
            _favoriteSongsFlow.value = emptyList()
        }
    }

    private fun saveFavoritesDirectly(list: List<FavoriteSong>) {
        try {
            val jsonText = json.encodeToString(list)
            favoritesFile.writeText(jsonText)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private suspend fun saveFavorites(list: List<FavoriteSong>) = withContext(Dispatchers.IO) {
        saveFavoritesDirectly(list)
    }

    override suspend fun getFavoriteSongs(): Result<List<FavoriteSong>> {
        delay(NETWORK_DELAY_MS)
        return Result.success(_favoriteSongsFlow.value)
    }

    override suspend fun isFavorite(songId: String): Boolean {
        return _favoriteSongsFlow.value.any { it.id == songId }
    }

    override suspend fun toggleFavorite(song: FavoriteSong): Result<Unit> {
        val currentList = _favoriteSongsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == song.id }
        if (index != -1) {
            if (!song.isLiked) {
                currentList.removeAt(index)
            } else {
                currentList[index] = song
            }
        } else {
            if (song.isLiked) {
                currentList.add(song)
            }
        }
        _favoriteSongsFlow.value = currentList
        saveFavorites(currentList)
        return Result.success(Unit)
    }

    companion object {
        private const val NETWORK_DELAY_MS = 600L
    }
}

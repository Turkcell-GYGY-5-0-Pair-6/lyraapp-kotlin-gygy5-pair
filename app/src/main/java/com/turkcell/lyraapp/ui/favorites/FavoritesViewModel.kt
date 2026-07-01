package com.turkcell.lyraapp.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.favorites.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Favoriler ekranı için ViewModel.
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val _effect = Channel<FavoritesEffect>(Channel.BUFFERED)
    val effect: Flow<FavoritesEffect> = _effect.receiveAsFlow()

    init {
        onIntent(FavoritesIntent.LoadSongs)
    }

    fun onIntent(intent: FavoritesIntent) {
        when (intent) {
            is FavoritesIntent.LoadSongs -> loadSongs()
            is FavoritesIntent.PlayAll -> playAll()
            is FavoritesIntent.ToggleShuffle -> _uiState.update { it.copy(isShuffleEnabled = !it.isShuffleEnabled) }
            is FavoritesIntent.ToggleDownload -> _uiState.update { it.copy(isDownloaded = !it.isDownloaded) }
            is FavoritesIntent.SongClicked -> handleSongClicked(intent.songId)
            is FavoritesIntent.ToggleLikeSong -> handleToggleLikeSong(intent.songId)
            is FavoritesIntent.BackClicked -> viewModelScope.launch {
                _effect.send(FavoritesEffect.NavigateBack)
            }
        }
    }

    private var isCollecting = false

    private fun loadSongs() {
        if (isCollecting) return
        isCollecting = true
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            favoritesRepository.favoriteSongsFlow.collect { songs ->
                _uiState.update { it.copy(songs = songs, isLoading = false) }
            }
        }
    }

    private fun playAll() {
        val currentSongs = _uiState.value.songs
        if (currentSongs.isEmpty()) return
        val firstSongId = currentSongs.first().id
        _uiState.update { state ->
            val updatedSongs = state.songs.mapIndexed { index, song ->
                song.copy(isPlaying = index == 0)
            }
            state.copy(songs = updatedSongs)
        }
        viewModelScope.launch {
            _effect.send(FavoritesEffect.NavigateToNowPlaying(firstSongId))
        }
    }

    private fun handleSongClicked(songId: String) {
        _uiState.update { state ->
            val updatedSongs = state.songs.map { song ->
                song.copy(isPlaying = song.id == songId)
            }
            state.copy(songs = updatedSongs)
        }
        viewModelScope.launch {
            _effect.send(FavoritesEffect.NavigateToNowPlaying(songId))
        }
    }

    private fun handleToggleLikeSong(songId: String) {
        viewModelScope.launch {
            val song = _uiState.value.songs.find { it.id == songId } ?: return@launch
            val updatedSong = song.copy(isLiked = !song.isLiked)
            favoritesRepository.toggleFavorite(updatedSong)
        }
    }
}

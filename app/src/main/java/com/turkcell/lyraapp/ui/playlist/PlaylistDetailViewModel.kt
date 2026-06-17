package com.turkcell.lyraapp.ui.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.playlist.PlaylistRepository
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

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PlaylistDetailEffect>(Channel.BUFFERED)
    val effect: Flow<PlaylistDetailEffect> = _effect.receiveAsFlow()

    private val playlistId: String? = savedStateHandle["playlistId"]

    init {
        playlistId?.let {
            onIntent(PlaylistDetailIntent.LoadPlaylist(it))
        }
    }

    fun onIntent(intent: PlaylistDetailIntent) {
        when (intent) {
            is PlaylistDetailIntent.LoadPlaylist -> loadPlaylist(intent.playlistId)
            is PlaylistDetailIntent.ToggleLikeSong -> toggleLikeSong(intent.songId)
            PlaylistDetailIntent.TogglePlaylistFavorite -> togglePlaylistFavorite()
            PlaylistDetailIntent.TogglePlaylistDownload -> togglePlaylistDownload()
            PlaylistDetailIntent.ToggleShuffle -> toggleShuffle()
            PlaylistDetailIntent.PlayPlaylist -> playPlaylist()
            is PlaylistDetailIntent.SongClicked -> {
                viewModelScope.launch {
                    _effect.send(PlaylistDetailEffect.NavigateToNowPlaying(intent.songId))
                }
            }
            PlaylistDetailIntent.BackClicked -> {
                viewModelScope.launch {
                    _effect.send(PlaylistDetailEffect.NavigateBack)
                }
            }
        }
    }

    private fun loadPlaylist(id: String) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = playlistRepository.getPlaylistDetail(id)
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess { detail ->
                    _uiState.update { it.copy(playlist = detail) }
                }
                .onFailure { error ->
                    val errorMsg = error.message ?: "Playlist detayları yüklenemedi."
                    _uiState.update { it.copy(error = errorMsg) }
                    _effect.send(PlaylistDetailEffect.ShowError(errorMsg))
                }
        }
    }

    private fun toggleLikeSong(songId: String) {
        val currentId = playlistId ?: return
        viewModelScope.launch {
            val result = playlistRepository.toggleLikeSong(currentId, songId)
            result.onSuccess {
                // Local state'i güncellemek için playlist'i tekrar yükle
                reloadPlaylistDirectly(currentId)
            }.onFailure { error ->
                _effect.send(PlaylistDetailEffect.ShowError(error.message ?: "Şarkı beğeni durumu güncellenemedi."))
            }
        }
    }

    private fun togglePlaylistFavorite() {
        val currentId = playlistId ?: return
        viewModelScope.launch {
            val result = playlistRepository.togglePlaylistFavorite(currentId)
            result.onSuccess {
                reloadPlaylistDirectly(currentId)
            }.onFailure { error ->
                _effect.send(PlaylistDetailEffect.ShowError(error.message ?: "Çalma listesi favori durumu güncellenemedi."))
            }
        }
    }

    private fun togglePlaylistDownload() {
        val currentId = playlistId ?: return
        viewModelScope.launch {
            val result = playlistRepository.togglePlaylistDownload(currentId)
            result.onSuccess {
                reloadPlaylistDirectly(currentId)
            }.onFailure { error ->
                _effect.send(PlaylistDetailEffect.ShowError(error.message ?: "İndirme durumu güncellenemedi."))
            }
        }
    }

    private fun toggleShuffle() {
        val currentId = playlistId ?: return
        viewModelScope.launch {
            val result = playlistRepository.toggleShuffle(currentId)
            result.onSuccess {
                reloadPlaylistDirectly(currentId)
            }.onFailure { error ->
                _effect.send(PlaylistDetailEffect.ShowError(error.message ?: "Karıştırma modu güncellenemedi."))
            }
        }
    }

    private fun playPlaylist() {
        val currentId = playlistId ?: return
        viewModelScope.launch {
            val result = playlistRepository.playPlaylist(currentId)
            result.onSuccess {
                reloadPlaylistDirectly(currentId)
            }.onFailure { error ->
                _effect.send(PlaylistDetailEffect.ShowError(error.message ?: "Çalma listesi oynatılamadı."))
            }
        }
    }

    private suspend fun reloadPlaylistDirectly(id: String) {
        playlistRepository.getPlaylistDetail(id).onSuccess { detail ->
            _uiState.update { it.copy(playlist = detail) }
        }
    }
}

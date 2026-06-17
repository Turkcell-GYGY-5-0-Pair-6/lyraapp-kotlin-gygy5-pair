package com.turkcell.lyraapp.ui.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.player.PlayerRepository
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
class NowPlayingViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(NowPlayingUiState())
    val uiState: StateFlow<NowPlayingUiState> = _uiState.asStateFlow()

    private val _effect = Channel<NowPlayingEffect>(Channel.BUFFERED)
    val effect: Flow<NowPlayingEffect> = _effect.receiveAsFlow()

    private val songId: String? = savedStateHandle["songId"]

    init {
        viewModelScope.launch {
            playerRepository.playbackStateFlow.collect { state ->
                state?.let { s ->
                    _uiState.update { it.copy(playbackState = s) }
                }
            }
        }
        songId?.let {
            onIntent(NowPlayingIntent.LoadSong(it))
        }
    }

    fun onIntent(intent: NowPlayingIntent) {
        when (intent) {
            is NowPlayingIntent.LoadSong -> loadSong(intent.songId)
            NowPlayingIntent.TogglePlayPause -> togglePlayPause()
            NowPlayingIntent.ToggleLike -> toggleLike()
            NowPlayingIntent.ToggleShuffle -> toggleShuffle()
            NowPlayingIntent.ToggleRepeat -> toggleRepeat()
            is NowPlayingIntent.SeekTo -> seekTo(intent.progressMs)
            NowPlayingIntent.SkipNext -> skipToNext()
            NowPlayingIntent.SkipPrevious -> skipToPrevious()
            NowPlayingIntent.BackClicked -> {
                viewModelScope.launch {
                    _effect.send(NowPlayingEffect.NavigateBack)
                }
            }
        }
    }

    private fun loadSong(id: String) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = playerRepository.getPlaybackState(id)
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess { state ->
                    _uiState.update { it.copy(playbackState = state) }
                }
                .onFailure { error ->
                    val errorMsg = error.message ?: "Şarkı yüklenemedi."
                    _uiState.update { it.copy(error = errorMsg) }
                    _effect.send(NowPlayingEffect.ShowError(errorMsg))
                }
        }
    }

    private fun togglePlayPause() {
        viewModelScope.launch {
            playerRepository.togglePlayPause().onFailure { error ->
                _effect.send(NowPlayingEffect.ShowError(error.message ?: "Oynatma durumu değiştirilemedi."))
            }
        }
    }

    private fun toggleLike() {
        viewModelScope.launch {
            playerRepository.toggleLike().onFailure { error ->
                _effect.send(NowPlayingEffect.ShowError(error.message ?: "Beğeni durumu değiştirilemedi."))
            }
        }
    }

    private fun toggleShuffle() {
        viewModelScope.launch {
            playerRepository.toggleShuffle().onFailure { error ->
                _effect.send(NowPlayingEffect.ShowError(error.message ?: "Karıştırma durumu değiştirilemedi."))
            }
        }
    }

    private fun toggleRepeat() {
        viewModelScope.launch {
            playerRepository.toggleRepeat().onFailure { error ->
                _effect.send(NowPlayingEffect.ShowError(error.message ?: "Tekrarlama durumu değiştirilemedi."))
            }
        }
    }

    private fun seekTo(progressMs: Long) {
        viewModelScope.launch {
            playerRepository.seekTo(progressMs).onFailure { error ->
                _effect.send(NowPlayingEffect.ShowError(error.message ?: "Konum değiştirilemedi."))
            }
        }
    }

    private fun skipToNext() {
        viewModelScope.launch {
            playerRepository.skipToNext().onFailure { error ->
                _effect.send(NowPlayingEffect.ShowError(error.message ?: "Sonraki şarkıya geçilemedi."))
            }
        }
    }

    private fun skipToPrevious() {
        viewModelScope.launch {
            playerRepository.skipToPrevious().onFailure { error ->
                _effect.send(NowPlayingEffect.ShowError(error.message ?: "Önceki şarkıya geçilemedi."))
            }
        }
    }
}

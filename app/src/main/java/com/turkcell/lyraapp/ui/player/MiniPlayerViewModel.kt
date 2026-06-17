package com.turkcell.lyraapp.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.player.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MiniPlayerViewModel @Inject constructor(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiniPlayerUiState())
    val uiState: StateFlow<MiniPlayerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            playerRepository.playbackStateFlow.collect { state ->
                _uiState.update { it.copy(playbackState = state) }
            }
        }
    }

    fun onIntent(intent: MiniPlayerIntent) {
        when (intent) {
            MiniPlayerIntent.TogglePlayPause -> {
                viewModelScope.launch {
                    playerRepository.togglePlayPause()
                }
            }
            MiniPlayerIntent.ToggleLike -> {
                viewModelScope.launch {
                    playerRepository.toggleLike()
                }
            }
            MiniPlayerIntent.SkipNext -> {
                viewModelScope.launch {
                    playerRepository.skipToNext()
                }
            }
            MiniPlayerIntent.SkipPrevious -> {
                viewModelScope.launch {
                    playerRepository.skipToPrevious()
                }
            }
        }
    }
}

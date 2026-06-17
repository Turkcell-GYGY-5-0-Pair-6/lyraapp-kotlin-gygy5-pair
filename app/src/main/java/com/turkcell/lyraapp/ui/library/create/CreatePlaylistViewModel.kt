package com.turkcell.lyraapp.ui.library.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.library.LibraryRepository
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
 * Yeni Çalma Listesi ekleme ekranının ViewModel'i.
 */
@HiltViewModel
class CreatePlaylistViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePlaylistUiState())
    val uiState: StateFlow<CreatePlaylistUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CreatePlaylistEffect>(Channel.BUFFERED)
    val effect: Flow<CreatePlaylistEffect> = _effect.receiveAsFlow()

    init {
        onIntent(CreatePlaylistIntent.LoadSongs)
    }

    fun onIntent(intent: CreatePlaylistIntent) {
        when (intent) {
            is CreatePlaylistIntent.LoadSongs -> loadSongs()
            is CreatePlaylistIntent.TitleChanged -> updateTitle(intent.value)
            is CreatePlaylistIntent.DescriptionChanged -> {
                _uiState.update { it.copy(description = intent.value) }
            }
            is CreatePlaylistIntent.ToggleSongSelection -> toggleSongSelection(intent.songId)
            is CreatePlaylistIntent.SetPublic -> {
                _uiState.update { it.copy(isPublic = intent.value) }
            }
            is CreatePlaylistIntent.SavePlaylist -> savePlaylist()
            is CreatePlaylistIntent.CancelClicked -> viewModelScope.launch {
                _effect.send(CreatePlaylistEffect.NavigateBack)
            }
            is CreatePlaylistIntent.ChangeCoverClicked -> {
                // Önizleme kapak değişimi simülasyonu
            }
        }
    }

    private fun loadSongs() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = libraryRepository.getAvailableSongs()
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess { songs ->
                    _uiState.update { it.copy(songs = songs) }
                }
                .onFailure { error ->
                    _effect.send(CreatePlaylistEffect.ShowError(error.message ?: "Şarkılar yüklenemedi."))
                }
        }
    }

    private fun updateTitle(title: String) {
        _uiState.update { current ->
            current.copy(
                title = title,
                isSaveEnabled = title.isNotBlank()
            )
        }
    }

    private fun toggleSongSelection(songId: String) {
        _uiState.update { current ->
            val updatedSelection = current.selectedSongIds.toMutableSet()
            if (updatedSelection.contains(songId)) {
                updatedSelection.remove(songId)
            } else {
                updatedSelection.add(songId)
            }
            current.copy(selectedSongIds = updatedSelection)
        }
    }

    private fun savePlaylist() {
        val state = _uiState.value
        if (!state.isSaveEnabled || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = libraryRepository.createPlaylist(
                title = state.title,
                description = state.description,
                isPublic = state.isPublic,
                songIds = state.selectedSongIds.toList()
            )
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess {
                    _effect.send(CreatePlaylistEffect.SaveSuccess)
                }
                .onFailure { error ->
                    _effect.send(CreatePlaylistEffect.ShowError(error.message ?: "Çalma listesi oluşturulamadı."))
                }
        }
    }
}

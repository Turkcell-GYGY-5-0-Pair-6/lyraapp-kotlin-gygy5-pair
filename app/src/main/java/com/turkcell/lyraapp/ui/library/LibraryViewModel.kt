package com.turkcell.lyraapp.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.library.LibraryRepository
import com.turkcell.lyraapp.data.library.LibraryPlaylist
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
 * Kütüphane ekranı durumunu ve iş mantığını yöneten Hilt ViewModel sınıfı.
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LibraryEffect>(Channel.BUFFERED)
    val effect: Flow<LibraryEffect> = _effect.receiveAsFlow()

    init {
        onIntent(LibraryIntent.LoadLibrary)
    }

    fun onIntent(intent: LibraryIntent) {
        when (intent) {
            is LibraryIntent.LoadLibrary -> loadLibrary()
            is LibraryIntent.FilterSelected -> {
                _uiState.update { it.copy(selectedFilter = intent.filter) }
            }
            is LibraryIntent.AddPlaylistClicked -> {
                viewModelScope.launch {
                    _effect.send(LibraryEffect.NavigateToCreatePlaylist)
                }
            }
            is LibraryIntent.ToggleSearch -> {
                _uiState.update { current ->
                    val isSearching = !current.isSearching
                    val query = if (isSearching) current.searchQuery else ""
                    current.copy(
                        isSearching = isSearching,
                        searchQuery = query,
                        filteredPlaylists = filterPlaylists(current.playlists, query)
                    )
                }
            }
            is LibraryIntent.SearchQueryChanged -> {
                _uiState.update { current ->
                    current.copy(
                        searchQuery = intent.query,
                        filteredPlaylists = filterPlaylists(current.playlists, intent.query)
                    )
                }
            }
            is LibraryIntent.PlaylistClicked -> {
                viewModelScope.launch {
                    _effect.send(LibraryEffect.NavigateToPlaylistDetail(intent.playlistId))
                }
            }
        }
    }

    private fun loadLibrary() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = libraryRepository.getLibraryPlaylists()
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess { list ->
                    _uiState.update { current ->
                        current.copy(
                            playlists = list,
                            filteredPlaylists = filterPlaylists(list, current.searchQuery)
                        )
                    }
                }
                .onFailure { error ->
                    val errorMsg = error.message ?: "Kütüphane listesi yüklenemedi."
                    _uiState.update { it.copy(error = errorMsg) }
                    _effect.send(LibraryEffect.ShowError(errorMsg))
                }
        }
    }

    private fun filterPlaylists(list: List<LibraryPlaylist>, query: String): List<LibraryPlaylist> {
        if (query.isBlank()) return list
        return list.filter { it.title.contains(query, ignoreCase = true) }
    }
}

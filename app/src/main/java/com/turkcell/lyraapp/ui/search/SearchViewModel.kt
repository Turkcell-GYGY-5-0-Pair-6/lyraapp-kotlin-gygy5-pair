package com.turkcell.lyraapp.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.search.SearchRepository
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
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState(isLoading = true))
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SearchEffect>(Channel.BUFFERED)
    val effect: Flow<SearchEffect> = _effect.receiveAsFlow()

    init {
        loadCategories()
    }

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> _uiState.update { it.copy(query = intent.value) }
            is SearchIntent.FilterSelected -> _uiState.update { it.copy(selectedFilter = intent.filter) }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = searchRepository.getSearchCategories()
            _uiState.update { it.copy(isLoading = false) }

            result.onSuccess { categories ->
                _uiState.update { it.copy(categories = categories) }
            }.onFailure { error ->
                _effect.send(SearchEffect.ShowError(error.message ?: "Kategoriler yüklenemedi."))
            }
        }
    }
}

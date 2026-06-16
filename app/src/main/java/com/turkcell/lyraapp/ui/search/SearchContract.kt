package com.turkcell.lyraapp.ui.search

import com.turkcell.lyraapp.data.search.SearchCategory

/**
 * Search ekranının MVI sözleşmesi: State, Intent ve Effect.
 */

data class SearchUiState(
    val query: String = "",
    val selectedFilter: String = "Hepsi",
    val filters: List<String> = listOf("Hepsi", "Pop", "Elektronik", "Akustik"),
    val categories: List<SearchCategory> = emptyList(),
    val isLoading: Boolean = false,
)

sealed interface SearchIntent {
    data class QueryChanged(val value: String) : SearchIntent
    data class FilterSelected(val filter: String) : SearchIntent
}

sealed interface SearchEffect {
    data class ShowError(val message: String) : SearchEffect
}

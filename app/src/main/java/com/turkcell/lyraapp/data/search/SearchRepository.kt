package com.turkcell.lyraapp.data.search

interface SearchRepository {
    suspend fun getSearchCategories(): Result<List<SearchCategory>>
}

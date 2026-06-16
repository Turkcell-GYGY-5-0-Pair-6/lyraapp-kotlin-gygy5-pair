package com.turkcell.lyraapp.data.search

/**
 * Search ekranında sunulan kategori kartları.
 */

data class SearchCategory(
    val id: String,
    val title: String,
    val startColor: Long,
    val endColor: Long,
)

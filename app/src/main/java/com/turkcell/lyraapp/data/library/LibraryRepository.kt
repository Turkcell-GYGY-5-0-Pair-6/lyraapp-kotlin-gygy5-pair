package com.turkcell.lyraapp.data.library

interface LibraryRepository {
    suspend fun getLibraryPlaylists(): Result<List<LibraryPlaylist>>
}

data class LibraryPlaylist(
    val id: String,
    val title: String,
    val type: String,
    val songCount: Int,
    val isPinned: Boolean,
    val artworkStartColor: Long,
    val artworkEndColor: Long
)

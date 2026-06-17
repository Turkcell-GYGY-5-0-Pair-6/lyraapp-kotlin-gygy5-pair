package com.turkcell.lyraapp.data.library

interface LibraryRepository {
    suspend fun getLibraryPlaylists(): Result<List<LibraryPlaylist>>
    suspend fun getAvailableSongs(): Result<List<LibrarySong>>
    suspend fun createPlaylist(
        title: String,
        description: String,
        isPublic: Boolean,
        songIds: List<String>
    ): Result<Unit>
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

data class LibrarySong(
    val id: String,
    val title: String,
    val artist: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long
)


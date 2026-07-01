package com.turkcell.lyraapp.data.playlist

import com.turkcell.lyraapp.data.songs.SongDto
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface PlaylistApi {

    @GET("api/v1/me/playlists")
    suspend fun getMyPlaylists(
        @Header("Authorization") authorization: String
    ): MyPlaylistsResponseDto

    @POST("api/v1/me/playlists")
    suspend fun createPlaylist(
        @Header("Authorization") authorization: String,
        @Body request: CreatePlaylistRequestDto
    ): CreatePlaylistResponseDto

    @DELETE("api/v1/me/playlists/{id}")
    suspend fun deletePlaylist(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): DeletePlaylistResponseDto

    @GET("api/v1/playlists/{id}")
    suspend fun getPlaylistDetail(
        @Path("id") id: String
    ): PlaylistDetailResponseDto

    @POST("api/v1/me/playlists/{id}/tracks")
    suspend fun addTrackToPlaylist(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
        @Body request: AddTrackRequestDto
    ): AddTrackResponseDto

    @DELETE("api/v1/me/playlists/{id}/tracks/{songId}")
    suspend fun removeTrackFromPlaylist(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
        @Path("songId") songId: String
    ): RemoveTrackResponseDto
}

@Serializable
data class PlaylistDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val createdAt: String? = null,
    val ownerId: String? = null
)

@Serializable
data class MyPlaylistsResponseDto(
    val data: List<PlaylistDto>
)

@Serializable
data class CreatePlaylistRequestDto(
    val name: String,
    val description: String? = null
)

@Serializable
data class CreatePlaylistResponseDto(
    val data: PlaylistDto
)

@Serializable
data class DeletePlaylistResponseDto(
    val data: DeletePlaylistDataDto
)

@Serializable
data class DeletePlaylistDataDto(
    val deleted: Boolean
)

@Serializable
data class PlaylistWithSongsDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val createdAt: String? = null,
    val ownerId: String? = null,
    val songs: List<SongDto> = emptyList()
)

@Serializable
data class PlaylistDetailResponseDto(
    val data: PlaylistWithSongsDto
)

@Serializable
data class AddTrackRequestDto(
    val songId: String
)

@Serializable
data class AddTrackResponseDto(
    val data: AddTrackDataDto
)

@Serializable
data class AddTrackDataDto(
    val added: Boolean
)

@Serializable
data class RemoveTrackResponseDto(
    val data: RemoveTrackDataDto
)

@Serializable
data class RemoveTrackDataDto(
    val removed: Boolean
)

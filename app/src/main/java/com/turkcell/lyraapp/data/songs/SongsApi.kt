package com.turkcell.lyraapp.data.songs

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.POST
import retrofit2.http.Body

import retrofit2.http.Header
import kotlinx.serialization.Serializable

/**
 * Streaming API'nin şarkı uç noktaları için Retrofit arayüzü.
 *
 * Base URL [com.turkcell.lyraapp.data.network.NetworkModule] tarafından sağlanır; buradaki
 * yollar ona görelidir.
 */
interface SongsApi {

    /**
     * Şarkı kataloğunun bir sayfasını döndürür (cursor pagination + arama).
     *
     * @param limit Sayfa boyutu (1..100, varsayılan API tarafında 20).
     * @param cursor Önceki yanıttan gelen `nextCursor`; ilk sayfa için `null`.
     * @param query Başlık/sanatçı/albüm üzerinde büyük-küçük harf duyarsız arama.
     */
    @GET("api/v1/songs")
    suspend fun getSongs(
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null,
        @Query("q") query: String? = null,
    ): SongsPageDto


    /**
     * Bir şarkı için kısa ömürlü, imzalı stream URL'si üretir (TTL ~300sn).
     *
     * Dönen [StreamUrlEnvelope.data] içindeki `url` doğrudan ExoPlayer'a verilir; Range
     * isteklerini desteklediğinden seek (ilerletme/geri alma) çalışır. URL listeyle birlikte
     * önbelleğe alınmamalı, oynatmadan hemen önce alınmalıdır (bkz. openapi.json /stream-url).
     */
    @GET("api/v1/songs/{id}/stream-url")
    suspend fun getStreamUrl(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): StreamUrlEnvelope

    /**
     * Bir sonraki oynatılacak öğeyi (şarkı veya reklam + şarkı) döner.
     */
    @POST("api/v1/me/playback/next")
    suspend fun getPlaybackNext(
        @Header("Authorization") authorization: String,
        @Body request: PlaybackNextRequest
    ): PlaybackResponseDto

    /**
     * Reklamın izlendiğini sunucuya bildirir.
     */
    @POST("api/v1/me/playback/ad-complete")
    suspend fun adComplete(
        @Header("Authorization") authorization: String,
        @Body request: AdCompleteRequest
    ): AdCompleteResponse

    /**
     * Kullanıcıya özel önerilen şarkıları listeler.
     */
    @GET("api/v1/me/recommendations")
    suspend fun getRecommendations(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int? = null,
    ): RecommendationsResponseDto

    /**
     * Kullanıcının son dinlediği şarkıları listeler.
     */
    @GET("api/v1/me/recently-played")
    suspend fun getRecentlyPlayed(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int? = null,
    ): RecentlyPlayedResponseDto

    /**
     * Kullanıcının şarkı dinlediğini sisteme kaydeder.
     */
    @POST("api/v1/me/plays")
    suspend fun recordPlay(
        @Header("Authorization") authorization: String,
        @Body request: RecordPlayRequest
    ): RecordPlayResponse

    /**
     * Kullanıcıya özel "Senin İçin Müzikler" karma listesini döner.
     */
    @GET("api/v1/me/for-you")
    suspend fun getForYou(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int? = null,
    ): ForYouResponseDto
}

@Serializable
data class RecommendationsResponseDto(
    val data: List<SongDto> = emptyList(),
)

@Serializable
data class RecentlyPlayedResponseDto(
    val data: List<SongDto> = emptyList(),
)

@Serializable
data class ForYouResponseDto(
    val data: List<SongDto> = emptyList(),
)

@Serializable
data class RecordPlayRequest(
    val songId: String,
)

@Serializable
data class RecordPlayResponse(
    val data: RecordPlayResponseData,
)

@Serializable
data class RecordPlayResponseData(
    val recorded: Boolean,
)

@Serializable
data class PlaybackNextRequest(
    val songId: String
)

@Serializable
data class PlaybackResponseDto(
    val data: PlaybackDataDto
)

@Serializable
data class PlaybackDataDto(
    val type: String, // "song" | "ad"
    val song: SongDto? = null,
    val stream: StreamLinkDto? = null,
    val ad: AdDto? = null,
    val adStream: StreamLinkDto? = null,
    val impressionId: String? = null
)

@Serializable
data class StreamLinkDto(
    val url: String,
    val expiresAt: String,
    val mimeType: String
)

@Serializable
data class AdDto(
    val id: String,
    val title: String,
    val advertiser: String,
    val durationMs: Int,
    val mimeType: String
)

@Serializable
data class AdCompleteRequest(
    val impressionId: String
)

@Serializable
data class AdCompleteResponse(
    val data: AdCompleteResponseData
)

@Serializable
data class AdCompleteResponseData(
    val completed: Boolean
)
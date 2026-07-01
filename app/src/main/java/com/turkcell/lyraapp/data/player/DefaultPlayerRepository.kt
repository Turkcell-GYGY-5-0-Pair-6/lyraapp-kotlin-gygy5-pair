package com.turkcell.lyraapp.data.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.turkcell.lyraapp.data.songs.SongDto
import com.turkcell.lyraapp.data.songs.SongsApi
import com.turkcell.lyraapp.data.songs.RecordPlayRequest
import com.turkcell.lyraapp.data.songs.PlaybackNextRequest
import com.turkcell.lyraapp.data.songs.AdCompleteRequest
import com.turkcell.lyraapp.data.auth.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.roundToInt
import okhttp3.OkHttpClient
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import java.io.File
import java.io.IOException

/**
 * [PlayerRepository] arayüzünün ExoPlayer tabanlı gerçek veri ve oynatma yönetimi uygulamasıdır.
 * Bu sınıf uygulama genelinde tekil (Singleton) olarak hizmet verir.
 */
@Singleton
class DefaultPlayerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songsApi: SongsApi,
    private val authRepository: AuthRepository,
    private val okHttpClient: OkHttpClient,
    private val json: Json,
) : PlayerRepository {

    // Arka plan işleri ve asenkron operasyonlar için ana iş parçacığında (Main) çalışan coroutine kapsamı.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Şarkı çalarken geçen süreyi periyodik olarak sorgulayıp arayüze bildiren görevin referansı.
    private var progressJob: Job? = null

    // ExoPlayer nesnesini ilk ihtiyaç duyulduğunda (lazy) yapılandırıp oluşturur ve olay dinleyicisini ekler.
    override val player: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            addListener(listener)
        }
    }

    // Oynatıcının güncel durumunu sınıf içinde tutmak ve güncellemek için kullanılan MutableStateFlow.
    private val _playbackStateFlow = MutableStateFlow<PlaybackState?>(null)
    
    // UI katmanının oynatıcı durumunu güvenli şekilde (salt okunur) dinlemesi için sunulan akış.
    override val playbackStateFlow: Flow<PlaybackState?> = _playbackStateFlow.asStateFlow()

    // Sunucudan çekilen şarkı listesini hafızada tutarak gereksiz ağ isteklerini önleyen önbellek.
    private var cachedSongs: List<SongDto> = emptyList()
    
    // O an çalınmakta olan veya yüklenen şarkının benzersiz kimliği.
    private var currentSongId: String? = null
    
    // Aktif reklam gösterimi kimliği.
    private var pendingAdImpressionId: String? = null
    
    // Şarkının beğenilme (favori) durumunu tutan değişken.
    private var isLikedState = false
    
    // Şarkıların karışık sırada çalınıp çalınmayacağını tutan değişken.
    private var isShuffleState = false
    
    // Şarkının bittiğinde tekrar edip etmeyeceğini tutan değişken.
    private var isRepeatState = false

    // İndirme durumlarını tutan StateFlow
    private val _downloadStates = MutableStateFlow<Map<String, SongDownloadState>>(emptyMap())

    init {
        // Uygulama başladığında yerel indirmeleri kontrol edip durum haritasını doldurur.
        scope.launch(Dispatchers.IO) {
            val downloadsDir = File(context.filesDir, "downloads")
            if (downloadsDir.exists()) {
                val downloadedIds = downloadsDir.listFiles { file ->
                    file.isFile && file.name.endsWith(".mp3")
                }?.map { it.nameWithoutExtension } ?: emptyList()

                val statesMap = downloadedIds.associateWith { SongDownloadState.DOWNLOADED }
                _downloadStates.value = statesMap
            }
        }
    }

    // ExoPlayer oynatma olaylarını ve durum değişikliklerini dinleyen olay yöneticisi.
    private val listener = object : Player.Listener {
        
        // Çalma durumu (oynatma/duraklatma) değiştiğinde tetiklenir.
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePlaybackState()
            if (isPlaying) {
                // Şarkı oynatılıyorsa ilerleme çubuğunu güncelleyen görevi ve arka plan servisini başlat.
                startProgressPolling()
                startPlaybackService()
            } else {
                // Şarkı duraklatıldıysa periyodik sorgulamayı durdur.
                stopProgressPolling()
            }
        }

        // Oynatıcının iç durumu (tamamlandı, hazırlanıyor, boşta vb.) değiştiğinde tetiklenir.
        override fun onPlaybackStateChanged(state: Int) {
            updatePlaybackState()
            // Şarkının çalınması tamamlandığında
            if (state == Player.STATE_ENDED) {
                // Eğer tek şarkı tekrar modu aktif değilse otomatik olarak bir sonraki şarkıya geç.
                if (player.repeatMode != Player.REPEAT_MODE_ONE) {
                    scope.launch { skipToNext() }
                }
            }
        }

        // Oynatma pozisyonunda beklenmedik bir kesinti veya manuel atlama olduğunda tetiklenir.
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            updatePlaybackState()
        }

        // Medya öğesi geçişlerinde reklam izleme tamamlandığını bildirmek için tetiklenir.
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updatePlaybackState()
            val impressionId = pendingAdImpressionId
            if (impressionId != null && (mediaItem == null || !mediaItem.mediaId.startsWith("ad_"))) {
                pendingAdImpressionId = null
                scope.launch(Dispatchers.IO) {
                    try {
                        val token = authRepository.getAccessToken()
                        if (token != null) {
                            songsApi.adComplete(
                                authorization = "Bearer $token",
                                request = AdCompleteRequest(impressionId)
                            )
                        }
                    } catch (e: Exception) {
                        // Hataları sessizce yut
                    }
                }
            }
        }
    }

    // Şarkı listesini getiren yardımcı metot. Önbellek boşsa sunucuya istek atar.
    private suspend fun getSongList(): List<SongDto> {
        if (cachedSongs.isNotEmpty()) return cachedSongs
        return try {
            val response = songsApi.getSongs(limit = 100)
            cachedSongs = response.data
            response.data
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Her 500 milisaniyede bir çalışarak şarkının anlık ilerleme süresini arayüze bildiren periyodik görevi başlatır.
    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                updatePlaybackState()
                delay(500L)
            }
        }
    }

    // Zaman ilerlemesini takip eden periyodik coroutine görevini sonlandırır.
    private fun stopProgressPolling() {
        progressJob?.cancel()
        progressJob = null
    }

    // ExoPlayer üzerindeki güncel durum bilgilerini alıp PlaybackState modeli ile akış üzerinden yayınlar.
    private fun updatePlaybackState() {
        val songId = currentSongId ?: return
        val song = cachedSongs.find { it.id == songId }

        val isPlaying = player.isPlaying
        val durationMs = player.duration.coerceAtLeast(0L)
        val progressMs = player.currentPosition.coerceAtLeast(0L)

        val currentMediaItem = player.currentMediaItem
        val isAd = currentMediaItem?.mediaId?.startsWith("ad_") == true
        val title: String
        val artist: String
        val albumName: String
        val startColor: Long
        val endColor: Long

        if (isAd) {
            val mediaItem = currentMediaItem!!
            title = mediaItem.mediaMetadata.title?.toString() ?: "Reklam"
            artist = mediaItem.mediaMetadata.artist?.toString() ?: "Sponsorlu"
            albumName = mediaItem.mediaMetadata.albumTitle?.toString() ?: "Reklam"
            val colors = artworkColorsFor(mediaItem.mediaId)
            startColor = colors.first
            endColor = colors.second
        } else {
            val metadataFile = File(context.filesDir, "downloads/$songId.json")
            if (metadataFile.exists()) {
                var loadedFromOffline = false
                var offlineTitle = ""
                var offlineArtist = ""
                var offlineAlbum = ""
                var offlineStartColor = 0L
                var offlineEndColor = 0L

                try {
                    val metadataJson = metadataFile.readText()
                    var metadata = json.decodeFromString<OfflineMetadata>(metadataJson)
                    
                    // Eğer yerel metaveride süre 0 ise ve ExoPlayer gerçek süreyi öğrenmişse JSON'ı güncelle.
                    if (metadata.durationMs == 0L && durationMs > 0L) {
                        metadata = metadata.copy(
                            durationMs = durationMs,
                            durationText = formatTime(durationMs)
                        )
                        scope.launch(Dispatchers.IO) {
                            try {
                                metadataFile.writeText(json.encodeToString(OfflineMetadata.serializer(), metadata))
                            } catch (e: Exception) {
                                // Sessizce yut
                            }
                        }
                    }
                    offlineTitle = metadata.title
                    offlineArtist = metadata.artist
                    offlineAlbum = metadata.albumName
                    offlineStartColor = metadata.artworkStartColor
                    offlineEndColor = metadata.artworkEndColor
                    loadedFromOffline = true
                } catch (e: Exception) {
                    loadedFromOffline = false
                }

                if (loadedFromOffline) {
                    title = offlineTitle
                    artist = offlineArtist
                    albumName = offlineAlbum
                    startColor = offlineStartColor
                    endColor = offlineEndColor
                } else if (song != null) {
                    title = song.title
                    artist = song.artist
                    albumName = song.album ?: "Lyra Album"
                    val colors = artworkColorsFor(songId)
                    startColor = colors.first
                    endColor = colors.second
                } else {
                    title = "Bilinmeyen Şarkı"
                    artist = "Bilinmeyen Sanatçı"
                    albumName = "Lyra Album"
                    val colors = artworkColorsFor(songId)
                    startColor = colors.first
                    endColor = colors.second
                }
            } else if (song != null) {
                title = song.title
                artist = song.artist
                albumName = song.album ?: "Lyra Album"
                val colors = artworkColorsFor(songId)
                startColor = colors.first
                endColor = colors.second
            } else {
                return
            }
        }

        val displayDurationMs = if (durationMs > 0L) durationMs else getOfflineDurationMs(songId)

        val newState = PlaybackState(
            songId = songId,
            title = title,
            artist = artist,
            albumName = albumName,
            durationText = formatTime(displayDurationMs),
            durationMs = displayDurationMs,
            currentProgressText = formatTime(progressMs),
            currentProgressMs = progressMs,
            isPlaying = isPlaying,
            isLiked = isLikedState,
            isShuffleEnabled = isShuffleState,
            isRepeatEnabled = isRepeatState,
            artworkStartColor = startColor,
            artworkEndColor = endColor
        )
        _playbackStateFlow.value = newState
    }

    private fun getOfflineDurationMs(songId: String): Long {
        return try {
            val metadataFile = File(context.filesDir, "downloads/$songId.json")
            if (metadataFile.exists()) {
                val metadata = json.decodeFromString<OfflineMetadata>(metadataFile.readText())
                metadata.durationMs
            } else 0L
        } catch (e: Exception) {
            0L
        }
    }

    // Şarkının sunucudan akış (stream) URL'sini çeker.
    override suspend fun getStreamUrl(songId: String): Result<String> = runCatching {
        val token = authRepository.getAccessToken() ?: throw IllegalStateException("Token yok")
        songsApi.getStreamUrl("Bearer $token", songId).data.url
    }

    // Aktif şarkı kimliğini ayarlar ve asenkron olarak oynatma durumunu günceller.
    override fun setCurrentSongId(songId: String) {
        currentSongId = songId
        scope.launch {
            getSongList()
            updatePlaybackState()
        }
    }

    // Belirtilen şarkıyı hazırlar, ExoPlayer'a yükler ve oynatmayı başlatır.
    override suspend fun getPlaybackState(songId: String): Result<PlaybackState> = withContext(Dispatchers.Main) {
        runCatching {
            // Eğer şarkı yerel olarak indirilmişse doğrudan yerel dosyadan oynat.
            val mediaFile = File(context.filesDir, "downloads/$songId.mp3")
            val metadataFile = File(context.filesDir, "downloads/$songId.json")
            if (mediaFile.exists() && metadataFile.exists()) {
                val metadata = json.decodeFromString<OfflineMetadata>(metadataFile.readText())
                if (currentSongId != songId) {
                    currentSongId = songId

                    val songMetadata = MediaMetadata.Builder()
                        .setTitle(metadata.title)
                        .setArtist(metadata.artist)
                        .setAlbumTitle(metadata.albumName)
                        .build()

                    val songMediaItem = MediaItem.Builder()
                        .setMediaId(songId)
                        .setUri(Uri.fromFile(mediaFile))
                        .setMediaMetadata(songMetadata)
                        .build()

                    pendingAdImpressionId = null
                    player.setMediaItem(songMediaItem)
                    player.prepare()
                }
                player.play()
                updatePlaybackState()
                return@runCatching _playbackStateFlow.value!!
            }

            // Şarkı önbellekte aranır, yoksa listeyi yeniden çeker.
            var song = cachedSongs.find { it.id == songId }
            if (song == null) {
                try {
                    getSongList()
                    song = cachedSongs.find { it.id == songId }
                } catch (e: Exception) {
                    // Sessizce yut
                }
            }

            // Şarkının playback bilgilerini (veya reklamı) sunucudan talep eder.
            val token = authRepository.getAccessToken() ?: throw IllegalStateException("Oturum açılmadı. Lütfen giriş yapın.")
            val response = songsApi.getPlaybackNext("Bearer $token", PlaybackNextRequest(songId))
            val playbackData = response.data

            // Eğer oynatılmak istenen şarkı şu an yüklü olandan farklıysa ExoPlayer'a yükler.
            if (currentSongId != songId) {
                currentSongId = songId

                if (playbackData.type == "ad" && playbackData.ad != null && playbackData.adStream != null && playbackData.impressionId != null) {
                    // Reklam meta verilerini hazırlar.
                    val adMetadata = MediaMetadata.Builder()
                        .setTitle(playbackData.ad.title)
                        .setArtist(playbackData.ad.advertiser)
                        .setAlbumTitle("Reklam")
                        .build()

                    val adMediaItem = MediaItem.Builder()
                        .setMediaId("ad_${playbackData.impressionId}")
                        .setUri(Uri.parse(playbackData.adStream.url))
                        .setMediaMetadata(adMetadata)
                        .build()

                    // Şarkı meta verilerini hazırlar.
                    val songMetadata = MediaMetadata.Builder()
                        .setTitle(song?.title ?: playbackData.song?.title ?: "Bilinmeyen Şarkı")
                        .setArtist(song?.artist ?: playbackData.song?.artist ?: "Bilinmeyen Sanatçı")
                        .setAlbumTitle(song?.album ?: playbackData.song?.album ?: "Lyra Album")
                        .build()

                    val songMediaItem = MediaItem.Builder()
                        .setMediaId(songId)
                        .setUri(Uri.parse(playbackData.stream?.url ?: ""))
                        .setMediaMetadata(songMetadata)
                        .build()

                    pendingAdImpressionId = playbackData.impressionId
                    player.setMediaItems(listOf(adMediaItem, songMediaItem))
                } else {
                    // Şarkı meta verilerini hazırlar.
                    val songMetadata = MediaMetadata.Builder()
                        .setTitle(song?.title ?: playbackData.song?.title ?: "Bilinmeyen Şarkı")
                        .setArtist(song?.artist ?: playbackData.song?.artist ?: "Bilinmeyen Sanatçı")
                        .setAlbumTitle(song?.album ?: playbackData.song?.album ?: "Lyra Album")
                        .build()

                    val songMediaItem = MediaItem.Builder()
                        .setMediaId(songId)
                        .setUri(Uri.parse(playbackData.stream?.url ?: ""))
                        .setMediaMetadata(songMetadata)
                        .build()

                    pendingAdImpressionId = null
                    player.setMediaItem(songMediaItem)
                }

                player.prepare()
            }
            player.play()

            updatePlaybackState()
            _playbackStateFlow.value!!
        }
    }

    // Arka planda oynatmanın kesintisiz devam edebilmesi için PlaybackService servisini başlatır.
    private fun startPlaybackService() {
        try {
            val intent = Intent(context, PlaybackService::class.java)
            context.startService(intent)
        } catch (e: Exception) {
            // Arka planda servis başlatma hatalarını güvenle yut.
        }
    }

    // Oynatmayı duraklatır veya duraklatılmışsa devam ettirir.
    override suspend fun togglePlayPause(): Result<Unit> = withContext(Dispatchers.Main) {
        runCatching {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
            updatePlaybackState()
        }
    }

    // Şarkının beğenilme (beğendim/beğenmedim) durumunu değiştirir.
    override suspend fun toggleLike(): Result<Unit> = runCatching {
        isLikedState = !isLikedState
        withContext(Dispatchers.Main) {
            updatePlaybackState()
        }
    }

    // Karışık çalma modunu açar veya kapatır.
    override suspend fun toggleShuffle(): Result<Unit> = runCatching {
        isShuffleState = !isShuffleState
        withContext(Dispatchers.Main) {
            updatePlaybackState()
        }
    }

    // Tekrar oynatma modunu değiştirir (tek şarkı tekrarı aktif/pasif).
    override suspend fun toggleRepeat(): Result<Unit> = withContext(Dispatchers.Main) {
        runCatching {
            isRepeatState = !isRepeatState
            // ExoPlayer'a ilgili tekrar modunu set eder.
            player.repeatMode = if (isRepeatState) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
            updatePlaybackState()
        }
    }

    // Şarkıyı belirtilen milisaniyeye (ilerleme pozisyonuna) sarar.
    override suspend fun seekTo(progressMs: Long): Result<Unit> = withContext(Dispatchers.Main) {
        runCatching {
            player.seekTo(progressMs)
            updatePlaybackState()
        }
    }

    // Listede sıradaki bir sonraki şarkıyı bulur ve oynatır.
    override suspend fun skipToNext(): Result<Unit> = withContext(Dispatchers.Main) {
        runCatching {
            val songList = getSongList()
            val currentIndex = songList.indexOfFirst { it.id == currentSongId }
            if (currentIndex != -1 && songList.isNotEmpty()) {
                val nextIndex = (currentIndex + 1) % songList.size
                val nextSong = songList[nextIndex]
                getPlaybackState(nextSong.id)
            }
            Unit
        }
    }

    // Listede bir önceki şarkıyı bulur ve oynatır.
    override suspend fun skipToPrevious(): Result<Unit> = withContext(Dispatchers.Main) {
        runCatching {
            val songList = getSongList()
            val currentIndex = songList.indexOfFirst { it.id == currentSongId }
            if (currentIndex != -1 && songList.isNotEmpty()) {
                val prevIndex = if (currentIndex - 1 < 0) songList.size - 1 else currentIndex - 1
                val prevSong = songList[prevIndex]
                getPlaybackState(prevSong.id)
            }
            Unit
        }
    }

    override suspend fun stop(): Result<Unit> = withContext(Dispatchers.Main) {
        runCatching {
            if (player.isPlaying) {
                player.pause()
            }
            player.stop()
            player.clearMediaItems()
            currentSongId = null
            _playbackStateFlow.value = null
            stopProgressPolling()
            stopPlaybackService()
        }
    }

    private fun stopPlaybackService() {
        try {
            val intent = Intent(context, PlaybackService::class.java)
            context.stopService(intent)
        } catch (e: Exception) {
            // Arka planda servis durdurma hatalarını güvenle yut.
        }
    }

    override fun isSongDownloaded(songId: String): Boolean {
        return _downloadStates.value[songId] == SongDownloadState.DOWNLOADED
    }

    override fun getSongDownloadState(songId: String): Flow<SongDownloadState> {
        return _downloadStates.map { states ->
            states[songId] ?: SongDownloadState.NOT_DOWNLOADED
        }
    }

    override suspend fun downloadSong(songId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            _downloadStates.update { it + (songId to SongDownloadState.DOWNLOADING) }

            val streamUrl = getStreamUrl(songId).getOrThrow()

            val request = okhttp3.Request.Builder().url(streamUrl).build()
            val downloadsDir = File(context.filesDir, "downloads").apply { mkdirs() }
            val tempFile = File(downloadsDir, "$songId.tmp")
            val destFile = File(downloadsDir, "$songId.mp3")

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Download failed: $response")
                val body = response.body ?: throw IOException("Empty body")

                body.byteStream().use { inputStream ->
                    tempFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                if (!tempFile.renameTo(destFile)) {
                    throw IOException("Could not rename temp file")
                }
            }

            var song = cachedSongs.find { it.id == songId }
            if (song == null) {
                getSongList()
                song = cachedSongs.find { it.id == songId }
            }
            val title = song?.title ?: _playbackStateFlow.value?.title ?: "Bilinmeyen Şarkı"
            val artist = song?.artist ?: _playbackStateFlow.value?.artist ?: "Bilinmeyen Sanatçı"
            val albumName = song?.album ?: _playbackStateFlow.value?.albumName ?: "Lyra Album"

            val durationMs = if (songId == _playbackStateFlow.value?.songId) {
                _playbackStateFlow.value?.durationMs ?: 0L
            } else {
                0L
            }
            val colors = artworkColorsFor(songId)

            val metadata = OfflineMetadata(
                songId = songId,
                title = title,
                artist = artist,
                albumName = albumName,
                durationMs = durationMs,
                durationText = formatTime(durationMs),
                artworkStartColor = colors.first,
                artworkEndColor = colors.second
            )

            val metadataFile = File(downloadsDir, "$songId.json")
            val metadataJson = json.encodeToString(OfflineMetadata.serializer(), metadata)
            metadataFile.writeText(metadataJson)

            _downloadStates.update { it + (songId to SongDownloadState.DOWNLOADED) }
            showDownloadCompleteNotification(title, songId)
        }.onFailure { error ->
            _downloadStates.update { it + (songId to SongDownloadState.NOT_DOWNLOADED) }
        }
    }

    override suspend fun deleteDownloadedSong(songId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val downloadsDir = File(context.filesDir, "downloads")
            val mediaFile = File(downloadsDir, "$songId.mp3")
            val metadataFile = File(downloadsDir, "$songId.json")

            if (mediaFile.exists()) mediaFile.delete()
            if (metadataFile.exists()) metadataFile.delete()

            _downloadStates.update { it - songId }
        }
    }

    private fun showDownloadCompleteNotification(songTitle: String, songId: String) {
        val channelId = "song_downloads"
        val notificationId = songId.hashCode()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Şarkı İndirmeleri"
            val descriptionText = "Şarkı indirme tamamlandığında gösterilen bildirimler"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.turkcell.lyraapp.R.drawable.ic_download_done)
            .setContentTitle("İndirme tamamlandı")
            .setContentText("\"$songTitle\" çevrimdışı kullanıma hazır")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    notificationManager.notify(notificationId, builder.build())
                }
            } else {
                notificationManager.notify(notificationId, builder.build())
            }
        } catch (e: SecurityException) {
            // Güvenle yut.
        }
    }

    companion object {
        
        // Milisaniye cinsinden süreyi "dakika:saniye" (Örn: 3:45) biçiminde formatlar.
        fun formatTime(ms: Long): String {
            if (ms <= 0) return "0:00"
            val totalSeconds = ms / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }

        // Şarkının benzersiz kimliğini kullanarak HSL formatında uyumlu gradyan renk kodları üretir.
        fun artworkColorsFor(id: String): Pair<Long, Long> {
            val hue = (abs(id.hashCode()) % 360).toFloat()
            val start = hslToArgb(hue, saturation = 0.50f, lightness = 0.55f)
            val end = hslToArgb(hue, saturation = 0.55f, lightness = 0.32f)
            return start to end
        }

        // HSL (Renk Özü, Doygunluk, Parlaklık) değerlerini ARGB formatında 32 bitlik renk koduna çevirir.
        fun hslToArgb(hue: Float, saturation: Float, lightness: Float): Long {
            val c = (1f - abs(2f * lightness - 1f)) * saturation
            val hPrime = hue / 60f
            val x = c * (1f - abs(hPrime % 2f - 1f))
            val (r1, g1, b1) = when {
                hPrime < 1f -> Triple(c, x, 0f)
                hPrime < 2f -> Triple(x, c, 0f)
                hPrime < 3f -> Triple(0f, c, x)
                hPrime < 4f -> Triple(0f, x, c)
                hPrime < 5f -> Triple(x, 0f, c)
                else -> Triple(c, 0f, x)
            }
            val m = lightness - c / 2f
            val r = ((r1 + m) * 255f).roundToInt().coerceIn(0, 255).toLong()
            val g = ((g1 + m) * 255f).roundToInt().coerceIn(0, 255).toLong()
            val b = ((b1 + m) * 255f).roundToInt().coerceIn(0, 255).toLong()
            return (0xFFL shl 24) or (r shl 16) or (g shl 8) or b
    }
    }
}

@Serializable
data class OfflineMetadata(
    val songId: String,
    val title: String,
    val artist: String,
    val albumName: String,
    val durationMs: Long,
    val durationText: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long
)
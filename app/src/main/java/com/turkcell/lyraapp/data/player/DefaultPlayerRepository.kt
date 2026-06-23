package com.turkcell.lyraapp.data.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.turkcell.lyraapp.data.songs.SongDto
import com.turkcell.lyraapp.data.songs.SongsApi
import com.turkcell.lyraapp.data.songs.RecordPlayRequest
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * [PlayerRepository] arayüzünün ExoPlayer tabanlı gerçek veri ve oynatma yönetimi uygulamasıdır.
 * Bu sınıf uygulama genelinde tekil (Singleton) olarak hizmet verir.
 */
@Singleton
class DefaultPlayerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songsApi: SongsApi,
    private val authRepository: AuthRepository,
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
    
    // Şarkının beğenilme (favori) durumunu tutan değişken.
    private var isLikedState = false
    
    // Şarkıların karışık sırada çalınıp çalınmayacağını tutan değişken.
    private var isShuffleState = false
    
    // Şarkının bittiğinde tekrar edip etmeyeceğini tutan değişken.
    private var isRepeatState = false

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
        val song = cachedSongs.find { it.id == songId } ?: return

        val isPlaying = player.isPlaying
        val durationMs = player.duration.coerceAtLeast(0L)
        val progressMs = player.currentPosition.coerceAtLeast(0L)
        val (startColor, endColor) = artworkColorsFor(songId)

        val newState = PlaybackState(
            songId = songId,
            title = song.title,
            artist = song.artist,
            albumName = song.album ?: "Lyra Album",
            durationText = formatTime(durationMs),
            durationMs = durationMs,
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

    // Şarkının sunucudan akış (stream) URL'sini çeker.
    override suspend fun getStreamUrl(songId: String): Result<String> = runCatching {
        songsApi.getStreamUrl(songId).data.url
    }

    // Aktif şarkı kimliğini ayarlar ve asenkron olarak oynatma durumunu günceller.
    override fun setCurrentSongId(songId: String) {
        currentSongId = songId
        scope.launch {
            getSongList()
            updatePlaybackState()
        }
    }

    // Belirtilen şarkıyı hazırlar, ExoPlayer'a yükler, oynatmayı başlatır ve backend tarafına dinlenme kaydı gönderir.
    override suspend fun getPlaybackState(songId: String): Result<PlaybackState> = withContext(Dispatchers.Main) {
        runCatching {
            // Şarkı önbellekte aranır, yoksa listeyi yeniden çeker.
            var song = cachedSongs.find { it.id == songId }
            if (song == null) {
                getSongList()
                song = cachedSongs.find { it.id == songId } ?: throw NoSuchElementException("Song not found")
            }

            // Şarkının imzalı akış bağlantısını sunucudan talep eder.
            val streamUrl = songsApi.getStreamUrl(songId).data.url

            // Eğer oynatılmak istenen şarkı şu an yüklü olandan farklıysa ExoPlayer'a yükler.
            if (currentSongId != songId) {
                currentSongId = songId
                
                // Medya meta verilerini hazırlar.
                val mediaMetadata = MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album ?: "Lyra Album")
                    .build()

                // Oynatılacak medya öğesini URL ve meta verileri ile oluşturur.
                val mediaItem = MediaItem.Builder()
                    .setUri(Uri.parse(streamUrl))
                    .setMediaMetadata(mediaMetadata)
                    .build()

                player.setMediaItem(mediaItem)
                player.prepare()
            }
            player.play()

            // Dinleme istatistiğini kaydetmek üzere asenkron olarak sunucuya istek gönderir.
            scope.launch(Dispatchers.IO) {
                try {
                    val token = authRepository.getAccessToken()
                    if (token != null) {
                        songsApi.recordPlay(
                            authorization = "Bearer $token",
                            request = RecordPlayRequest(songId)
                        )
                    }
                } catch (e: Exception) {
                    // Hataları sessizce yut
                }
            }

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
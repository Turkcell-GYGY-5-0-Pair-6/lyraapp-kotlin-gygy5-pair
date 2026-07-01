package com.turkcell.lyraapp.ui.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.player.PlaybackState
import com.turkcell.lyraapp.data.player.SongDownloadState
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

// Oynatıcı Ekranı İkon Tanımları (Local ImageVectors to keep code independent)
private val ExpandMoreIcon: ImageVector by lazy {
    ImageVector.Builder("ExpandMore", 24.dp, 24.dp, 24f, 24f)
        .addPath(PathParser().parsePathString("M16.59 8.59L12 13.17 7.41 8.59 6 10l6 6 6-6z").toNodes(), fill = SolidColor(Color.Black)).build()
}

private val SkipPreviousIcon: ImageVector by lazy {
    ImageVector.Builder("SkipPrevious", 24.dp, 24.dp, 24f, 24f)
        .addPath(PathParser().parsePathString("M 6 6 H 8 V 18 H 6 Z M 9.5 12 L 18 18 V 6 Z").toNodes(), fill = SolidColor(Color.Black)).build()
}

private val SkipNextIcon: ImageVector by lazy {
    ImageVector.Builder("SkipNext", 24.dp, 24.dp, 24f, 24f)
        .addPath(PathParser().parsePathString("M 6 18 L 14.5 12 L 6 6 Z M 16 6 H 18 V 18 H 16 Z").toNodes(), fill = SolidColor(Color.Black)).build()
}

private val RepeatIcon: ImageVector by lazy {
    ImageVector.Builder("Repeat", 24.dp, 24.dp, 24f, 24f)
        .addPath(PathParser().parsePathString("M7 7h10v3l4-4-4-4v3H5v6h2V7zm10 10H7v-3l-4 4 4 4v-3h12v-6h-2v4z").toNodes(), fill = SolidColor(Color.Black)).build()
}

private val DevicesIcon: ImageVector by lazy {
    ImageVector.Builder("Devices", 24.dp, 24.dp, 24f, 24f)
        .addPath(PathParser().parsePathString("M21 2H3c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h7v2H8v2h8v-2h-2v-2h7c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 12H3V4h18v10z").toNodes(), fill = SolidColor(Color.Black)).build()
}

private val QueueMusicIcon: ImageVector by lazy {
    ImageVector.Builder("QueueMusic", 24.dp, 24.dp, 24f, 24f)
        .addPath(PathParser().parsePathString("M15 6H3v2h12V6zm0 4H3v2h12v-2zM3 16h8v-2H3v2zm17-1.75V4h4V2h-6v8.75c-.5-.45-1.2-.75-2-.75-1.65 0-3 1.35-3 3s1.35 3 3 3 3-1.35 3-3z").toNodes(), fill = SolidColor(Color.Black)).build()
}

private val PauseIcon: ImageVector by lazy {
    ImageVector.Builder("Pause", 24.dp, 24.dp, 24f, 24f)
        .addPath(PathParser().parsePathString("M6 19h4V5H6v14zm8-14v14h4V5h-4z").toNodes(), fill = SolidColor(Color.Black)).build()
}

@Composable
fun NowPlayingRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                NowPlayingEffect.NavigateBack -> onNavigateBack()
                is NowPlayingEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    NowPlayingScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@Composable
fun NowPlayingScreen(
    state: NowPlayingUiState,
    onIntent: (NowPlayingIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (state.isLoading && state.playbackState == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (state.playbackState != null) {
            val playback = state.playbackState
            
            // Warm brown vertical gradient derived from the artwork color
            val backgroundGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(playback.artworkStartColor).copy(alpha = 0.35f),
                    Color.Black.copy(alpha = 0.95f)
                ),
                startY = 0f,
                endY = 1500f
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundGradient)
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Toolbar Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onIntent(NowPlayingIntent.BackClicked) }) {
                            Icon(
                                imageVector = ExpandMoreIcon,
                                contentDescription = "Kapat",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "ŞİMDİ ÇALIYOR",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = playback.albumName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        IconButton(onClick = { /* Menü */ }) {
                            Icon(
                                imageVector = LyraIcons.MoreVert,
                                contentDescription = "Seçenekler",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Album Artwork
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PlayerCoverArtwork(
                            startColor = Color(playback.artworkStartColor),
                            endColor = Color(playback.artworkEndColor),
                            modifier = Modifier
                                .size(320.dp)
                                .clip(RoundedCornerShape(32.dp))
                        )
                    }

                    // Metadata (Title & Artist & Heart)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playback.title,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = playback.artist,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onIntent(NowPlayingIntent.ToggleDownload) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                when (state.downloadState) {
                                    SongDownloadState.DOWNLOADING -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    SongDownloadState.DOWNLOADED -> {
                                        Icon(
                                            imageVector = LyraIcons.DownloadCircle,
                                            contentDescription = "İndirildi",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    SongDownloadState.NOT_DOWNLOADED -> {
                                        Icon(
                                            imageVector = LyraIcons.Download,
                                            contentDescription = "İndir",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            IconButton(
                                onClick = { onIntent(NowPlayingIntent.ToggleLike) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (playback.isLiked) LyraIcons.Favorite else LyraIcons.FavoriteOutlined,
                                    contentDescription = "Beğen",
                                    tint = if (playback.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Slider
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Slider(
                            value = playback.currentProgressMs.toFloat(),
                            onValueChange = { onIntent(NowPlayingIntent.SeekTo(it.toLong())) },
                            valueRange = 0f..playback.durationMs.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.24f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = playback.currentProgressText,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = playback.durationText,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Playback Controls (Shuffle, Prev, Play/Pause, Next, Repeat)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onIntent(NowPlayingIntent.ToggleShuffle) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = LyraIcons.Shuffle,
                                contentDescription = "Karıştır",
                                tint = if (playback.isShuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = { onIntent(NowPlayingIntent.SkipPrevious) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = SkipPreviousIcon,
                                contentDescription = "Önceki",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Big Play/Pause circular button
                        IconButton(
                            onClick = { onIntent(NowPlayingIntent.TogglePlayPause) },
                            modifier = Modifier
                                .size(72.dp)
                                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = if (playback.isPlaying) PauseIcon else LyraIcons.PlayArrow,
                                contentDescription = if (playback.isPlaying) "Duraklat" else "Oynat",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        IconButton(
                            onClick = { onIntent(NowPlayingIntent.SkipNext) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = SkipNextIcon,
                                contentDescription = "Sonraki",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(
                            onClick = { onIntent(NowPlayingIntent.ToggleRepeat) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = RepeatIcon,
                                contentDescription = "Tekrarla",
                                tint = if (playback.isRepeatEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // Bottom Extra Actions (Devices, Background Alert, Queue)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { /* Cihazlar */ }) {
                            Icon(
                                imageVector = DevicesIcon,
                                contentDescription = "Cihazlar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Background Alarm Timer option
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { /* Arkaplan Ayarları */ }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = LyraIcons.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Arkaplan",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { /* Sıra */ }) {
                            Icon(
                                imageVector = QueueMusicIcon,
                                contentDescription = "Çalma Sırası",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.error ?: "Bir hata oluştu.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun PlayerCoverArtwork(
    startColor: Color,
    endColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Brush.linearGradient(listOf(startColor, endColor)))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = center
            val baseRadius = size.width * 0.15f
            val strokeWidth = 1.5.dp.toPx()
            
            // Draw concentric rings on cover
            for (i in 1..4) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f * (5 - i)),
                    radius = baseRadius + (i * 32.dp.toPx()),
                    center = centerOffset,
                    style = Stroke(width = strokeWidth)
                )
            }
        }
    }
}

@Preview(name = "NowPlaying - Dark Theme", showBackground = true, showSystemUi = true)
@Composable
private fun NowPlayingScreenPreview() {
    LyraAppTheme(darkTheme = true) {
        NowPlayingScreen(
            state = NowPlayingUiState(
                playbackState = PlaybackState(
                    songId = "preview",
                    title = "Neon Sokaklar",
                    artist = "Şehir Işıkları",
                    albumName = "Gece Vardiyası",
                    durationText = "3:43",
                    durationMs = 223000L,
                    currentProgressText = "1:33",
                    currentProgressMs = 93000L,
                    isPlaying = true,
                    isLiked = true,
                    artworkStartColor = 0xFFD98E4A,
                    artworkEndColor = 0xFF8A5526
                ),
                downloadState = SongDownloadState.DOWNLOADED
            ),
            onIntent = {}
        )
    }
}

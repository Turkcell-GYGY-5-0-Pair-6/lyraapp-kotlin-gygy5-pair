package com.turkcell.lyraapp.ui.playlist

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.turkcell.lyraapp.data.playlist.PlaylistDetail
import com.turkcell.lyraapp.data.playlist.PlaylistSong
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

// Local Pause Icon to avoid importing from material icons
private val PauseIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Pause",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = PathParser().parsePathString("M6 19h4V5H6v14zm8-14v14h4V5h-4z").toNodes(),
        fill = SolidColor(Color.Black)
    ).build()
}

@Composable
fun PlaylistDetailRoute(
    onNavigateBack: () -> Unit,
    onNavigateToNowPlaying: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                PlaylistDetailEffect.NavigateBack -> onNavigateBack()
                is PlaylistDetailEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is PlaylistDetailEffect.NavigateToNowPlaying -> onNavigateToNowPlaying(effect.songId)
            }
        }
    }

    PlaylistDetailScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@Composable
fun PlaylistDetailScreen(
    state: PlaylistDetailUiState,
    onIntent: (PlaylistDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (state.isLoading && state.playlist == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (state.playlist != null) {
            val playlist = state.playlist
            
            // Background plum gradient matching the artwork color
            val backgroundGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(playlist.artworkStartColor).copy(alpha = 0.25f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = 1200f
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundGradient)
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Toolbar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 0.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onIntent(PlaylistDetailIntent.BackClicked) }) {
                            Icon(
                                imageVector = LyraIcons.ArrowBack,
                                contentDescription = "Geri",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { /* Ek Seçenekler */ }) {
                            Icon(
                                imageVector = LyraIcons.MoreVert,
                                contentDescription = "Seçenekler",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header info & Artwork
                        item {
                            PlaylistHeaderSection(
                                playlist = playlist,
                                onPlayClick = { onIntent(PlaylistDetailIntent.PlayPlaylist) },
                                onFavoriteClick = { onIntent(PlaylistDetailIntent.TogglePlaylistFavorite) },
                                onDownloadClick = { onIntent(PlaylistDetailIntent.TogglePlaylistDownload) },
                                onShuffleClick = { onIntent(PlaylistDetailIntent.ToggleShuffle) }
                            )
                        }

                        // Songs
                        items(playlist.songs, key = { it.id }) { song ->
                            PlaylistSongRowItem(
                                song = song,
                                onClick = { onIntent(PlaylistDetailIntent.SongClicked(song.id)) },
                                onLikeToggle = { onIntent(PlaylistDetailIntent.ToggleLikeSong(song.id)) }
                            )
                        }
                    }
                }
            }
        } else {
            // Error State
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
private fun PlaylistHeaderSection(
    playlist: PlaylistDetail,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onShuffleClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Artwork Canvas Box
        PlaylistCoverArtwork(
            startColor = Color(playlist.artworkStartColor),
            endColor = Color(playlist.artworkEndColor),
            modifier = Modifier
                .size(260.dp)
                .clip(RoundedCornerShape(28.dp))
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = playlist.title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                textAlign = TextAlign.Center
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Description
        Text(
            text = playlist.description,
            style = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Center
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Metadata
        Text(
            text = "${playlist.creator} · ${playlist.songCount} şarkı · ${playlist.durationText}",
            style = MaterialTheme.typography.bodySmall.copy(
                textAlign = TextAlign.Center
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Action Icons Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (playlist.isFavorite) LyraIcons.Favorite else LyraIcons.FavoriteOutlined,
                        contentDescription = "Beğen",
                        tint = if (playlist.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onDownloadClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = LyraIcons.Download,
                        contentDescription = "İndir",
                        tint = if (playlist.isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = { /* Ekle */ },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = LyraIcons.Add,
                        contentDescription = "Ekle",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onShuffleClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = LyraIcons.Shuffle,
                        contentDescription = "Karıştır",
                        tint = if (playlist.isShuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Big Play/Pause Button (Right)
            IconButton(
                onClick = onPlayClick,
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
            ) {
                Icon(
                    imageVector = if (playlist.isPlaying) PauseIcon else LyraIcons.PlayArrow,
                    contentDescription = if (playlist.isPlaying) "Duraklat" else "Oynat",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun PlaylistCoverArtwork(
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

@Composable
private fun PlaylistSongRowItem(
    song: PlaylistSong,
    onClick: () -> Unit,
    onLikeToggle: () -> Unit
) {
    val isPlaying = song.isPlaying
    val rowBgColor = if (isPlaying) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(rowBgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Artwork
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(song.artworkStartColor), Color(song.artworkEndColor))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isPlaying) {
                // Waveform overlay
                Icon(
                    imageVector = LyraIcons.Waveform,
                    contentDescription = "Çalıyor",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Title and Artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Duration
        Text(
            text = song.duration,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Like heart icon
        IconButton(
            onClick = onLikeToggle,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = if (song.isLiked) LyraIcons.Favorite else LyraIcons.FavoriteOutlined,
                contentDescription = "Beğen",
                tint = if (song.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // More options icon
        IconButton(
            onClick = { /* Seçenekler */ },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = LyraIcons.MoreVert,
                contentDescription = "Seçenekler",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(name = "PlaylistDetail - Dark Theme", showBackground = true, showSystemUi = true)
@Composable
private fun PlaylistDetailScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        PlaylistDetailScreen(
            state = PlaylistDetailUiState(
                playlist = PlaylistDetail(
                    id = "preview",
                    title = "Gece Sürüşü",
                    description = "Karanlık yollar için synth-pop",
                    creator = "Zeynep Kaya",
                    songCount = 6,
                    durationText = "23 dk",
                    artworkStartColor = 0xFF8B6FB8,
                    artworkEndColor = 0xFF4A3D6B,
                    isFavorite = true,
                    isDownloaded = true,
                    isShuffleEnabled = false,
                    isPlaying = true,
                    songs = listOf(
                        PlaylistSong("1", "Neon Sokaklar", "Şehir Işıkları", "3:43", isLiked = true, isPlaying = true, 0xFFD98E4A, 0xFF8A5526),
                        PlaylistSong("2", "Gece Yarısı", "Mavi Deniz", "3:34", isLiked = true, isPlaying = false, 0xFF6FBF5A, 0xFF356B2A),
                        PlaylistSong("3", "Mor Bulutlar", "Derin Kaya", "3:52", isLiked = false, isPlaying = false, 0xFF4AC2A8, 0xFF1F6E5C),
                        PlaylistSong("4", "Son Tren", "Peron", "3:37", isLiked = false, isPlaying = false, 0xFF4AC2A8, 0xFF1F6E5C),
                        PlaylistSong("5", "Yıldız Tozu", "Polaris", "4:07", isLiked = true, isPlaying = false, 0xFF3D5A80, 0xFF1B2A45),
                        PlaylistSong("6", "Sessiz Şehir", "Ela Tuna", "4:10", isLiked = false, isPlaying = false, 0xFF8B6FB8, 0xFF4A3D6B)
                    )
                )
            ),
            onIntent = {}
        )
    }
}

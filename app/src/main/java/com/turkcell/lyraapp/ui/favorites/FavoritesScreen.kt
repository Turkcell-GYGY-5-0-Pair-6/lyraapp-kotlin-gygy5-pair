package com.turkcell.lyraapp.ui.favorites

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.favorites.FavoriteSong
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Favoriler ekranının durumlu (stateful) giriş noktası.
 * ViewModel'den durumları toplar ve tek seferlik olayları tüketir.
 */
@Composable
fun FavoritesRoute(
    onNavigateBack: () -> Unit,
    onNavigateToNowPlaying: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FavoritesEffect.NavigateBack -> onNavigateBack()
                is FavoritesEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is FavoritesEffect.NavigateToNowPlaying -> onNavigateToNowPlaying(effect.songId)
            }
        }
    }

    FavoritesScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

/**
 * Favoriler ekranının durumsuz (stateless) çizimi.
 */
@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    onIntent: (FavoritesIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Üst Bar (Geri Tuşu)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onIntent(FavoritesIntent.BackClicked) }) {
                    Icon(
                        imageVector = LyraIcons.ArrowBack,
                        contentDescription = "Geri",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (state.isLoading && state.songs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Üst Başlık & Kapak Alanı
                    item {
                        FavoritesHeaderSection(
                            songCountText = state.totalSongsText,
                            durationText = state.totalDurationText,
                            isShuffleEnabled = state.isShuffleEnabled,
                            isDownloaded = state.isDownloaded,
                            onPlayClick = { onIntent(FavoritesIntent.PlayAll) },
                            onShuffleClick = { onIntent(FavoritesIntent.ToggleShuffle) },
                            onDownloadClick = { onIntent(FavoritesIntent.ToggleDownload) }
                        )
                    }

                    // Şarkı Listesi Elemanları
                    items(state.songs, key = { it.id }) { song ->
                        FavoriteSongRowItem(
                            song = song,
                            onClick = { onIntent(FavoritesIntent.SongClicked(song.id)) },
                            onLikeToggle = { onIntent(FavoritesIntent.ToggleLikeSong(song.id)) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Ekranın üst kapak görselini, çalma listesi bilgilerini ve buton grubunu barındıran bölüm.
 */
@Composable
private fun FavoritesHeaderSection(
    songCountText: String,
    durationText: String,
    isShuffleEnabled: Boolean,
    isDownloaded: Boolean,
    onPlayClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Kapak Resmi & Bilgiler
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Büyük Gradyan Beğenilen Şarkılar Kapağı
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFFD9E2), Color(0xFF8F4A5F))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LyraIcons.Favorite,
                    contentDescription = null,
                    tint = Color(0xFF5E1133), // Koyu kiraz/şarap rengi
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Metinler
            Column {
                Text(
                    text = "Beğenilen Şarkılar",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$songCountText · $durationText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Aksiyon Butonları (Çal, Karıştır, İndir)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Çal (Play) Butonu
            Button(
                onClick = onPlayClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = LyraIcons.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Çal",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Karıştır (Shuffle) Butonu
            IconButton(
                onClick = onShuffleClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isShuffleEnabled) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    )
            ) {
                Icon(
                    imageVector = LyraIcons.Shuffle,
                    contentDescription = "Karıştır",
                    tint = if (isShuffleEnabled) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // İndir (Download) Butonu
            IconButton(
                onClick = onDownloadClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDownloaded) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    )
            ) {
                Icon(
                    imageVector = LyraIcons.Download,
                    contentDescription = "İndir",
                    tint = if (isDownloaded) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

/**
 * Tek bir şarkı listesi öğesi.
 */
@Composable
private fun FavoriteSongRowItem(
    song: FavoriteSong,
    onClick: () -> Unit,
    onLikeToggle: () -> Unit
) {
    // Çalan şarkı satırı için hafif bir vurgulama arka planı uygulanır.
    val backgroundModifier = if (song.isPlaying) {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
    } else {
        Modifier.fillMaxWidth()
    }

    Row(
        modifier = backgroundModifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sol Albüm Kapağı
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
            if (song.isPlaying) {
                // Ekolayzer dalgası (Waveform) simgesi çalan şarkıda kapak üstüne biner
                Icon(
                    imageVector = LyraIcons.Waveform,
                    contentDescription = "Çalıyor",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Şarkı & Sanatçı Bilgisi
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (song.isPlaying) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
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

        // Süre Bilgisi
        Text(
            text = song.duration,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Kalp İkonu (Beğeni Durumu)
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

        // Seçenekler (Dikey üç nokta)
        IconButton(
            onClick = { /* Ek seçenekler menüsü */ },
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

@Preview(name = "Favorites - Dark Theme", showBackground = true, showSystemUi = true)
@Composable
private fun FavoritesScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        FavoritesScreen(
            state = FavoritesUiState(
                songs = listOf(
                    FavoriteSong("1", "Gece Yarısı", "Mavi Deniz", "3:34", true, false, 0xFF6FBF5A, 0xFF356B2A),
                    FavoriteSong("2", "Yıldız Tozu", "Polaris", "4:07", true, false, 0xFF3D5A80, 0xFF1B2A45),
                    FavoriteSong("3", "İlk Işık", "Sabah Ezgisi", "3:25", true, false, 0xFF5AAFC9, 0xFF2A5F73),
                    FavoriteSong("4", "Neon Sokaklar", "Şehir Işıkları", "3:43", true, true, 0xFFD98E4A, 0xFF8A5526),
                    FavoriteSong("5", "Derin Mavi", "Okyanus", "4:29", true, false, 0xFF6FBF5A, 0xFF356B2A)
                )
            ),
            onIntent = {}
        )
    }
}

@Preview(name = "Favorites - Light Theme", showBackground = true, showSystemUi = true)
@Composable
private fun FavoritesScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        FavoritesScreen(
            state = FavoritesUiState(
                songs = listOf(
                    FavoriteSong("1", "Gece Yarısı", "Mavi Deniz", "3:34", true, false, 0xFF6FBF5A, 0xFF356B2A),
                    FavoriteSong("2", "Yıldız Tozu", "Polaris", "4:07", true, false, 0xFF3D5A80, 0xFF1B2A45),
                    FavoriteSong("3", "İlk Işık", "Sabah Ezgisi", "3:25", true, false, 0xFF5AAFC9, 0xFF2A5F73),
                    FavoriteSong("4", "Neon Sokaklar", "Şehir Işıkları", "3:43", true, true, 0xFFD98E4A, 0xFF8A5526),
                    FavoriteSong("5", "Derin Mavi", "Okyanus", "4:29", true, false, 0xFF6FBF5A, 0xFF356B2A)
                )
            ),
            onIntent = {}
        )
    }
}

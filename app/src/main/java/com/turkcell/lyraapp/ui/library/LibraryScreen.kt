package com.turkcell.lyraapp.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.library.LibraryPlaylist
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

@Composable
fun LibraryRoute(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LibraryEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    LibraryScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onIntent: (LibraryIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(top = 16.dp)
        ) {
            // Üst Başlık ve İkonlar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kütüphane",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { /* Arama fonksiyonu */ }) {
                        Icon(
                            imageVector = LyraIcons.Search,
                            contentDescription = "Ara",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { /* Ekle fonksiyonu */ }) {
                        Icon(
                            imageVector = LyraIcons.Add,
                            contentDescription = "Çalma Listesi Ekle",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filtre Chip Satırı
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val filters = listOf("Çalma listeleri", "Sanatçılar", "Albümler")
                items(filters) { filter ->
                    val isSelected = state.selectedFilter == filter
                    FilterChipItem(
                        text = filter,
                        isSelected = isSelected,
                        onClick = { onIntent(LibraryIntent.FilterSelected(filter)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sıralama ve Izgara Değiştirici Satırı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { /* Sıralama değiştirme */ }
                ) {
                    Icon(
                        imageVector = LyraIcons.SortArrows,
                        contentDescription = "Sıralama",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Son eklenenler",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

            }

            Spacer(modifier = Modifier.height(12.dp))

            // Liste Görünümü
            if (state.isLoading && state.playlists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(state.playlists, key = { it.id }) { item ->
                        LibraryPlaylistItem(item = item, onClick = { /* Playlist detaya git */ })
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = LyraIcons.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LibraryPlaylistItem(
    item: LibraryPlaylist,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sol Artwork
        if (item.isPinned) {
            // Beğenilen Şarkılar için Kalp İkonlu Özel Kutucuk
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(item.artworkStartColor), Color(item.artworkEndColor))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LyraIcons.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, // Kalp ikonu pembe/gül rengi
                    modifier = Modifier.size(28.dp)
                )
            }
        } else {
            // Standart Çalma Listeleri için Dalga Desenli/Gradyanlı Sanat Çalışması
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(item.artworkStartColor), Color(item.artworkEndColor))
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Orta Yazı Alanı
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.type,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary // "Çalma listesi" yazısı belirgin renk
                )
                Text(
                    text = " · ${item.songCount} şarkı",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Sağ İkon
        if (item.isPinned) {
            Icon(
                imageVector = LyraIcons.PushPin,
                contentDescription = "Sabitlendi",
                tint = MaterialTheme.colorScheme.primary, // Sabitleme ikonu pembe/gül rengi
                modifier = Modifier.size(18.dp)
            )
        } else {
            IconButton(onClick = { /* Menü aç */ }) {
                Icon(
                    imageVector = LyraIcons.MoreVert,
                    contentDescription = "Seçenekler",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(name = "Library - Light Theme", showBackground = true, showSystemUi = true)
@Composable
private fun LibraryScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        LibraryScreen(
            state = LibraryUiState(
                playlists = listOf(
                    LibraryPlaylist("1", "Beğenilen Şarkılar", "Çalma listesi", 5, true, 0xFFFFD9E2, 0xFF8F4A5F),
                    LibraryPlaylist("2", "Gece Sürüşü", "Çalma listesi", 6, false, 0xFF8B6FB8, 0xFF4A3D6B)
                )
            ),
            onIntent = {}
        )
    }
}

@Preview(name = "Library - Dark Theme", showBackground = true, showSystemUi = true)
@Composable
private fun LibraryScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        LibraryScreen(
            state = LibraryUiState(
                playlists = listOf(
                    LibraryPlaylist("1", "Beğenilen Şarkılar", "Çalma listesi", 5, true, 0xFFFFD9E2, 0xFF8F4A5F),
                    LibraryPlaylist("2", "Gece Sürüşü", "Çalma listesi", 6, false, 0xFF8B6FB8, 0xFF4A3D6B)
                )
            ),
            onIntent = {}
        )
    }
}

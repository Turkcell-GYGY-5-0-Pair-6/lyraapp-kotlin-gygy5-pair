package com.turkcell.lyraapp.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons

@Composable
fun MiniPlayerRoute(
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MiniPlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MiniPlayerBar(
        state = uiState,
        onIntent = viewModel::onIntent,
        onCardClick = onCardClick,
        modifier = modifier
    )
}

@Composable
fun MiniPlayerBar(
    state: MiniPlayerUiState,
    onIntent: (MiniPlayerIntent) -> Unit,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val playbackState = state.playbackState ?: return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable { onCardClick(playbackState.songId) }
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art
                Artwork(
                    startColor = playbackState.artworkStartColor,
                    endColor = playbackState.artworkEndColor,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))

                // Song Title and Artist
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = playbackState.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = playbackState.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Like button (Heart)
                IconButton(
                    onClick = { onIntent(MiniPlayerIntent.ToggleLike) }
                ) {
                    Icon(
                        imageVector = if (playbackState.isLiked) LyraIcons.Favorite else LyraIcons.FavoriteOutlined,
                        contentDescription = "Beğen",
                        tint = if (playbackState.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Skip Previous button
                IconButton(
                    onClick = { onIntent(MiniPlayerIntent.SkipPrevious) }
                ) {
                    Icon(
                        imageVector = SkipPreviousIcon,
                        contentDescription = "Önceki Şarkı",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Play/Pause button
                IconButton(
                    onClick = { onIntent(MiniPlayerIntent.TogglePlayPause) }
                ) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) PauseIcon else LyraIcons.PlayArrow,
                        contentDescription = if (playbackState.isPlaying) "Duraklat" else "Oynat",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Skip Next button
                IconButton(
                    onClick = { onIntent(MiniPlayerIntent.SkipNext) }
                ) {
                    Icon(
                        imageVector = SkipNextIcon,
                        contentDescription = "Sonraki Şarkı",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Slim progress bar at the bottom
            val progressFraction = if (playbackState.durationMs > 0) {
                playbackState.currentProgressMs.toFloat() / playbackState.durationMs
            } else 0f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun Artwork(
    startColor: Long,
    endColor: Long,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Brush.linearGradient(listOf(Color(startColor), Color(endColor))))
            .background(
                Brush.radialGradient(
                    listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                ),
            ),
    )
}

private val PauseIcon: ImageVector by lazy {
    ImageVector.Builder("Pause", 24.dp, 24.dp, 24f, 24f)
        .addPath(PathParser().parsePathString("M6 19h4V5H6v14zm8-14v14h4V5h-4z").toNodes(), fill = SolidColor(Color.Black)).build()
}

private val SkipPreviousIcon: ImageVector by lazy {
    ImageVector.Builder("SkipPrevious", 24.dp, 24.dp, 24f, 24f)
        .addPath(PathParser().parsePathString("M 6 6 H 8 V 18 H 6 Z M 9.5 12 L 18 18 V 6 Z").toNodes(), fill = SolidColor(Color.Black)).build()
}

private val SkipNextIcon: ImageVector by lazy {
    ImageVector.Builder("SkipNext", 24.dp, 24.dp, 24f, 24f)
        .addPath(PathParser().parsePathString("M 6 18 L 14.5 12 L 6 6 Z M 16 6 H 18 V 18 H 16 Z").toNodes(), fill = SolidColor(Color.Black)).build()
}

package com.turkcell.lyraapp.ui.home

import com.turkcell.lyraapp.data.home.HomeSong
import com.turkcell.lyraapp.data.home.PlaylistForYou
import com.turkcell.lyraapp.data.home.QuickPick
import com.turkcell.lyraapp.data.home.RecentlyPlayed

/**
 * Home ekranının MVI sözleşmesi: State (durum), Intent (kullanıcı niyeti) ve
 * Effect (tek seferlik olay) tek dosyada toplanmıştır.
 */


data class HomeUiState(
    val isLoading: Boolean = false,
    val greeting: String = "",
    val userInitials: String = "",
    val songs: List<HomeSong> = emptyList(),
    val quickPicks: List<QuickPick> = emptyList(),
    val recentlyPlayed: List<RecentlyPlayed> = emptyList(),
    val playlistsForYou: List<PlaylistForYou> = emptyList(),

)

sealed interface HomeIntent {
    data object Retry : HomeIntent
    data class PlaylistClicked(val playlistId: String) : HomeIntent
    data class SongSelected(val song: HomeSong) : HomeIntent
}

sealed interface HomeEffect {
    data class ShowError(val message: String) : HomeEffect
    data class NavigateToPlaylistDetail(val playlistId: String) : HomeEffect
    data class NavigateToNowPlaying(val songId: String) : HomeEffect
}

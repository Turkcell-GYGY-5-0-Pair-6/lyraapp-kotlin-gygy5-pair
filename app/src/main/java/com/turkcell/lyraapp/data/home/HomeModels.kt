package com.turkcell.lyraapp.data.home

data class HomeFeed(
    val userInitials: String,
    val songs: List<HomeSong>,
    val quickPicks: List<QuickPick>,
    val recentlyPlayed: List<RecentlyPlayed>,
    val playlistsForYou: List<PlaylistForYou>,
)

/**
 * "Şarkılar" bölümündeki, API'dan (`GET /api/v1/songs`) gelen şarkı.
 *
 * API'da kapak görseli/BG bilgisi olmadığından [artworkStartColor]/[artworkEndColor] gradyan
 * renk çifti (ARGB hex) şarkı [id]'sinden deterministik olarak türetilir; aynı şarkı her zaman
 * aynı rengi alır (bkz. docs/decisions.md — Şarkı Listesi API Entegrasyonu).
 */
data class HomeSong(
    val id: String,
    val title: String,
    val artist: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)

data class QuickPick(
    val id: String,
    val title: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)

data class RecentlyPlayed(
    val id: String,
    val title: String,
    val subtitle: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)

data class PlaylistForYou(
    val id: String,
    val title: String,
    val artist: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)
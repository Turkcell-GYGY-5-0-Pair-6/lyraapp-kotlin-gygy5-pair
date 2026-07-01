package com.turkcell.lyraapp.ui.navigation

enum class LyraDestination(val route: String) {
    Login("login"),
    Register("register"),
    Home("home"),
    Search("search"),
    Library("library"),
    Favorites("favorites"),
    Profile("profile"),
    CreatePlaylist("create_playlist"),
    PlaylistDetail("playlist_detail/{playlistId}"),
    NowPlaying("now_playing/{songId}"),
    Premium("premium"),
}
package com.turkcell.lyraapp.ui.home

/**
 * Home ekranının MVI sözleşmesi: State (durum), Intent (kullanıcı niyeti) ve
 * Effect (tek seferlik olay) tek dosyada toplanmıştır.
 */

data class HomeUiState(
    val headline: String = "Lyra'ya hoş geldin",
    val description: String = "En sevdiğin şarkıları keşfetmeye başla.",
)

sealed interface HomeIntent {
    data object LogoutClicked : HomeIntent
}

sealed interface HomeEffect {
    data object NavigateToLogin : HomeEffect
}

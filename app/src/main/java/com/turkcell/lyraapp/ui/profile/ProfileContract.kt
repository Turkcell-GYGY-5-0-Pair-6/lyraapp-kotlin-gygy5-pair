package com.turkcell.lyraapp.ui.profile

import com.turkcell.lyraapp.data.profile.UserProfile

/**
 * Profil ekranının MVI sözleşmesi: State (durum), Intent (kullanıcı niyeti) ve
 * Effect (tek seferlik olay) tek dosyada tanımlanmıştır.
 */

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface ProfileIntent {
    data object LoadProfile : ProfileIntent
    data object Logout : ProfileIntent
}

sealed interface ProfileEffect {
    data object NavigateToLogin : ProfileEffect
    data class ShowError(val message: String) : ProfileEffect
}

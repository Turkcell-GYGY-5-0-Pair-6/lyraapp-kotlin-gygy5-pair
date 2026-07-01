package com.turkcell.lyraapp.ui.premium

/**
 * Premium ekranının MVI sözleşmesi: State (durum), Intent (kullanıcı niyeti) ve
 * Effect (tek seferlik olay) tek dosyada tanımlanmıştır.
 */
data class PremiumUiState(
    val selectedPlanId: String = "recurring", // "recurring" veya "one-time"
    val isLoading: Boolean = false
)

sealed interface PremiumIntent {
    data class SelectPlan(val planId: String) : PremiumIntent
    data object BackClick : PremiumIntent
}

sealed interface PremiumEffect {
    data object NavigateBack : PremiumEffect
}

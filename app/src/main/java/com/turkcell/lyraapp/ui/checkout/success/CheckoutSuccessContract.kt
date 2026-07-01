package com.turkcell.lyraapp.ui.checkout.success

/**
 * Ödeme başarılı ekranının MVI sözleşmesi: State (durum), Intent (kullanıcı niyeti) ve
 * Effect (tek seferlik olay) tanımlarını içerir.
 */
data class CheckoutSuccessUiState(
    val isLoading: Boolean = false
)

sealed interface CheckoutSuccessIntent {
    data object StartListeningClick : CheckoutSuccessIntent
}

sealed interface CheckoutSuccessEffect {
    data object NavigateToHome : CheckoutSuccessEffect
}

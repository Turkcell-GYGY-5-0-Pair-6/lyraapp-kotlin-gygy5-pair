package com.turkcell.lyraapp.ui.checkout

/**
 * Ödeme ekranının MVI sözleşmesi: State (durum), Intent (kullanıcı niyeti) ve
 * Effect (tek seferlik olay) tanımlarını içerir.
 */
data class CheckoutUiState(
    val planId: String = "recurring", // "recurring" veya "one-time"
    val cardNumber: String = "",
    val cardHolderName: String = "",
    val expiryDate: String = "", // AA/YY formatında
    val cvc: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFormValid: Boolean = false // Türetilen alan
)

sealed interface CheckoutIntent {
    data class InitPlan(val planId: String) : CheckoutIntent
    data class CardNumberChanged(val value: String) : CheckoutIntent
    data class CardHolderNameChanged(val value: String) : CheckoutIntent
    data class ExpiryDateChanged(val value: String) : CheckoutIntent
    data class CvcChanged(val value: String) : CheckoutIntent
    data object SubmitPayment : CheckoutIntent
    data object BackClick : CheckoutIntent
}

sealed interface CheckoutEffect {
    data object NavigateBack : CheckoutEffect
    data object NavigateToProfile : CheckoutEffect
    data class ShowError(val message: String) : CheckoutEffect
}

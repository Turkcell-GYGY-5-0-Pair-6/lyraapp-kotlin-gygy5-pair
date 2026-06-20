package com.turkcell.lyraapp.ui.auth.login

/**
 * Login ekranının MVI sözleşmesi: State (durum), Intent (kullanıcı niyeti) ve
 * Effect (tek seferlik olay) tek dosyada toplanmıştır.
 */

/**
 * Ekranın gözlemlenebilir tüm durumu. Tek bir immutable kaynak (single source of truth).
 */
data class LoginUiState(
    val phoneNumber: String = "",
    val verificationCode: String = "",
    val isOtpSent: Boolean = false,
    val firstTime: Boolean = false,
    val isLoading: Boolean = false,
    val isRequestOtpEnabled: Boolean = false,
    val isVerifyOtpEnabled: Boolean = false,
)

/**
 * Kullanıcıdan gelen niyetler.
 */
sealed interface LoginIntent {
    data class PhoneNumberChanged(val value: String) : LoginIntent
    data class VerificationCodeChanged(val value: String) : LoginIntent
    data object SubmitPhone : LoginIntent
    data object SubmitVerify : LoginIntent
    data object ResendOtp : LoginIntent
    data object BackToPhoneEntry : LoginIntent
}

/**
 * Tek seferlik (one-shot) olaylar: navigasyon, snackbar vb.
 */
sealed interface LoginEffect {
    data object NavigateToHome : LoginEffect
    data object NavigateToRegister : LoginEffect
    data class ShowError(val message: String) : LoginEffect
}

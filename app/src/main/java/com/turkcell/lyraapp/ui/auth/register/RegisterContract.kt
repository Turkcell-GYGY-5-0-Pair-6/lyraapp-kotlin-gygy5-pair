package com.turkcell.lyraapp.ui.auth.register

/**
 * Register ("Bilgilerini tamamla") ekranının MVI sözleşmesi: State (durum), Intent (kullanıcı niyeti)
 * ve Effect (tek seferlik olay) tek dosyada toplanmıştır.
 */
data class RegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val birthDay: String = "",
    val birthMonth: String = "",
    val birthYear: String = "",
    val isLoading: Boolean = false,
    val isCompleteEnabled: Boolean = false,
)

/**
 * Kullanıcıdan gelen niyetler.
 */
sealed interface RegisterIntent {
    data class FirstNameChanged(val value: String) : RegisterIntent
    data class LastNameChanged(val value: String) : RegisterIntent
    data class BirthDayChanged(val value: String) : RegisterIntent
    data class BirthMonthChanged(val value: String) : RegisterIntent
    data class BirthYearChanged(val value: String) : RegisterIntent
    data object Submit : RegisterIntent
    data object BackClicked : RegisterIntent
}

/**
 * Tek seferlik (one-shot) olaylar.
 */
sealed interface RegisterEffect {
    data object NavigateToHome : RegisterEffect
    data object NavigateBack : RegisterEffect
    data class ShowError(val message: String) : RegisterEffect
}

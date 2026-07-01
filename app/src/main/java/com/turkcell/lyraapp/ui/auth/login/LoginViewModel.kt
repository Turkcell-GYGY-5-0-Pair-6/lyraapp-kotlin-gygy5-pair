package com.turkcell.lyraapp.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.AuthRepository
import com.turkcell.lyraapp.data.network.toUserFriendlyMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Login ekranının MVI ViewModel'i.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LoginEffect>(Channel.BUFFERED)
    val effect: Flow<LoginEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.PhoneNumberChanged -> updateForm { it.copy(phoneNumber = intent.value) }
            is LoginIntent.VerificationCodeChanged -> updateForm { it.copy(verificationCode = intent.value) }
            is LoginIntent.SubmitPhone -> submitPhone()
            is LoginIntent.SubmitVerify -> submitVerify()
            is LoginIntent.ResendOtp -> resendOtp()
            is LoginIntent.BackToPhoneEntry -> _uiState.update {
                it.copy(isOtpSent = false, verificationCode = "")
            }
        }
    }

    private fun updateForm(transform: (LoginUiState) -> LoginUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.copy(
                isRequestOtpEnabled = updated.phoneNumber.isValidPhoneNumber(),
                isVerifyOtpEnabled = updated.verificationCode.isValidVerificationCode()
            )
        }
    }

    private fun submitPhone() {
        val state = _uiState.value
        if (!state.isRequestOtpEnabled || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val formattedPhone = "+90" + state.phoneNumber.filter { it.isDigit() }
            val result = authRepository.requestOtp(formattedPhone)
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            isOtpSent = true,
                            firstTime = data.firstTime
                        )
                    }
                }
                .onFailure { error ->
                    val userFriendlyError = error.toUserFriendlyMessage(
                        fallbackMessage = "Kod gönderme başarısız."
                    )
                    _effect.send(LoginEffect.ShowError(userFriendlyError))
                }
        }
    }

    private fun resendOtp() {
        val state = _uiState.value
        if (state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val formattedPhone = "+90" + state.phoneNumber.filter { it.isDigit() }
            val result = authRepository.requestOtp(formattedPhone)
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess { data ->
                    _uiState.update { it.copy(firstTime = data.firstTime) }
                    _effect.send(LoginEffect.ShowError("Kod tekrar gönderildi."))
                }
                .onFailure { error ->
                    val userFriendlyError = error.toUserFriendlyMessage(
                        fallbackMessage = "Kod gönderme başarısız."
                    )
                    _effect.send(LoginEffect.ShowError(userFriendlyError))
                }
        }
    }

    private fun submitVerify() {
        val state = _uiState.value
        if (!state.isVerifyOtpEnabled || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val formattedPhone = "+90" + state.phoneNumber.filter { it.isDigit() }
            val result = authRepository.verifyOtp(formattedPhone, state.verificationCode)
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess {
                    if (state.firstTime) {
                        _effect.send(LoginEffect.NavigateToRegister)
                    } else {
                        _effect.send(LoginEffect.NavigateToHome)
                    }
                }
                .onFailure { error ->
                    val userFriendlyError = error.toUserFriendlyMessage(
                        fallbackMessage = "Kod doğrulama başarısız.",
                        unauthorizedMessage = "Girdiğiniz doğrulama kodu hatalıdır. Lütfen kontrol edin."
                    )
                    _effect.send(LoginEffect.ShowError(userFriendlyError))
                }
        }
    }
}

private fun String.isValidPhoneNumber(): Boolean {
    val digits = filter { it.isDigit() }
    return digits.length == 10
}

private fun String.isValidVerificationCode(): Boolean {
    return filter { it.isDigit() }.length == 6
}
package com.turkcell.lyraapp.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.profile.ProfileRepository
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
 * Ödeme ekranının MVI ViewModel sınıfı.
 * Kullanıcı girdilerini formatlar, doğrular ve ödeme işlemini başlatır.
 */
@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    private val planId: String = savedStateHandle["planId"] ?: "recurring"

    private val _uiState = MutableStateFlow(CheckoutUiState(planId = planId))
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CheckoutEffect>(Channel.BUFFERED)
    val effect: Flow<CheckoutEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: CheckoutIntent) {
        when (intent) {
            is CheckoutIntent.InitPlan -> _uiState.update { it.copy(planId = intent.planId) }
            is CheckoutIntent.CardNumberChanged -> updateForm { it.copy(cardNumber = formatCardNumber(intent.value)) }
            is CheckoutIntent.CardHolderNameChanged -> updateForm { it.copy(cardHolderName = intent.value) }
            is CheckoutIntent.ExpiryDateChanged -> updateForm { it.copy(expiryDate = formatExpiryDate(intent.value)) }
            is CheckoutIntent.CvcChanged -> updateForm { it.copy(cvc = formatCvc(intent.value)) }
            is CheckoutIntent.SubmitPayment -> submitPayment()
            is CheckoutIntent.BackClick -> navigateBack()
        }
    }

    private fun updateForm(transform: (CheckoutUiState) -> CheckoutUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.copy(isFormValid = updated.calculateFormValid())
        }
    }

    private fun CheckoutUiState.calculateFormValid(): Boolean {
        val isCardValid = cardNumber.length == 16
        val isNameValid = cardHolderName.trim().isNotBlank()
        val isExpiryValid = expiryDate.length == 4
        val isCvcValid = cvc.length in 3..4
        return isCardValid && isNameValid && isExpiryValid && isCvcValid
    }

    private fun formatCardNumber(input: String): String {
        return input.filter { it.isDigit() }.take(16)
    }

    private fun formatExpiryDate(input: String): String {
        return input.filter { it.isDigit() }.take(4)
    }

    private fun formatCvc(input: String): String {
        return input.filter { it.isDigit() }.take(4)
    }

    private fun submitPayment() {
        val state = _uiState.value
        if (!state.isFormValid || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val expMonth = if (state.expiryDate.length >= 2) state.expiryDate.substring(0, 2).toIntOrNull() ?: 1 else 1
            val rawYear = if (state.expiryDate.length >= 4) state.expiryDate.substring(2, 4).toIntOrNull() ?: 30 else 30
            val expYear = 2000 + rawYear

            val result = profileRepository.checkout(
                plan = state.planId,
                cardNumber = state.cardNumber,
                expMonth = expMonth,
                expYear = expYear,
                cvc = state.cvc,
                holderName = state.cardHolderName
            )

            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess {
                    _effect.send(CheckoutEffect.NavigateToSuccess)
                }
                .onFailure { error ->
                    val errorMsg = error.message ?: "Ödeme gerçekleştirilemedi. Lütfen bilgilerinizi kontrol edin."
                    _uiState.update { it.copy(error = errorMsg) }
                    _effect.send(CheckoutEffect.ShowError(errorMsg))
                }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effect.send(CheckoutEffect.NavigateBack)
        }
    }
}

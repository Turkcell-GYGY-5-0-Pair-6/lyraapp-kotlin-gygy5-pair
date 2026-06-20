package com.turkcell.lyraapp.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.AuthRepository
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
 * Register ("Bilgilerini tamamla") ekranının MVI ViewModel'i.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _effect = Channel<RegisterEffect>(Channel.BUFFERED)
    val effect: Flow<RegisterEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.FirstNameChanged -> updateForm { it.copy(firstName = intent.value) }
            is RegisterIntent.LastNameChanged -> updateForm { it.copy(lastName = intent.value) }
            is RegisterIntent.BirthDayChanged -> {
                val filtered = intent.value.filter { it.isDigit() }
                if (filtered.length <= 2) {
                    updateForm { it.copy(birthDay = filtered) }
                }
            }
            is RegisterIntent.BirthMonthChanged -> {
                val filtered = intent.value.filter { it.isDigit() }
                if (filtered.length <= 2) {
                    updateForm { it.copy(birthMonth = filtered) }
                }
            }
            is RegisterIntent.BirthYearChanged -> {
                val filtered = intent.value.filter { it.isDigit() }
                if (filtered.length <= 4) {
                    updateForm { it.copy(birthYear = filtered) }
                }
            }
            is RegisterIntent.Submit -> submit()
            is RegisterIntent.BackClicked -> sendEffect(RegisterEffect.NavigateBack)
        }
    }

    private fun updateForm(transform: (RegisterUiState) -> RegisterUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.copy(isCompleteEnabled = updated.isFormValid())
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.isCompleteEnabled || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val dayFormatted = state.birthDay.padStart(2, '0')
            val monthFormatted = state.birthMonth.padStart(2, '0')
            val birthDateString = "${state.birthYear}-${monthFormatted}-${dayFormatted}"

            val result = authRepository.updateInformation(
                firstName = state.firstName.trim(),
                lastName = state.lastName.trim(),
                birthDate = birthDateString
            )
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess { _effect.send(RegisterEffect.NavigateToHome) }
                .onFailure { error ->
                    _effect.send(RegisterEffect.ShowError(error.message ?: "Profil bilgileri kaydedilemedi."))
                }
        }
    }

    private fun sendEffect(effect: RegisterEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}

private fun RegisterUiState.isFormValid(): Boolean {
    val day = birthDay.toIntOrNull() ?: 0
    val month = birthMonth.toIntOrNull() ?: 0
    val year = birthYear.toIntOrNull() ?: 0
    return firstName.isNotBlank() &&
            lastName.isNotBlank() &&
            birthDay.length == 2 && day in 1..31 &&
            birthMonth.length == 2 && month in 1..12 &&
            birthYear.length == 4 && year in 1900..2026
}
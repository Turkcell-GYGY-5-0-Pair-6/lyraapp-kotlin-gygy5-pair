package com.turkcell.lyraapp.ui.checkout.success

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ödeme başarılı ekranının MVI ViewModel sınıfı.
 */
@HiltViewModel
class CheckoutSuccessViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutSuccessUiState())
    val uiState: StateFlow<CheckoutSuccessUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CheckoutSuccessEffect>(Channel.BUFFERED)
    val effect: Flow<CheckoutSuccessEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: CheckoutSuccessIntent) {
        when (intent) {
            is CheckoutSuccessIntent.StartListeningClick -> startListening()
        }
    }

    private fun startListening() {
        viewModelScope.launch {
            _effect.send(CheckoutSuccessEffect.NavigateToHome)
        }
    }
}

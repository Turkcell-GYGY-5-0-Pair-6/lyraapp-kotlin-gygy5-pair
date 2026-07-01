package com.turkcell.lyraapp.ui.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * Premium ekranının MVI ViewModel sınıfı.
 * Tek giriş noktası [onIntent]'tir.
 */
@HiltViewModel
class PremiumViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PremiumEffect>(Channel.BUFFERED)
    val effect: Flow<PremiumEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: PremiumIntent) {
        when (intent) {
            is PremiumIntent.SelectPlan -> selectPlan(intent.planId)
            is PremiumIntent.BackClick -> navigateBack()
        }
    }

    private fun selectPlan(planId: String) {
        _uiState.update { it.copy(selectedPlanId = planId) }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effect.send(PremiumEffect.NavigateBack)
        }
    }
}
